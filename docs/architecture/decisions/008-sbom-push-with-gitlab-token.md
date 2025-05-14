| Status   | Date       | Author(s)                            |
|:---------|:-----------|:-------------------------------------|
| Proposed | 2025-05-14 | [@lmphil](https://github.com/lmphil) |

## Context

Adding GitLab integration to the Dependency Track/Hyades project is under consideration, with a key aspect being the use
 of GitLab job ID tokens (in JWT format) for authentication, in addition to the existing API key-based authentication.
 This new authentication method is needed to provide a more streamlined and secure experience for users who are already
 authenticated with GitLab, reducing the need for additional credentials and minimizing the administrative burden
 associated with managing multiple authentication tokens.

## Decision

Implement GitLab job ID token authentication for Dependency Track, allowing users to publish Software Bill of Materials
 (SBOMs) using a GitLab JWT token (in JWT format). The implementation will include the following key components:

* Authenticate users using a GitLab job ID token (in JWT format).
* Authorize actions based on the user's role in GitLab.
* Automatically create projects in Dependency Track if they do not exist.

## Consequences

The implementation of this feature will result in a more streamlined and secure experience for users, and will reduce or
 eliminate the need to manage multiple authentication tokens.
