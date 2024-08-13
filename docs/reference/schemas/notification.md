





















<a name="org-dependencytrack-notification-v1-Notification"></a>

## Notification




| Field | Type | Description |
| :---- | :--- | :---------- |
| `level` | [`Level`](#org-dependencytrack-notification-v1-Level) |  | - |
| `scope` | [`Scope`](#org-dependencytrack-notification-v1-Scope) |  | - |
| `group` | [`Group`](#org-dependencytrack-notification-v1-Group) |  | - |
| `title` | `string` |  | - |
| `content` | `string` |  | - |
| `timestamp` | `google.protobuf.Timestamp` |  | - |
| `subject` | `google.protobuf.Any` |  | - |





































## Subjects









<a name="org-dependencytrack-notification-v1-BomConsumedOrProcessedSubject"></a>

### BomConsumedOrProcessedSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `bom` | [`Bom`](#org-dependencytrack-notification-v1-Bom) |  | - |
| `token` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-BomProcessingFailedSubject"></a>

### BomProcessingFailedSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `bom` | [`Bom`](#org-dependencytrack-notification-v1-Bom) |  | - |
| `cause` | `string` |  | - |
| `token` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-BomValidationFailedSubject"></a>

### BomValidationFailedSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `bom` | [`Bom`](#org-dependencytrack-notification-v1-Bom) |  | - |
| `errors` | `string[]` |  | - |









<a name="org-dependencytrack-notification-v1-ComponentVulnAnalysisCompleteSubject"></a>

### ComponentVulnAnalysisCompleteSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `vulnerabilities` | [`Vulnerability[]`](#org-dependencytrack-notification-v1-Vulnerability) |  | - |







<a name="org-dependencytrack-notification-v1-NewVulnerabilitySubject"></a>

### NewVulnerabilitySubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `vulnerability` | [`Vulnerability`](#org-dependencytrack-notification-v1-Vulnerability) |  | - |
| `affected_projects_reference` | [`BackReference`](#org-dependencytrack-notification-v1-BackReference) |  | - |
| `vulnerability_analysis_level` | `string` |  | - |
| `affected_projects` | [`Project[]`](#org-dependencytrack-notification-v1-Project) | List of projects affected by the vulnerability. DEPRECATED: This list only holds one item, and it is identical to the one in the project field. The field is kept for backward compatibility of JSON notifications, but consumers should not expect multiple projects here. Transmitting all affected projects in one notification is not feasible for large portfolios, see https://github.com/DependencyTrack/hyades/issues/467 for details. | - |







<a name="org-dependencytrack-notification-v1-NewVulnerableDependencySubject"></a>

### NewVulnerableDependencySubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `vulnerabilities` | [`Vulnerability[]`](#org-dependencytrack-notification-v1-Vulnerability) |  | - |

















<a name="org-dependencytrack-notification-v1-PolicyViolationAnalysisDecisionChangeSubject"></a>

### PolicyViolationAnalysisDecisionChangeSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `policy_violation` | [`PolicyViolation`](#org-dependencytrack-notification-v1-PolicyViolation) |  | - |
| `analysis` | [`PolicyViolationAnalysis`](#org-dependencytrack-notification-v1-PolicyViolationAnalysis) |  | - |







<a name="org-dependencytrack-notification-v1-PolicyViolationSubject"></a>

### PolicyViolationSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `policy_violation` | [`PolicyViolation`](#org-dependencytrack-notification-v1-PolicyViolation) |  | - |









<a name="org-dependencytrack-notification-v1-ProjectVulnAnalysisCompleteSubject"></a>

### ProjectVulnAnalysisCompleteSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `findings` | [`ComponentVulnAnalysisCompleteSubject[]`](#org-dependencytrack-notification-v1-ComponentVulnAnalysisCompleteSubject) |  | - |
| `status` | [`ProjectVulnAnalysisStatus`](#org-dependencytrack-notification-v1-ProjectVulnAnalysisStatus) |  | - |
| `token` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-UserSubject"></a>

### UserSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `username` | `string` |  | - |
| `email` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-VexConsumedOrProcessedSubject"></a>

### VexConsumedOrProcessedSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `vex` | `bytes` |  | - |
| `format` | `string` |  | - |
| `spec_version` | `string` |  | - |















<a name="org-dependencytrack-notification-v1-VulnerabilityAnalysisDecisionChangeSubject"></a>

### VulnerabilityAnalysisDecisionChangeSubject




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `vulnerability` | [`Vulnerability`](#org-dependencytrack-notification-v1-Vulnerability) |  | - |
| `analysis` | [`VulnerabilityAnalysis`](#org-dependencytrack-notification-v1-VulnerabilityAnalysis) |  | - |







## Messages





<a name="org-dependencytrack-notification-v1-BackReference"></a>

### BackReference




| Field | Type | Description |
| :---- | :--- | :---------- |
| `api_uri` | `string` | URI to the API endpoint from which additional information can be fetched. | - |
| `frontend_uri` | `string` | URI to the frontend where additional information can be seen. | - |







<a name="org-dependencytrack-notification-v1-Bom"></a>

### Bom




| Field | Type | Description |
| :---- | :--- | :---------- |
| `content` | `string` |  | - |
| `format` | `string` |  | - |
| `spec_version` | `string` |  | - |













<a name="org-dependencytrack-notification-v1-Component"></a>

### Component




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `group` | `string` |  | - |
| `name` | `string` |  | - |
| `version` | `string` |  | - |
| `purl` | `string` |  | - |
| `md5` | `string` |  | - |
| `sha1` | `string` |  | - |
| `sha256` | `string` |  | - |
| `sha512` | `string` |  | - |















<a name="org-dependencytrack-notification-v1-Policy"></a>

### Policy




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `name` | `string` |  | - |
| `violation_state` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-PolicyCondition"></a>

### PolicyCondition




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `subject` | `string` |  | - |
| `operator` | `string` |  | - |
| `value` | `string` |  | - |
| `policy` | [`Policy`](#org-dependencytrack-notification-v1-Policy) |  | - |







<a name="org-dependencytrack-notification-v1-PolicyViolation"></a>

### PolicyViolation




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `type` | `string` |  | - |
| `timestamp` | `google.protobuf.Timestamp` |  | - |
| `condition` | [`PolicyCondition`](#org-dependencytrack-notification-v1-PolicyCondition) |  | - |







<a name="org-dependencytrack-notification-v1-PolicyViolationAnalysis"></a>

### PolicyViolationAnalysis




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `policy_violation` | [`PolicyViolation`](#org-dependencytrack-notification-v1-PolicyViolation) |  | - |
| `state` | `string` |  | - |
| `suppressed` | `bool` |  | - |











<a name="org-dependencytrack-notification-v1-Project"></a>

### Project




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `name` | `string` |  | - |
| `version` | `string` |  | - |
| `description` | `string` |  | - |
| `purl` | `string` |  | - |
| `tags` | `string[]` |  | - |













<a name="org-dependencytrack-notification-v1-Vulnerability"></a>

### Vulnerability




| Field | Type | Description |
| :---- | :--- | :---------- |
| `uuid` | `string` |  | - |
| `vuln_id` | `string` |  | - |
| `source` | `string` |  | - |
| `aliases` | [`Vulnerability.Alias[]`](#org-dependencytrack-notification-v1-Vulnerability-Alias) |  | - |
| `title` | `string` |  | - |
| `sub_title` | `string` |  | - |
| `description` | `string` |  | - |
| `recommendation` | `string` |  | - |
| `cvss_v2` | `double` |  | - |
| `cvss_v3` | `double` |  | - |
| `owasp_rr_likelihood` | `double` |  | - |
| `owasp_rr_technical_impact` | `double` |  | - |
| `owasp_rr_business_impact` | `double` |  | - |
| `severity` | `string` |  | - |
| `cwes` | [`Vulnerability.Cwe[]`](#org-dependencytrack-notification-v1-Vulnerability-Cwe) |  | - |
| `cvss_v2_vector` | `string` |  | - |
| `cvss_v3_vector` | `string` |  | - |
| `owasp_rr_vector` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-Vulnerability-Alias"></a>

### Vulnerability.Alias




| Field | Type | Description |
| :---- | :--- | :---------- |
| `id` | `string` |  | - |
| `source` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-Vulnerability-Cwe"></a>

### Vulnerability.Cwe




| Field | Type | Description |
| :---- | :--- | :---------- |
| `cwe_id` | `int32` |  | - |
| `name` | `string` |  | - |







<a name="org-dependencytrack-notification-v1-VulnerabilityAnalysis"></a>

### VulnerabilityAnalysis




| Field | Type | Description |
| :---- | :--- | :---------- |
| `component` | [`Component`](#org-dependencytrack-notification-v1-Component) |  | - |
| `project` | [`Project`](#org-dependencytrack-notification-v1-Project) |  | - |
| `vulnerability` | [`Vulnerability`](#org-dependencytrack-notification-v1-Vulnerability) |  | - |
| `state` | `string` |  | - |
| `suppressed` | `bool` |  | - |









## Enums




<a name="org-dependencytrack-notification-v1-Group"></a>

### Group



| Name | Description |
| :--- | :---------- |
| `GROUP_UNSPECIFIED` |  |
| `GROUP_CONFIGURATION` |  |
| `GROUP_DATASOURCE_MIRRORING` |  |
| `GROUP_REPOSITORY` |  |
| `GROUP_INTEGRATION` |  |
| `GROUP_FILE_SYSTEM` |  |
| `GROUP_ANALYZER` |  |
| `GROUP_NEW_VULNERABILITY` |  |
| `GROUP_NEW_VULNERABLE_DEPENDENCY` |  |
| `GROUP_PROJECT_AUDIT_CHANGE` |  |
| `GROUP_BOM_CONSUMED` |  |
| `GROUP_BOM_PROCESSED` |  |
| `GROUP_VEX_CONSUMED` |  |
| `GROUP_VEX_PROCESSED` |  |
| `GROUP_POLICY_VIOLATION` |  |
| `GROUP_PROJECT_CREATED` |  |
| `GROUP_BOM_PROCESSING_FAILED` |  |
| `GROUP_PROJECT_VULN_ANALYSIS_COMPLETE` |  |
| `GROUP_USER_CREATED` |  |
| `GROUP_USER_DELETED` |  |
| `GROUP_BOM_VALIDATION_FAILED` |  |




<a name="org-dependencytrack-notification-v1-Level"></a>

### Level



| Name | Description |
| :--- | :---------- |
| `LEVEL_UNSPECIFIED` |  |
| `LEVEL_INFORMATIONAL` |  |
| `LEVEL_WARNING` |  |
| `LEVEL_ERROR` |  |




<a name="org-dependencytrack-notification-v1-ProjectVulnAnalysisStatus"></a>

### ProjectVulnAnalysisStatus



| Name | Description |
| :--- | :---------- |
| `PROJECT_VULN_ANALYSIS_STATUS_UNSPECIFIED` |  |
| `PROJECT_VULN_ANALYSIS_STATUS_FAILED` |  |
| `PROJECT_VULN_ANALYSIS_STATUS_COMPLETED` |  |




<a name="org-dependencytrack-notification-v1-Scope"></a>

### Scope



| Name | Description |
| :--- | :---------- |
| `SCOPE_UNSPECIFIED` |  |
| `SCOPE_PORTFOLIO` |  |
| `SCOPE_SYSTEM` |  |



