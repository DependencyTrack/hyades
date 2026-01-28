## Introduction

Dependency-Track includes a robust and configurable notification framework,
capable of alerting users or systems about the occurrences of various events
in the platform.

## Concepts

### Alerts

Alerts, a.k.a. *notification rules*, are configurations that specify

### Publishers

Publishers are software components that send notifications emitted by the platform
to a destination system. Dependency-Track supports multiple publishers, ranging from
email to Webhook. Refer to [Publishers](publishers.md) for details.

### Templates

Templates define how the platform-internal representation of notifications
(see [Notification Schema](../../reference/schemas/notification.md)) is transformed
to match the expectation of notification recipients.

While each [publisher](#publishers) ships with a default template, administrators
can also configure custom templates. Refer to [Templating](templating.md) for details.

### Levels

Notifications can have one of three possible levels:

* Informational
* Warning
* Error

These levels behave similar to logging levels, in that they allow [alerts](#alerts)
to define the verbosity of notifications being sent:

* Configuring an alert for level *Informational* will match notifications of level *Informational*, *Warning*, and *Error*.
* Configuring an alert for level *Warning* will match notifications of level *Warning* and *Error*.
* Configuring an alert for level *Error* will only match notifications of level *Error*.

### Scopes

Notifications are emitted for different *scopes*. A scope broadly categorises the *subject*
of a notification.

* **SYSTEM**: Informs about system-level events, such as users being created, or integrations failing.
* **PORTFOLIO**: Informs about portfolio-level events, such as BOM uploads, or newly identified vulnerabilities.

### Groups

A group is a granular classification of notification subjects within a [scope](#scopes).

| Scope     | Group                         | Level(s)      | Description                                                                                                                       |
|-----------|-------------------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------------|
| SYSTEM    | ANALYZER                      | (Any)         | Notifications generated as a result of interacting with an external source of vulnerability intelligence                          |
| SYSTEM    | DATASOURCE_MIRRORING          | (Any)         | Notifications generated when performing mirroring of one of the supported datasources such as the NVD                             |
| SYSTEM    | FILE_SYSTEM                   | (Any)         | Notifications generated as a result of a file system operation. These are typically only generated on error conditions            |
| SYSTEM    | REPOSITORY                    | (Any)         | Notifications generated as a result of interacting with one of the supported repositories such as Maven Central, RubyGems, or NPM |
| SYSTEM    | USER_CREATED                  | Informational | Notifications generated as a result of a user creation                                                                            |
| SYSTEM    | USER_DELETED                  | Informational | Notifications generated as a result of a user deletion                                                                            |
| PORTFOLIO | NEW_VULNERABILITY             | Informational | Notifications generated whenever a new vulnerability is identified                                                                |
| PORTFOLIO | NEW_VULNERABLE_DEPENDENCY     | Informational | Notifications generated as a result of a vulnerable component becoming a dependency of a project                                  |
| PORTFOLIO | GLOBAL_AUDIT_CHANGE           | Informational | Notifications generated whenever an analysis or suppression state has changed on a finding from a component (global)              |
| PORTFOLIO | PROJECT_AUDIT_CHANGE          | Informational | Notifications generated whenever an analysis or suppression state has changed on a finding from a project                         |
| PORTFOLIO | BOM_CONSUMED                  | Informational | Notifications generated whenever a supported BOM is ingested and identified                                                       |
| PORTFOLIO | BOM_PROCESSED                 | Informational | Notifications generated after a supported BOM is ingested, identified, and successfully processed                                 |
| PORTFOLIO | BOM_PROCESSING_FAILED         | Error         | Notifications generated whenever a BOM upload process fails                                                                       |
| PORTFOLIO | BOM_VALIDATION_FAILED         | Error         | Notifications generated whenever an invalid BOM is uploaded                                                                       |
| PORTFOLIO | POLICY_VIOLATION              | Informational | Notifications generated whenever a policy violation is identified                                                                 |
