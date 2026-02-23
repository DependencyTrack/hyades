| Status                                               | Date       | Author(s)                            |
|:-----------------------------------------------------|:-----------|:-------------------------------------|
| Accepted                                             | 2026-02-23 | [@nscuro](https://github.com/nscuro) |

## Context

Vulnerability aliases are stored in the denormalized `VULNERABILITYALIAS` table:

| Column      | Type   | Constraints      |
|:------------|:-------|:-----------------|
| ID          | BIGINT | PK               |
| CVE_ID      | TEXT   |                  |
| GHSA_ID     | TEXT   |                  |
| GSD_ID      | TEXT   |                  |
| INTERNAL_ID | TEXT   |                  |
| OSV_ID      | TEXT   |                  |
| SNYK_ID     | TEXT   |                  |
| SONATYPE_ID | TEXT   |                  |
| VULNDB_ID   | TEXT   |                  |
| UUID        | UUID   | NOT NULL, UNIQUE |

This design poses a few challenges:

* Rows lack a natural key, making it impossible to detect and prevent duplicates.
* Modifying rows (i.e. adding a new ID to an existing alias group) is prone to race conditions.
* Due to the combination of the points above, batching operations on this table is not possible.
* Vulnerability sources are hardcoded as columns, making it unnecessarily challenging to add new sources.
* Querying the table is unnecessarily hard, as it requires the caller to know what column to query on.

The [logic to create or modify alias records](https://github.com/DependencyTrack/hyades-apiserver/blob/f969a32387c03b45eff186e2fcc4ba900a7059f9/apiserver/src/main/java/org/dependencytrack/persistence/VulnerabilityQueryManager.java#L474-L591)
is brittle and non-deterministic. Making it concurrency-safe would require acquisition of coarse advisory locks.

Alias synchronization unfortunately is in the hot path for vulnerability analysis result reconciliation,
and is performed concurrently with potentially overlapping data. To ensure that synchronization is both
performant and correct, we need a solution that allows us to batch database operations,
while effectively shielding us against data races.

## Decision

Normalize the data into the following schema:

| Column   | Type | Constraints |
|:---------|:-----|:------------|
| GROUP_ID | UUID | NOT NULL    |
| SOURCE   | TEXT | PK          |
| VULN_ID  | TEXT | PK          |

* The separate ID columns are collapsed into `SOURCE` and `VULN_ID`.
* `SOURCE` and `VULN_ID` form the natural (primary) key, effectively preventing duplicates.
* Alias relationships are identified via matching `GROUP_ID`.

A secondary index on `GROUP_ID` supports the group lookup pattern used by all alias queries:

| Index                        | Column(s) |
|:-----------------------------|:----------|
| `VULNERABILITY_ALIAS_PK`    | `SOURCE`, `VULN_ID` |
| `VULNERABILITY_ALIAS_GROUP_IDX` | `GROUP_ID` |

### Querying

To query all aliases of a vulnerability identified by `source` and `vulnId`, *excluding the input pair itself*:

```sql
SELECT va.*
  FROM "VULNERABILITY_ALIAS" AS va
 WHERE va."GROUP_ID" IN (
   SELECT va2."GROUP_ID"
     FROM "VULNERABILITY_ALIAS" AS va2
    WHERE va2."SOURCE" = :source
      AND va2."VULN_ID" = :vulnId
 )
   AND (va."SOURCE", va."VULN_ID") != (:source, :vulnId)
```

### Synchronization Algorithm

Given a set of vulnerability aliases to synchronize:

```js
[
  {cveId: 'CVE-1', ghsaId: 'GHSA-1'},
  {cveId: 'CVE-1', snykId: 'SNYK-1'}
]
```

1. Begin transaction.
2. Compute unique source ↔ vuln ID pairs:
   ```js
   [
     {source: 'NVD', vulnId: 'CVE-1'},
     {source: 'GITHUB', vulnId: 'GHSA-1'},
     {source: 'SNYK', vulnId: 'SNYK-1'}
   ]
   ```
3. Compute edges between pairs, using their indices as nodes:
   ```js
   [
     {from: 0, to: 1},
     {from: 0, to: 2}
   ]
   ```
4. Acquire PostgreSQL advisory locks for all unique source ↔ vuln ID pairs,
   ordered by key to prevent deadlocks between concurrent transactions:
   ```sql
   SELECT PG_ADVISORY_XACT_LOCK(HASHTEXT(key))
     FROM (
       SELECT DISTINCT UNNEST(ARRAY['NVD|CVE-1', 'GITHUB|GHSA-1', 'SNYK|SNYK-1']) AS key
       ORDER BY 1
     ) AS t
   ```
   `HASHTEXT` returns `int4`, so hash collisions are expected. Collisions do not affect
   correctness — they only cause unrelated syncs to serialize, which is acceptable.
5. Query existing alias records for all unique source ↔ vuln ID pairs:
   ```sql
   SELECT "SOURCE"
        , "VULN_ID"
        , "GROUP_ID"
     FROM "VULNERABILITY_ALIAS"
    WHERE ("SOURCE", "VULN_ID") IN (
      ('NVD', 'CVE-1')
    , ('GITHUB', 'GHSA-1')
    , ('SNYK', 'SNYK-1')
    )
   ```
6. Leverage the [union-find] data structure to identify alias groups.
    * Values are the indices of source ↔ vuln ID pairs.
    * Sets are merged using the edges computed earlier.
    * Result: [connected components] representing alias groups.
    * Implementation instructions can be found [here](https://cp-algorithms.com/data_structures/disjoint_set_union.html).
7. For each identified alias group:
    1. Identify existing alias groups that source ↔ vuln ID pairs are already part of.
        1. If at least one existing group: Pick the lowest existing group UUID (deterministic).
        2. If no existing group: Generate new group ID.
    2. If more than one existing group, merge them:
       ```sql
       UPDATE "VULNERABILITY_ALIAS"
          SET "GROUP_ID" = :toGroup
        WHERE "GROUP_ID" = ANY(:fromGroups)
          AND "GROUP_ID" != :toGroup
       ```
8. Batch-insert alias records:
   ```sql
   INSERT INTO "VULNERABILITY_ALIAS" ("GROUP_ID", "SOURCE", "VULN_ID")
   SELECT *
     FROM (
       VALUES ('91f5876f-01cb-4ebe-b1f3-a0e46cd16fbd'::UUID, 'NVD', 'CVE-1')
            , ('91f5876f-01cb-4ebe-b1f3-a0e46cd16fbd'::UUID, 'GITHUB', 'GHSA-1')
            , ('91f5876f-01cb-4ebe-b1f3-a0e46cd16fbd'::UUID, 'SNYK', 'SNYK-1')
     ) AS t
   ON CONFLICT DO NOTHING
   ```
9. Commit transaction and release locks (implicit).

!!! note
    The SQL snippets above use `FROM ... VALUES` for brevity. The actual implementation
    uses `FROM UNNEST`, which is both more idiomatic and has better performance characteristics.

While this still relies on advisory locks to prevent data races, it only acquires locks
for source ↔ vuln ID pairs that it is actually processing.

Its ability to batch `SELECT`, `UPDATE`, and `INSERT` operations means it can complete
faster and with less overhead than the previous implementation. It also allows us to
process aliases of multiple vulnerabilities at once, without sacrificing performance
or correctness.

The `INSERT`'s `ON CONFLICT DO NOTHING` clause avoids unnecessary database writes.

### Data Migration

Existing data is migrated from `VULNERABILITYALIAS` to `VULNERABILITY_ALIAS` via Liquibase.
The migration replicates the [synchronization algorithm](#synchronization-algorithm) in SQL.

The old `VULNERABILITYALIAS` table is dropped afterwards.

An integration test verifies that the migration works as expected,
including the handling of potential duplicates in the existing data set.

## Consequences

* Adding new vulnerability sources requires no schema changes.
* Alias synchronization can be fully batched, reducing round trips in the hot path.
* The natural primary key prevents duplicate alias entries by construction.
* Querying aliases is uniform, and callers no longer need source-specific column knowledge.
* The old `UUID` column is dropped. Any external references to alias records by UUID will break.
  No known external consumers depend on this identifier.
* Advisory locks add contention under concurrent writes to overlapping alias sets.
  This is bounded by the lock granularity (per source ↔ vuln ID pair), and acceptable
  given the correctness guarantees it provides.
* Group merges (`UPDATE ... SET "GROUP_ID"`) touch all rows in the groups being unified.
  In practice, alias groups are small (< 5 members), so this is negligible.

[connected components]: https://en.wikipedia.org/wiki/Component_(graph_theory)
[union-find]: https://en.wikipedia.org/wiki/Disjoint-set_data_structure