# GitLab Integration - SBOM Push

## SBOM Push with JWT Token

SBOM Push allows users to upload SBOMs from GitLab to Dependency-Track. The Dependency-Track API accepts a valid [GitLab ID Token](https://docs.gitlab.com/ci/secrets/id_token_authentication/) and base64 encoded BOM file, assesses if a user has permission to upload SBOMs, and then uses the accepted parameters to upload the BOM file to Dependency-Track.

## How to Upload a BOM File

These are the steps needed to upload a BOM file from a GitLab job.

1. Generate a [GitLab application ID](#generate-a-gitlab-application-id)
2. Update the project's [`application.properites`](properties-settings)
3. Call the upload endpoint with an access token from [authenticating GitLab with Dependency-Track](authenticate-gitLab-with-dependency-track)

## Generate a GitLab Application ID

1. Navigate to a profile in gitlab:
    * Select the profile image from the top banner
    * Select "Edit profile"
2. Select "Applications" from the User settings column on the left side of the screen
3. Press the Add new application button
4. Specify a name in the Name field
5. In the Redirect URI box, add the following: <http://dtrack.example.com/static/oidc-callback.html>
6. Ensure the Confidential button is unchecked
7. Check the following buttons in the Scopes field: openid, profile, email
8. Press the Save Application box
9. Store the resuling Application ID for use in subsequent steps

## Properties Settings

Make the following chagnes to `application.properites`

* alpine.oidc.enabled=true
* alpine.oidc.client.id=<APP_ID>
* alpine.oidc.issuer=<BASE_GITLAB_URL>
* alpine.oidc.user.provisioning=true
* alpine.oidc.team.synchronization=false
* alpine.oidc.teams.default=GitLab Users
* alpine.oidc.auth.customizer=org.dependencytrack.integrations.gitlab.GitLabAuthenticationCustomizer

## Authenticate GitLab with Dependency-Track

To make API calls from a GitLab job to Dependency-Track, first authenticate with the Dependency-Track endpoint `/user/oidc/login`, and then use the recieved access token to make subsequent API calls.

```bash
curl -X "POST" "http://dtrack.example.com/api/v1/user/oidc/login" \
    -H 'Content-Type: application/x-www-form-urlencoded" \
    -F "idToken=$ID_TOKEN" \
```

## Submit A BOM Upload Request

To publish a BOM file from GitLab, use a valid access token and GitLab ID token.

The `/bom/gitlab` endpoint has the following form data parameters:

* `idToken` (required) - Must be a valid
* `bom` (required) - Must be a base64 encoded bom file
* `autoCreate` (optional) - Flag to create a project if it does not already exist
* `isLatest` (optional) - Flag to denote if this is the latest upload version

This is the curl request to make in the GitLab job.

```bash
curl -X "POST" "http://dtrack.example.com/api/v1/bom/gitlab" \
    -H "Authorization: Bearer <ACCESS_TOKEN>" \
    -H 'Content-Type: multipart/form-data' \
    -F "gitLab_token=$ID_TOKEN | base64" \
    -F "bom=base64_encoded_file.txt"
    -F "autoCreate=true" \
    -F "isLatest=true" \
```
