## Introduction

Dependency-Track allows policy conditions to be defined using the [Common Expression Language] (CEL),
enabling more flexibility, and more control over predefined conditions.

## Evaluation Context

Conditions are scoped to individual components. Each condition is evaluated for every single component in a project.
The context in which expressions are evaluated in contains the following variables:

| Variable    | Type                                                    | Description                                  |
|:------------|:--------------------------------------------------------|:---------------------------------------------|
| `component` | <code>[org.hyades.policy.v1.Component]</code>           | The component being evaluated                |
| `project`   | <code>[org.hyades.policy.v1.Project]</code>             | The project the component is part of         |
| `vulns`     | <code>list([org.hyades.policy.v1.Vulnerability])</code> | Vulnerabilities the component is affected by |

## Examples

### Component blacklist

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

### Vulnerability blacklist

Matches components in projects tagged as `3rd-party`, with at least one vulnerability
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

Matches components in public facing projects, with at least one `HIGH` or `CRITICAL`
vulnerability, where the [CVSSv3] attack vector is `Network`.

```js linenums="1"
"public-facing" in project.tags
  && vulns.exists(vuln, 
       vuln.severity in ["HIGH", "CRITICAL"] 
       && vuln.cvssv3_vector.matches(".*/AV:N/.*")
     )
```

## Reference

### Types

#### `org.hyades.policy.v1.Component`

| Field                | Type                                        | Description |
|:---------------------|:--------------------------------------------|:------------|
| `uuid`               | `string`                                    |             |
| `group`              | `string`                                    |             |
| `name`               | `string`                                    |             |
| `version`            | `string`                                    |             |
| `classifier`         | `string`                                    |             |
| `cpe`                | `string`                                    |             |
| `purl`               | `string`                                    |             |
| `swid_tag_id`        | `string`                                    |             |
| `is_internal`        | `bool`                                      |             |
| `md5`                | `string`                                    |             |
| `sha1`               | `string`                                    |             |
| `sha256`             | `string`                                    |             |
| `sha384`             | `string`                                    |             |
| `sha512`             | `string`                                    |             |
| `sha3_256`           | `string`                                    |             |
| `sha3_384`           | `string`                                    |             |
| `sha3_512`           | `string`                                    |             |
| `blake2b_256`        | `string`                                    |             |
| `blake2b_384`        | `string`                                    |             |
| `blake2b_512`        | `string`                                    |             |
| `blake3`             | `string`                                    |             |
| `license_name`       | `string`                                    |             |
| `license_expression` | `string`                                    |             |
| `resolved_license`   | <code>[org.hyades.policy.v1.License]</code> |             |

#### `org.hyades.policy.v1.License`

| Field              | Type                                                    | Description |
|:-------------------|:--------------------------------------------------------|:------------|
| `uuid`             | `string`                                                |             |
| `id`               | `string`                                                |             |
| `name`             | `string`                                                |             |
| `groups`           | <code>list([org.hyades.policy.v1.License.Group])</code> |             |
| `is_osi_approved`  | `bool`                                                  |             |
| `is_fsf_libre`     | `bool`                                                  |             |
| `is_deprecated_id` | `bool`                                                  |             |
| `is_custom`        | `bool`                                                  |             |

#### `org.hyades.policy.v1.License.Group`

| Field  | Type     | Description |
|:-------|:---------|:------------|
| `uuid` | `string` |             |
| `name` | `string` |             |

#### `org.hyades.policy.v1.Project`

| Field             | Type                                                       | Description |
|:------------------|:-----------------------------------------------------------|:------------|
| `uuid`            | `string`                                                   |             |
| `group`           | `string`                                                   |             |
| `name`            | `string`                                                   |             |
| `version`         | `string`                                                   |             |
| `classifier`      | `string`                                                   |             |
| `is_active`       | `bool`                                                     |             |
| `tags`            | `list(string)`                                             |             |
| `properties`      | <code>list([org.hyades.policy.v1.Project.Property])</code> |             |
| `cpe`             | `string`                                                   |             |
| `purl`            | `string`                                                   |             |
| `swid_tag_id`     | `string`                                                   |             |
| `last_bom_import` | `google.protobuf.Timestamp`                                |             |

#### `org.hyades.policy.v1.Project.Property`

| Field   | Type     | Description |
|:--------|:---------|:------------|
| `group` | `string` |             |
| `name`  | `string` |             |
| `value` | `string` |             |
| `type`  | `string` |             |

#### `org.hyades.policy.v1.Vulnerability`

| Field                             | Type                        | Description                                |
|:----------------------------------|:----------------------------|:-------------------------------------------|
| `uuid`                            | `string`                    |                                            |
| `id`                              | `string`                    | ID of the vulnerability (e.g. `CVE-123`)   |
| `source`                          | `string`                    | Authoritative source (e.g. `NVD`)          |
| `aliases`                         | `string`                    |                                            |
| `cwes`                            | `list(int)`                 | [CWE] IDs                                  |
| `created`                         | `google.protobuf.Timestamp` | When the vulnerability was created         |
| `published`                       | `google.protobuf.Timestamp` | When the vulnerability was published       |
| `updated`                         | `google.protobuf.Timestamp` | Then the vulnerability was updated         |
| `severity`                        | `string`                    |                                            |
| `cvssv2_base_score`               | `double`                    | [CVSSv2] base score                        |
| `cvssv2_impact_subscore`          | `double`                    | [CVSSv2] impact sub score                  |
| `cvssv2_exploitability_subscore`  | `double`                    | [CVSSv2] exploitability sub score          |
| `cvssv2_vector`                   | `string`                    | [CVSSv2] vector                            |
| `cvssv3_base_score`               | `double`                    | [CVSSv3] base score                        |
| `cvssv3_impact_subscore`          | `double`                    | [CVSSv3] impact sub score                  |
| `cvssv3_exploitability_subscore`  | `double`                    | [CVSSv3] exploitability sub score          |
| `cvssv3_vector`                   | `string`                    | [CVSSv3] vector                            |
| `owasp_rr_likelihood_score`       | `double`                    | [OWASP Risk Rating] likelihood score       |
| `owasp_rr_technical_impact_score` | `double`                    | [OWASP Risk Rating] technical impact score |
| `owasp_rr_business_impact_score`  | `double`                    | [OWASP Risk Rating] business impact score  |
| `owasp_rr_vector`                 | `string`                    | [OWASP Risk Rating] vector                 |
| `epss_score`                      | `double`                    | [EPSS] score                               |
| `epss_percentile`                 | `double`                    | [EPSS] percentile                          |

#### `org.hyades.policy.v1.Vulnerability.Alias`

| Field    | Type     | Description |
|:---------|:---------|:------------|
| `id`     | `string` |             |
| `source` | `string` |             |

### Function Definitions

https://github.com/google/cel-spec/blob/master/doc/langdef.md#list-of-standard-definitions

| Symbol             | Type                                                                                             | Description                                                   |
|:-------------------|:-------------------------------------------------------------------------------------------------|:--------------------------------------------------------------|
| `depends_on`       | <[Project]>.depends_on([Component]) -> `bool`                                                    | Check if `Project` depends on `Component`                     |
| `is_dependency_of` | (`Component`, `Component`) -> `bool`                                                             | Check if a `Component` is a dependency of another `Component` |
| `matches_range`    | (`Project`, `string`) -> `bool`<br/>(`Component`, `string`) -> `bool`                            |                                                               |
| `charAt`           | (`string`, `int`) -> `string`                                                                    |                                                               |
| `indexOf`          | (`string`, `string`) -> `int`<br/>(`string`, `string`, `int`) -> `int`                           |                                                               |
| `join`             | (`list(string)`) -> `string`<br/>(`list(string)`, `string`) -> `string`                          |                                                               |
| `lastIndexOf`      | (`string`, `string`) -> `int`<br/>(`string`, `string`, `int`) -> `string`                        |                                                               |
| `toLowerAscii`     | (`string`) -> `string`                                                                           |                                                               |
| `replace`          | (`string`, `string`, `string`) -> `string`<br/>(`string`, `string`, `string`, `int`) -> `string` |                                                               |
| `split`            | `<string>.split(<string>) -> <string>`                                                           |                                                               |


[CVSSv2]: https://www.first.org/cvss/v2/guide
[CVSSv3]: https://www.first.org/cvss/v3.0/specification-document
[CWE]: https://cwe.mitre.org/
[Common Expression Language]: https://github.com/google/cel-spec
[Component]: #orghyadespolicyv1component
[EPSS]: https://www.first.org/epss/
[OWASP Risk Rating]: https://owasp.org/www-community/OWASP_Risk_Rating_Methodology
[Project]: #orghyadespolicyv1project
[org.hyades.policy.v1.Component]: #orghyadespolicyv1component
[org.hyades.policy.v1.License.Group]: #orghyadespolicyv1licensegroup
[org.hyades.policy.v1.License]: #orghyadespolicyv1license
[org.hyades.policy.v1.Project.Property]: #orghyadespolicyv1projectproperty
[org.hyades.policy.v1.Project]: #orghyadespolicyv1project
[org.hyades.policy.v1.Vulnerability]: #orghyadespolicyv1vulnerability
[vers]: https://github.com/package-url/purl-spec/blob/version-range-spec/VERSION-RANGE-SPEC.rst