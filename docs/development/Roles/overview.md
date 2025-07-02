# Roles

## What are Roles?

Roles increase privacy and allow for better ease of use in large scale projects. Essentially, a role defines a set of permissions and responsibilities that a user or group of users can have within the project. By assigning specific roles, project administrators can control who has access to what resources, ensuring that sensitive information is only accessible to those who need it. Custom roles are also included, allowing for the user to create unique permissions for specific use case.

## Scoped Permissions

With the addition of `Roles`, a normalization of the permission model was done. The current model was split into a global, shared, and teams/project based permission set. For an example of how to utilize them, refer to `How To Use Them` section.

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

## User, Role, Permission Relationship Diagram

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

## How to Use Them?

## Creating a New Role

As a user with `ADMIN` access:

- Navigate to the `Administration > Access Management > Roles` page.
  ![Role](images/roles_page_view.png)
- Click the `+ Create Role` button.
  ![Create Role](images/create_role.png)
- Provide a `Role Name` and add `Permissions`
- click `Create`.

## Assigning a Role, Project, and Permissions to a User

- Navigate to any of the three user pages: `LDAPUsers`, `Managed Users`, or `OpenID Connect Users`.
  ![Managed User](images/managed_users_page_view.png)
- Click `+ Create User` button.
- Complete the fields in the **Create User Modal** and click `Create`.
  ![Create Managed User](images/create_managed_user.png)
- Click `+` in the `Roles` modal.
- Select your desired `Project` and `Role`.
- Click `Assign`.
  ![Assign Role](images/assign_role.png)
- Click `+` button in the `Permissions` modal select desired permissions.
- Click `Select`.
  ![Select Permissions](images/select_permissions.png)
 