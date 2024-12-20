## Messages




<a name="org-dependencytrack-policy-v1-Component"></a>

### Component




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` | UUID of the component. | - |
| `group` | `string` | Group / namespace of the component. | - |
| `name` | `string` | Name of the component. | - |
| `version` | `string` | Version of the component. | - |
| `classifier` | `string` | Classifier / type of the component. May be any of: - APPLICATION - CONTAINER - DEVICE - FILE - FIRMWARE - FRAMEWORK - LIBRARY - OPERATING_SYSTEM | - |
| `cpe` | `string` | CPE of the component. https://csrc.nist.gov/projects/security-content-automation-protocol/specifications/cpe | - |
| `purl` | `string` | Package URL of the component. https://github.com/package-url/purl-spec | - |
| `swid_tag_id` | `string` | SWID tag ID of the component. https://csrc.nist.gov/projects/Software-Identification-SWID | - |
| `is_internal` | `bool` | Whether the component is internal to the organization. | - |
| `md5` | `string` |  | - |
| `sha1` | `string` |  | - |
| `sha256` | `string` |  | - |
| `sha384` | `string` |  | - |
| `sha512` | `string` |  | - |
| `sha3_256` | `string` |  | - |
| `sha3_384` | `string` |  | - |
| `sha3_512` | `string` |  | - |
| `blake2b_256` | `string` |  | - |
| `blake2b_384` | `string` |  | - |
| `blake2b_512` | `string` |  | - |
| `blake3` | `string` |  | - |
| `license_name` | `string` |  | - |
| `license_expression` | `string` |  | - |
| `resolved_license` | [`License`](#org-dependencytrack-policy-v1-License) |  | - |
| `published_at` | `google.protobuf.Timestamp` | When the component current version last modified. | - |
| `latest_version` | `string` |  | - |





<a name="org-dependencytrack-policy-v1-License"></a>

### License




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `id` | `string` |  | - |
| `name` | `string` |  | - |
| `groups` | [`License.Group[]`](#org-dependencytrack-policy-v1-License-Group) |  | - |
| `is_osi_approved` | `bool` |  | - |
| `is_fsf_libre` | `bool` |  | - |
| `is_deprecated_id` | `bool` |  | - |
| `is_custom` | `bool` |  | - |





<a name="org-dependencytrack-policy-v1-License-Group"></a>

### License.Group




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `name` | `string` |  | - |





<a name="org-dependencytrack-policy-v1-Project"></a>

### Project




| Field             | Type | Description |
|:------------------| :--- | :---------- |
| `uuid`            | `string` |  | - |
| `group`           | `string` |  | - |
| `name`            | `string` |  | - |
| `version`         | `string` |  | - |
| `classifier`      | `string` |  | - |
| `inactive_since`  | `google.protobuf.Timestamp` |  | - |
| `tags`            | `string[]` |  | - |
| `properties`      | [`Project.Property[]`](#org-dependencytrack-policy-v1-Project-Property) |  | - |
| `cpe`             | `string` |  | - |
| `purl`            | `string` |  | - |
| `swid_tag_id`     | `string` |  | - |
| `last_bom_import` | `google.protobuf.Timestamp` |  | - |
| `metadata`        | [`Project.Metadata`](#org-dependencytrack-policy-v1-Project-Metadata) |  | - |





<a name="org-dependencytrack-policy-v1-Project-Metadata"></a>

### Project.Metadata




| Field | Type | Description |
| :---- | :--- | :---------- |
| `tools` | [`Tools`](#org-dependencytrack-policy-v1-Tools) |  | - |
| `bom_generated` | `google.protobuf.Timestamp` |  | - |





<a name="org-dependencytrack-policy-v1-Project-Property"></a>

### Project.Property




| Field | Type | Description |
| :---- | :--- | :---------- |
| `group` | `string` |  | - |
| `name` | `string` |  | - |
| `value` | `string` |  | - |
| `type` | `string` |  | - |





<a name="org-dependencytrack-policy-v1-Tools"></a>

### Tools




| Field | Type | Description |
| :---- | :--- | :---------- |
| `components` | [`Component[]`](#org-dependencytrack-policy-v1-Component) | Components used as tools. | - |





<a name="org-dependencytrack-policy-v1-VersionDistance"></a>

### VersionDistance




| Field | Type | Description |
| :---- | :--- | :---------- |
| `epoch` | `string` |  | - |
| `major` | `string` |  | - |
| `minor` | `string` |  | - |
| `patch` | `string` |  | - |





<a name="org-dependencytrack-policy-v1-Vulnerability"></a>

### Vulnerability




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `id` | `string` |  | - |
| `source` | `string` |  | - |
| `aliases` | [`Vulnerability.Alias[]`](#org-dependencytrack-policy-v1-Vulnerability-Alias) |  | - |
| `cwes` | `int32[]` |  | - |
| `created` | `google.protobuf.Timestamp` |  | - |
| `published` | `google.protobuf.Timestamp` |  | - |
| `updated` | `google.protobuf.Timestamp` |  | - |
| `severity` | `string` |  | - |
| `cvssv2_base_score` | `double` |  | - |
| `cvssv2_impact_subscore` | `double` |  | - |
| `cvssv2_exploitability_subscore` | `double` |  | - |
| `cvssv2_vector` | `string` |  | - |
| `cvssv3_base_score` | `double` |  | - |
| `cvssv3_impact_subscore` | `double` |  | - |
| `cvssv3_exploitability_subscore` | `double` |  | - |
| `cvssv3_vector` | `string` |  | - |
| `owasp_rr_likelihood_score` | `double` |  | - |
| `owasp_rr_technical_impact_score` | `double` |  | - |
| `owasp_rr_business_impact_score` | `double` |  | - |
| `owasp_rr_vector` | `string` |  | - |
| `epss_score` | `double` |  | - |
| `epss_percentile` | `double` |  | - |





<a name="org-dependencytrack-policy-v1-Vulnerability-Alias"></a>

### Vulnerability.Alias




| Field | Type | Description |
| :---- | :--- | :---------- |
| `id` | `string` |  | - |
| `source` | `string` |  | - |








