| Status   | Date       | Author(s)                            |
|:---------|:-----------|:-------------------------------------|
| Proposed | 2025-04-16 | [@nscuro](https://github.com/nscuro) |

## Context

What is the issue that we're seeing that is motivating this decision or change?

### Data Model

```mermaid
erDiagram
    ldap_user {
        bigint id pk
        text dn
        text username
        text email
    }

    managed_user {
        bigint id pk
        text username
        text fullname
        text email
        timestamptz last_password_change
        bool non_expiry_password
        bool force_password_change
        bool suspended
    }

    oidc_user {
        bigint id pk
        text subject_identifier
        text username
        text email
    }

    permission {
        bigint id pk
        text name
    }

    team {
        bigint id pk
        text name
    }

    ldap_user }o--o{ permission: "has"
    managed_user }o--o{ permission: "has"
    oidc_user }o--o{ permission: "has"
    ldap_user }o--o{ team: "is member of"
    managed_user }o--o{ team: "is member of"
    oidc_user }o--o{ team: "is member of"
    team }o--o{ permission: "has"
```

### Drawbacks

* Uniqueness of usernames cannot be enforced across all user tables.
* Queries to determine permissions of a user are unnecessarily complex.

## Decision

What is the change that we're proposing and/or doing?

### Data Model

```mermaid
erDiagram
    permission {
        bigint id pk
        text name
    }

    team {
        bigint id pk
        text name
    }

    user {
        bigint id pk
        enum type
        text username
        text fullname
        text email
        text ldap_dn
        text oidc_subject_identifier
        text password
        timestamptz last_password_change
        bool non_expiry_password
        bool force_password_change
        bool suspended
    }

    user }o--o{ permission: "has"
    user }o--o{ team: "is member of"
    team }o--o{ permission: "has"
```

### Invariants

Not all fields make sense for all user types:

* LDAP and OIDC users don't have a password.
* Managed users have no LDAP DN or OIDC subject identifier.

Such invariants should be prevented at the database level, using `check` constraints. For example:

```sql
(type = 'managed' and password is not null and ldap_dn is null and oidc_subject_identifier is null)
or (type = 'ldap' and password is null and ldap_dn is not null and oidc_subject_identifier is null)
or (type = 'oidc' and password is null and ldap_dn is null and oidc_subject_identifier is not null)
```

## Consequences

* Existing user records will need to be migrated.
* Need to decide if we want to consolidate REST API endpoints or keep the current endpoints and responses
to avoid breaking changes.