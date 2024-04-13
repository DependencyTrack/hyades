## Introduction

Component policies allow you to flag individual components for certain types of risk,
based on a variety of criteria.

For example, they allow you to highlight components with an undesirable license (e.g. `AGPL-3.0`),
certain high-profile vulnerabilities (e.g. `CVE-2021-44228`), a specific CWE you deem unacceptable,
or simply based on their PURL.

Component policies are a great way of projecting your organization's risk awareness to your portfolio
of projects, and the components they use.

!!! note
    Authoring component policies requires the `POLICY_MANAGEMENT` permission.

## How it Works

A component policy consists of the following building blocks:

* An operator
* A violation state
* One or more conditions

As the name implies, *conditions* represent the criteria you want to match on. We provide a handful of pre-defined
conditions for your convenience, but you're more than welcome to leverage the full power of [expressions](./expressions.md)!

The *operator* determines how multiple conditions are chained together. The operator `All` behaves like a
logical **and** (`&&`), whereas `Any` behaves like a logical **or** (`||`).

The *violation state* controls the severity of a policy violation. You have the choice between `INFO`, `WARN`, and `FAIL`.
There is no functionality tied to the violation state, it merely exists to help you prioritize violations according
to their importance.

## Evaluation

Component policies are evaluated in the following scenarios:

* After vulnerability analysis completion
    * As part of a BOM upload
    * When triggered via *Reanalyze* button in the UI
* After manually adding components to a project via UI or REST API
* After manually updating project or component details via UI or REST API

## Conditions

### Risk Types

Each condition has an associated type of risk it checks for. Available risk types are:

* License
* Operational
* Security

### Builtins

#### Component Age

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### Component Hash

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### Coordinates

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### CPE

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### CWE

* **Subject**: Vulnerability
* **Risk Type**: Operational

TBD

#### License

* **Subject**: Component
* **Risk Type**: License

TBD

#### License Group

* **Subject**: Component
* **Risk Type**: License

TBD

#### Package URL

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### Severity

* **Subject**: Vulnerability
* **Risk Type**: Security

TBD

#### SWID Tag ID

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### Version

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### Version Distance

* **Subject**: Component
* **Risk Type**: Operational

TBD

#### Vulnerability ID

* **Subject**: Vulnerability
* **Risk Type**: Security

TBD

### Expressions

TBD
