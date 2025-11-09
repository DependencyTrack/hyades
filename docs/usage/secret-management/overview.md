!!! note
    This feature is still being developed. While you can already *manage* secrets,
    you cannot *use* them anywhere yet.

Dependency-Track integrates with various 3rd party systems,
most of which require some sort of credential to authenticate
with them: API keys, passwords, or access tokens. Such secrets
must be stored securely to prevent leakage.

While injecting secrets via environment variables or files is possible
and very common, it has a significant downside: it requires applications
to be restarted, leading to undesirable downtime.

To address this issue, Dependency-Track offers a mechanism for centralized
secret management. Changes made here become immediately available to all
nodes in a cluster, without the need to restart them.

Depending on the capabilities of the configured [provider](./providers.md),
secrets can be [created](#creating-secrets), [updated](#updating-secrets),
and [deleted](#deleting-secrets).

![Secret management overview](./images/secret-management-overview.png)

!!! info
    It is not possible to *view* the value of secrets after they have been created.
    Secrets are decrypted by the platform as needed, but never disclosed via
    REST API or user interface.

## Creating Secrets

Users with the `SECRET_MANAGEMENT` or `SECRET_MANAGEMENT_CREATE` permission
can create new secrets by clicking the *Create* button. This will open a
dialogue asking for the following information:

* A unique name for the secret
* The value of the secret
* An optional description

![Create secret dialogue](./images/create-secret.png)

## Updating Secrets

Users with the `SECRET_MANAGEMENT` or `SECRET_MANAGEMENT_UPDATE` permission
can update existing secrets by clicking the :fontawesome-solid-pen: button
in the *Actions* column of the secret. This will open a dialogue asking for
the following information:

* The new secret value
* An optional description

![Update secret dialogue](./images/update-secret.png)

Leaving the *value* input empty will cause the existing value to remain
unchanged. If a new value is provided, the old value is unrecoverably
overwritten.

## Deleting Secrets

Users with the `SECRET_MANAGEMENT` or `SECRET_MANAGEMENT_DELETE` permission
can delete existing secrets by clicking the :fontawesome-solid-trash-alt: button
in the *Actions* column of the secret. This will open a dialogue asking for
confirmation.

![Delete secret dialogue](./images/delete-secret.png)

Deleted secrets cannot be restored. Proceed with caution.

## Using Secrets

!!! warning
    This is not yet implemented.

It will be possible to securely reference secrets in configuration using expressions, e.g.:

```jinja linenums="1"
{{ secret('OSSINDEX_API_KEY') }}
```