# GitLab Sync

GitLab Sync enables automatic synchronization of GitLab projects with Dependency-Track when logging in via GitLab single sign-on (SSO) using GitLab's OIDC OAuth integration. SSO with GitLab is always available, but enabling the GitLab Sync integration specifically controls whether project synchronization from GitLab occurs upon OIDC login. This ensures authentication with GitLab SSO is always possible, but GitLab projects are only pulled and managed within Dependency-Track when the integration is enabled.

> **Note:** SSO/OIDC must be configured for GitLab in Dependency-Track for GitLab Sync to function. Without this configuration, GitLab Sync will not operate.

[Official guide](https://docs.dependencytrack.org/getting-started/openidconnect-configuration/).


## Enabling GitLab Sync

Before enabling GitLab Sync, GitLab must be configured as an OpenID Connect (OIDC) provider in the Dependency-Track instance. This involves registering a new OAuth application in GitLab and configuring the appropriate redirect URIs and scopes.

For detailed, step-by-step instructions and prerequisites, refer to the [Dependency-Track OpenID Connect Configuration documentation](https://docs.dependencytrack.org/getting-started/openidconnect-configuration/) under the "GitLab Sync" section.

## Configuration Options

Several configuration options are available for GitLab Sync, many of which are similar to properties in `application.properties` for Dependency-Track server:

- **Application ID:**
  Displays the OAuth application ID registered for the GitLab integration. This field is uneditable and provided for reference and copying only.

- **URL:**
  Displays the base URL of the configured GitLab instance (e.g., `https://gitlab.com`). This field is uneditable and provided for reference and copying only.

- **Include archived projects:**
  Toggle to include or exclude archived GitLab projects from synchronization.

- **Topics:**
  Specify topics to filter which projects are synchronized.

- **Enable GitLab SBOM push:**
  Toggle to enable or disable pushing SBOMs (Software Bill of Materials) to GitLab.
  *When disabled, the following options are hidden:*

    - **Allow users to create missing projects:**
      Allow users to create new projects in GitLab if they do not already exist.

    - **Audience:**
      The expected audience value for OIDC tokens.

    - **GitLab JWKS Path:**
      The path to the JSON Web Key Set (JWKS) for validating GitLab tokens.

These settings can be managed in the Dependency-Track UI under `Administration > Integration > GitLab`.

## How It Works

1. **SSO Authentication:**
   Users can always sign in to Dependency-Track using GitLab SSO, regardless of whether GitLab Sync is enabled.

2. **Project Synchronization:**
   When GitLab Sync is enabled, Dependency-Track will automatically synchronize the user's GitLab projects upon OIDC login. If disabled, SSO login remains available, but no project synchronization occurs.

3. **Integration Control:**
   The GitLab Sync integration and its options can be toggled in the Dependency-Track UI.

---

For further assistance, consult the Dependency-Track documentation or your system administrator.
