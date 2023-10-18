## Introduction

Dependency-Track allows policy conditions to be defined using the [Common Expression Language] (CEL),
enabling more flexibility, and more control compared to predefined conditions.

To use CEL, simply select the subject `Expression` when adding a new condition. A code editor will appear in which
expressions can be provided.

![CEL condition](../../images/usage/policy-compliance/expression-condition.png)

In addition to the expression itself, it's necessary to specify a violation type, which may be any of `License`,
`Operational`, or `Security`. The violation type aids in communicating what kind of risk is introduced by the
condition being matched.

## Syntax

The CEL syntax is similar to other [C-style languages] like Java and JavaScript.
However, CEL is not [Turing-complete]. As such, it does *not* support constructs like `if` statements or loops (i.e. `for`, `while`).

As a compensation for missing loops, CEL offers [macros] like `all`, `exists`, `exists_one`, `map`, and `filter`.

## Evaluation Context

Conditions are scoped to individual components.  
Each condition is evaluated for every single component in a project.

The context in which expressions are evaluated in contains the following variables:

| Variable    | Type                               | Description                                  |
|:------------|:-----------------------------------|:---------------------------------------------|
| `component` | <code>[Component]</code>           | The component being evaluated                |
| `project`   | <code>[Project]</code>             | The project the component is part of         |
| `vulns`     | <code>list([Vulnerability])</code> | Vulnerabilities the component is affected by |

## Best Practices

1. **Keep expressions simple and concise**. The more complex an expression becomes, the harder it gets to determine why
it did or did not match. Use policy operators (`Any`, `All`) to chain multiple expressions if practical.
2. TODO

## Examples

### Component blacklist

The following expression matches on the [Component]'s [Package URL], using a regular expression in [RE2] syntax.
Additionally, it checks whether the [Component]'s version falls into a given [vers] range, consisting of multiple
constraints.

```js linenums="1"
component.purl.matches("^pkg:maven/com.acme/acme-lib\\b.*")
  && component.matches_range("vers:maven/>0|<1|!=0.2.4")
```

The expression will match:

* `pkg:maven/com.acme/acme-lib@0.1.0`
* `pkg:maven/com.acme/acme-lib@0.9.9`

but not:

* `pkg:maven/com.acme/acme-library@0.1.0`
* `pkg:maven/com.acme/acme-lib@0.2.4`

### License blacklist

The following expression matches [Component]s that are **not** internal to the organization,
and have either:

* No resolved [License] at all
* A resolved [License] that is not part of the `Permissive` license group

```js linenums="1"
!component.is_internal && (
  !has(component.resolved_license)
    || component.resolved_license.groups.exisits(licenseGroup, 
         licenseGroup.name == "Permissive")
)
```

### Vulnerability blacklist

The following expression matches [Component]s in [Project]s tagged as `3rd-party`, with at least one [Vulnerability]
being any of the given blacklisted IDs.

```js linenums="1"
"3rd-party" in project.tags
  && vulns.exists(vuln, vuln.id in [
       "CVE-2017-5638",  // struts RCE
       "CVE-2021-44228", // log4shell
       "CVE-2022-22965", // spring4shell
     ])
```

### Vulnerabilities with high severity in public facing projects

The following expression matches [Component]s in [Project]s tagged as `public-facing`, with at least one `HIGH`
or `CRITICAL`
[Vulnerability], where the [CVSSv3] attack vector is `Network`.

```js linenums="1"
"public-facing" in project.tags
  && vulns.exists(vuln,
    vuln.severity in ["HIGH", "CRITICAL"]
      && vuln.cvssv3_vector.matches(".*/AV:N/.*")
  )
```

## Reference

### Types

#### `Component`

| Field                | Type                   | Description     |
|:---------------------|:-----------------------|:----------------|
| `uuid`               | `string`               | Internal [UUID] |
| `group`              | `string`               |                 |
| `name`               | `string`               |                 |
| `version`            | `string`               |                 |
| `classifier`         | `string`               |                 |
| `cpe`                | `string`               | [CPE]           |
| `purl`               | `string`               | [Package URL]   |
| `swid_tag_id`        | `string`               |                 |
| `is_internal`        | `bool`                 |                 |
| `md5`                | `string`               |                 |
| `sha1`               | `string`               |                 |
| `sha256`             | `string`               |                 |
| `sha384`             | `string`               |                 |
| `sha512`             | `string`               |                 |
| `sha3_256`           | `string`               |                 |
| `sha3_384`           | `string`               |                 |
| `sha3_512`           | `string`               |                 |
| `blake2b_256`        | `string`               |                 |
| `blake2b_384`        | `string`               |                 |
| `blake2b_512`        | `string`               |                 |
| `blake3`             | `string`               |                 |
| `license_name`       | `string`               |                 |
| `license_expression` | `string`               |                 |
| `resolved_license`   | <code>[License]</code> |                 |

#### `License`

| Field              | Type                               | Description     |
|:-------------------|:-----------------------------------|:----------------|
| `uuid`             | `string`                           | Internal [UUID] |
| `id`               | `string`                           |                 |
| `name`             | `string`                           |                 |
| `groups`           | <code>list([License.Group])</code> |                 |
| `is_osi_approved`  | `bool`                             |                 |
| `is_fsf_libre`     | `bool`                             |                 |
| `is_deprecated_id` | `bool`                             |                 |
| `is_custom`        | `bool`                             |                 |

#### `License.Group`

| Field  | Type     | Description     |
|:-------|:---------|:----------------|
| `uuid` | `string` | Internal [UUID] |
| `name` | `string` |                 |

#### `Project`

| Field             | Type                                  | Description     |
|:------------------|:--------------------------------------|:----------------|
| `uuid`            | `string`                              | Internal [UUID] |
| `group`           | `string`                              |                 |
| `name`            | `string`                              |                 |
| `version`         | `string`                              |                 |
| `classifier`      | `string`                              |                 |
| `is_active`       | `bool`                                |                 |
| `tags`            | `list(string)`                        |                 |
| `properties`      | <code>list([Project.Property])</code> |                 |
| `cpe`             | `string`                              | [CPE]           |
| `purl`            | `string`                              | [Package URL]   |
| `swid_tag_id`     | `string`                              |                 |
| `last_bom_import` | `google.protobuf.Timestamp`           |                 |

#### `Project.Property`

| Field   | Type     | Description |
|:--------|:---------|:------------|
| `group` | `string` |             |
| `name`  | `string` |             |
| `value` | `string` |             |
| `type`  | `string` |             |

#### `Vulnerability`

| Field                             | Type                                     | Description                                |
|:----------------------------------|:-----------------------------------------|:-------------------------------------------|
| `uuid`                            | `string`                                 | Internal [UUID]                            |
| `id`                              | `string`                                 | ID of the vulnerability (e.g. `CVE-123`)   |
| `source`                          | `string`                                 | Authoritative source (e.g. `NVD`)          |
| `aliases`                         | <code>list([Vulnerability.Alias])</code> |                                            |
| `cwes`                            | `list(int)`                              | [CWE] IDs                                  |
| `created`                         | `google.protobuf.Timestamp`              | When the vulnerability was created         |
| `published`                       | `google.protobuf.Timestamp`              | When the vulnerability was published       |
| `updated`                         | `google.protobuf.Timestamp`              | Then the vulnerability was updated         |
| `severity`                        | `string`                                 |                                            |
| `cvssv2_base_score`               | `double`                                 | [CVSSv2] base score                        |
| `cvssv2_impact_subscore`          | `double`                                 | [CVSSv2] impact sub score                  |
| `cvssv2_exploitability_subscore`  | `double`                                 | [CVSSv2] exploitability sub score          |
| `cvssv2_vector`                   | `string`                                 | [CVSSv2] vector                            |
| `cvssv3_base_score`               | `double`                                 | [CVSSv3] base score                        |
| `cvssv3_impact_subscore`          | `double`                                 | [CVSSv3] impact sub score                  |
| `cvssv3_exploitability_subscore`  | `double`                                 | [CVSSv3] exploitability sub score          |
| `cvssv3_vector`                   | `string`                                 | [CVSSv3] vector                            |
| `owasp_rr_likelihood_score`       | `double`                                 | [OWASP Risk Rating] likelihood score       |
| `owasp_rr_technical_impact_score` | `double`                                 | [OWASP Risk Rating] technical impact score |
| `owasp_rr_business_impact_score`  | `double`                                 | [OWASP Risk Rating] business impact score  |
| `owasp_rr_vector`                 | `string`                                 | [OWASP Risk Rating] vector                 |
| `epss_score`                      | `double`                                 | [EPSS] score                               |
| `epss_percentile`                 | `double`                                 | [EPSS] percentile                          |

#### `Vulnerability.Alias`

| Field    | Type     | Description                               |
|:---------|:---------|:------------------------------------------|
| `id`     | `string` | ID of the vulnerability (e.g. `GHSA-123`) |
| `source` | `string` | Authoritative source (e.g. `GITHUB`)      |

### Function Definitions

In addition to the standard definitions of the CEL specification[^1], Dependency-Track offers additional functions
to unlock even more use cases:

| Symbol             | Type                                                                                        | Description                                                   |
|:-------------------|:--------------------------------------------------------------------------------------------|:--------------------------------------------------------------|
| `depends_on`       | <code>([Project], [Component])</code> -> `bool`                                             | Check if `Project` depends on `Component`                     |
| `is_dependency_of` | <code>([Component], [Component])</code> -> `bool`                                           | Check if a `Component` is a dependency of another `Component` |
| `matches_range`    | <code>([Project], string)</code> -> `bool`<br/><code>([Component], string)</code> -> `bool` | Check if a `Project` or `Component` matches a [vers] range    |

[C-style languages]: https://en.wikipedia.org/wiki/List_of_C-family_programming_languages
[CVSSv2]: https://www.first.org/cvss/v2/guide
[CVSSv3]: https://www.first.org/cvss/v3.0/specification-document
[CPE]: https://csrc.nist.gov/projects/security-content-automation-protocol/specifications/cpe
[CWE]: https://cwe.mitre.org/
[Common Expression Language]: https://github.com/google/cel-spec
[Component]: #component
[EPSS]: https://www.first.org/epss/
[License.Group]: #licensegroup
[License]: #license
[OWASP Risk Rating]: https://owasp.org/www-community/OWASP_Risk_Rating_Methodology
[Package URL]: https://github.com/package-url/purl-spec/blob/master/PURL-SPECIFICATION.rst
[Project.Property]: #projectproperty
[Project]: #project
[RE2]: https://github.com/google/re2/wiki/Syntax
[Turing-complete]: https://en.wikipedia.org/wiki/Turing_completeness
[UUID]: https://en.wikipedia.org/wiki/Universally_unique_identifier
[Vulnerability.Alias]: #vulnerabilityalias
[Vulnerability]: #vulnerability
[^1]: https://github.com/google/cel-spec/blob/master/doc/langdef.md#list-of-standard-definitions
[macros]: https://github.com/google/cel-spec/blob/v0.13.0/doc/langdef.md#macros
[vers]: https://github.com/package-url/purl-spec/blob/version-range-spec/VERSION-RANGE-SPEC.rst