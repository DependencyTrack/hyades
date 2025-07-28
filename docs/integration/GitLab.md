# GitLab

## GitLab Sync

GitLab Sync enables automatic synchronization of GitLab projects with Dependency-Track when logging in via GitLab single sign-on (SSO) using GitLab's OIDC OAuth integration. SSO with GitLab is always available, but enabling the GitLab Sync integration specifically controls whether project synchronization from GitLab occurs upon OIDC login. This ensures authentication with GitLab SSO is always possible, but GitLab projects are only pulled and managed within Dependency-Track when the integration is enabled.

> **Note:** SSO/OIDC must be configured for GitLab in Dependency-Track for GitLab Sync to function. Without this configuration, GitLab Sync will not operate.

[Official guide](https://docs.dependencytrack.org/getting-started/openidconnect-configuration/).

### Enabling GitLab Sync

Before enabling GitLab Sync, GitLab must be configured as an OpenID Connect (OIDC) provider in the Dependency-Track instance. This involves registering a new OAuth application in GitLab and configuring the appropriate redirect URIs and scopes.

For detailed, step-by-step instructions and prerequisites, refer to the [Dependency-Track OpenID Connect Configuration documentation](https://docs.dependencytrack.org/getting-started/openidconnect-configuration/) under the "GitLab Sync" section.

### Configuration Options

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
  _When disabled, the following options are hidden:_

  - **Allow users to create missing projects:**
    Allow users to create new projects in GitLab if they do not already exist.

  - **Audience:**
    The expected audience value for OIDC tokens.

  - **GitLab JWKS Path:**
    The path to the JSON Web Key Set (JWKS) for validating GitLab tokens.

These settings can be managed in the Dependency-Track UI under `Administration > Integration > GitLab`.

### How It Works

1. **SSO Authentication:**
   Users can always sign in to Dependency-Track using GitLab SSO, regardless of whether GitLab Sync is enabled.

2. **Project Synchronization:**
   When GitLab Sync is enabled, Dependency-Track will automatically synchronize the user's GitLab projects upon OIDC login. If disabled, SSO login remains available, but no project synchronization occurs.

3. **Integration Control:**
   The GitLab Sync integration and its options can be toggled in the Dependency-Track UI.

---

For further assistance, consult the Dependency-Track documentation or your system administrator.

## GitLab SBOM Push

### What is SBOM Push with GitLab?

SBOM Push allows users to upload SBOMs from GitLab to Dependency-Track. The Dependency-Track API accepts a valid [GitLab ID Token](https://docs.gitlab.com/ci/secrets/id_token_authentication/) and base64 encoded BOM file, assesses if a user has permission to upload SBOMs, and then uses the accepted parameters to upload the BOM file to Dependency-Track.

### What is an Access Token?

An access token from the `/user/oidc/login` endpoint is required to authenticate GitLab with Dependency-Track and make subsequent API calls. The access token provided has an immediate expiration time, so nesting the request to `/user/oidc/login` as a bearer token will allow an additional API call.

If the setup below has been completed, this authentication request to Dependency-Track will provide an access token.

```bash
curl -X POST "http://dtrack.example.com/api/v1/user/oidc/login" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "idToken=$ID_TOKEN"
```

### Setup

#### Generate a GitLab Application ID

A GitLab application ID will be needed for authentication.

1. Navigate to a profile in GitLab:
   - Select the profile image from the top banner
   - Select "Edit profile"
2. Select "Applications" from the User settings column on the left side of the screen

   ![Applications](../../images/integrations_gitlab_applications.png)

3. Select the Add new application button
4. Specify a name in the Name field
5. In the Redirect URI box, add the following: <http://dtrack.example.com/static/oidc-callback.html>
6. Ensure the Confidential button is unchecked
7. Check the following buttons in the Scopes field: openid, profile, email
   ![New Application ID](../../images/integrations_gitlab_app_id.png)
   ![New Application ID 2](../../images/integrations_gitlab_app_id2.png)
8. Select the Save application button
9. Store the resulting Application ID for use in subsequent steps

#### Configuration Settings

Pushing SBOMs from GitLab is an optional feature for Dependency-Track, so an admin will need to turn on the necessary settings to enable the feature.

Configurations to be set by admins in `Administration > Integrations > GitLab`:

- Turn on `Enable GitLab integration`
- Set `GitLab URL` to the correct base url (Ex. <https://gitlab.example.com>)
- Turn on `Enable GitLab SBOM push`
- Turn on `Allow users to create missing projects` (optional but won't work if projects are missing)
- Set `GitLab JWKS Path` to **/oauth/discovery/keys**

![GitLab Push Settings](../../images/integrations_gitlab_sbom_settings.png)

Make the following changes to the `application.properites` file

- alpine.oidc.enabled=true
- alpine.oidc.client.id=<APP_ID>
- alpine.oidc.issuer=<BASE_GITLAB_URL>
- alpine.oidc.user.provisioning=true
- alpine.oidc.team.synchronization=false
- alpine.oidc.teams.default=GitLab Users
- alpine.oidc.auth.customizer=org.dependencytrack.integrations.gitlab.GitLabAuthenticationCustomizer

### Usage

Using a GitLab job to upload a BOM file to Dependency-Track will only work for a user who has previously signed into Dependency-Track and has the `BOM_UPLOAD` permission.

#### Adding the ID Token to a GitLab YAML File

To use a [GitLab ID Token](https://docs.gitlab.com/ci/secrets/id_token_authentication/) in the BOM upload api request, the application ID created above should be set to the ID Token's audience (aud) field.

```yml
bom_upload_job:
  id_tokens:
    ID_TOKEN:
      aud: <APP_ID>
```

#### How to Submit a BOM Upload Request

The `/bom/gitlab` endpoint has the following form data parameters:

- `gitLabToken` (required) - Use the GitLab ID Token `ID_TOKEN` previously configured
- `bom` (required) - Must be a bom file that is base64 encoded
- `isLatest` (optional) - Set as true to mark the current upload as the latest version

To publish a BOM file from GitLab, use the access token from the login endpoint and an ID token from a GitLab job.

```bash
curl -X POST "http://dtrack.example.com/api/v1/bom/gitlab" \
-H "Authorization: Bearer $(curl -s -X POST "http://dtrack.example.com/api/v1/user/oidc/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "idToken=$ID_TOKEN")" \
-F "gitLabToken=$ID_TOKEN" \
-F "bom=$BOM_ENCODED"
```
