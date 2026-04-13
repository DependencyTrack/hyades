| Status   | Date       | Author(s)                                    |
|:---------|:-----------|:---------------------------------------------|
| Proposed | 2026-04-13 | [@nscuro](https://github.com/nscuro)         |

## Context

Dependency-Track's vulnerability policy system currently only supports policies that are synchronized from a
remote bundle, a ZIP archive of YAML files fetched periodically. While this approach works well for centrally
managed policy sets distributed via CI pipelines, it presents a high barrier to entry for organizations that
want to define policies directly through the UI or API. There is no way for a user to create, edit, or delete
a vulnerability policy without modifying the bundle and triggering a sync.

Additionally, the current system lacks mechanisms for controlling the order in which policies are evaluated,
and for scoping policies to specific projects. All policies apply globally and are evaluated in insertion order,
which makes it difficult to express intent like "this policy should only apply to payment services" or
"this targeted override should take precedence over the general baseline."

Several community requests have converged on the need for user-managed policies with scoping capabilities.
The existing `TAG` infrastructure is already used throughout the system for project classification, making it
a natural fit for policy scoping without introducing new concepts.

## Decision

We will extend the vulnerability policy system to support user-managed policies alongside the existing
bundle-sync mechanism. The two sources coexist in the same `VULNERABILITY_POLICY` table, distinguished by
the presence or absence of a foreign key to `VULNERABILITY_POLICY_BUNDLE`.

**Identification.** Each policy receives a stable `UUID` column that serves as the external API identifier.
The existing `NAME` column remains globally unique across both user-managed and bundle-synced policies, avoiding
confusion from duplicate names. The UUID is immutable and generated server-side.

**Source discrimination.** Rather than introducing a separate `SOURCE` enum column, we derive source from
`VULNERABILITY_POLICY_BUNDLE_ID`. When it is `NULL`, the policy is user-managed. When it references a bundle,
the policy is bundle-managed. This avoids data inconsistency between a source flag and the actual FK state.
The API exposes a read-only `source` field (`USER` or `BUNDLE`) computed from this column.

**Priority ordering.** We add a `PRIORITY` column (integer, 0 to 100, lower values indicating higher precedence)
to give explicit control over evaluation order. Within the same priority level, project-scoped policies (those
with tag bindings) evaluate before global policies (those without), following the principle that more specific
rules should override broader defaults. This two-level ordering, explicit priority first and then specificity,
provides both control and sensible defaults.

**Tag-based project scoping.** We introduce a `VULNERABILITY_POLICIES_TAGS` join table linking policies to tags.
When a policy has tag bindings, it applies only to projects that have at least one of the bound tags. When a
policy has no tag bindings, it applies globally. This reuses the existing `TAG` table and `PROJECTS_TAGS`
relationship, keeping the concept space small.

**Bundle isolation.** The bundle sync mechanism is updated to only manage policies that belong to its bundle
(identified by `VULNERABILITY_POLICY_BUNDLE_ID`). It cannot create, modify, or delete user-managed policies.
Conversely, the CRUD API rejects modifications to bundle-managed policies.
Deletion of a bundle cascades to its policies via the FK's `ON DELETE CASCADE`,
with analysis reset and audit trail creation for each affected policy.

**REST API.** User-managed policy CRUD is exposed under `/api/v2/vuln-policies`,
bundle management is exposed under `/api/v2/vuln-policy-bundles`.

## Consequences

Organizations can now manage vulnerability policies directly through the UI and API without needing to set up
a bundle repository and CI pipeline. This lowers the barrier to entry significantly, especially for smaller
teams or those evaluating the feature.

The priority and tag-scoping mechanisms introduce more expressive policy evaluation, but also more complexity
in understanding which policy will match a given finding. The evaluation order (priority, then scoped-before-global,
then first-match-wins) must be clearly communicated in documentation to avoid user confusion.

Bundle-synced and user-managed policies share a single name namespace. This is intentional, as it prevents
the confusing situation where two policies with the same name behave differently. However, it means that a
bundle sync can fail if it tries to create a policy whose name is already taken by a user-managed policy.
This is an acceptable trade-off, because name conflicts surface as clear errors rather than silent shadowing.

The `VULNERABILITY_POLICIES_TAGS` join table adds a new relationship to the `TAG` entity.
The tag maintenance task must be updated to also check this table when determining
whether a tag is orphaned.

The database migration backfills `VULNERABILITY_POLICY_BUNDLE_ID` on all existing policies, associating them
with the existing bundle record. This means that after migration, all pre-existing policies are bundle-managed
and the system behaves identically to before until users explicitly create new policies.