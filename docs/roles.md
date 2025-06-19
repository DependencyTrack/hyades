# Roles 
Roles increase privacy and allow for better ease of use in large scale projects. Roles are synced to the user's Gitlab account with their LM SSO and they are automatically given access to the necessary projects. Custom roles are also included, allowing for the user to create unique permissions for specific use case.

# Scoped Permissions

With the addition of `Roles`, we had to normalize the permssion model for more fine-grained access control. The current model was split into a global, shared, and teams/project based permission set. 
This is done by seperating the current `NAME` text value into the distinct concepts of `RESOURCE` and `ACCESS_LEVEL`. The enforcement is implemented through a combination of annotations, query logic, and request filtering—particularly in the context of REST API endpoints that deal with projects and their related resources.

Current `PERMISSION TABLE`:
```mermaid
---
title: Current Model
---
erDiagram
    PERMISSION {
        BIGINT ID PK
        TEXT NAME
        TEXT DESCRIPTION
    }
```
---
Implemented `PERMISSION TABLE`:
```mermaid
---
title: Implemented Model
---
erDiagram
    PERMISSION {
        BIGINT ID PK
        TEXT RESOURCE
        TEXT ACCESS_LEVEL
        TEXT DESCRIPTION
    }
```

# User, Role, Permission Relationship Diagram 
```mermaid
erDiagram
    %% Table Definitions
    ROLE {
        bigint ID PK
        text NAME
        uuid UUID
    }
    USER {
        bigint ID PK
    }
    PERMISSION {
        bigint ID PK
        text RESORUCE
        text ACCESS_LEVEL
        text DESCRIPTION
    }
    ROLES_PERMISSIONS {
        bigint ROLE_ID FK "References ROLE(ID)"
        bigint PERMISSION_ID FK "References PERMISSION(ID)"
    }
    USERS_ROLES {
        bigint USER_ID FK "References USER(ID)"
        bigint ROLE_ID FK "References ROLE(ID)"
    }
    %% Relationships for USERS_ROLES: This table associates a USER and a ROLE.
    USER ||--o{ USERS_ROLES : "assigned"
    ROLE ||--o{ USERS_ROLES : "applied to"
    %% Relationships between ROLE and PERMISSION via ROLES_PERMISSIONS
    ROLE ||--o{ ROLES_PERMISSIONS : "has"
    PERMISSION ||--o{ ROLES_PERMISSIONS : "assigned via"
```
---

# How to Use Them?

**Creating a New Role**

As a user with `ADMIN` access: 
- Navigate to the `Administration > Access Management > Roles` page. There you will see all four default Roles, with accompanied permissions.
- Click the `+ Create Role` button.
- Provide a `Role Name` and add `Permissions` 
- click `Create`.

**Assigning a Role, Project, and Permissions to a User**
- Navigate to any of the three user pages: `LDAPUsers`, `Managed Users`, or `OpenID Connect Users`.
- Click `+ Create User` button.
- Complete the fields in the **Create User Modal** and click `Create`.
- Click `+` in the `Roles` modal.
- Select your desired `Project` and `Role`. 
- Click `Assign`.
- Click `+` button in the `Permissions` modal select desired permissions.
- Click `Select`.





