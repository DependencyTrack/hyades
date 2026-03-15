# File Storage

Dependency-Track uses file storage for intermediate data during background processing,
including uploaded BOMs, vulnerability analysis results, and large notifications.
Files are short-lived and automatically cleaned up after processing.

The storage provider is selected via [`dt.file-storage.provider`](../../reference/configuration/api-server.md#dtfile-storageprovider).

Both providers compress stored files using [zstd](https://facebook.github.io/zstd/). The compression level is
configurable per provider (default: 5, range: -7 to 22). Higher levels yield
better compression at the cost of CPU.

## Providers

### Local

The `local` provider stores files on the local filesystem. This is the default.

```ini
dt.file-storage.provider=local
dt.file-storage.local.directory=/data/storage
```

When running multiple instances, all nodes must have access to the same directory.
A shared persistent volume (e.g. NFS) works well for this.

Configuration:

- [`dt.file-storage.local.directory`](../../reference/configuration/api-server.md#dtfile-storagelocaldirectory)
- [`dt.file-storage.local.compression.level`](../../reference/configuration/api-server.md#dtfile-storagelocalcompressionlevel)

### S3

The `s3` provider stores files in an S3-compatible object store (AWS S3, MinIO, etc.).
Use this when a shared volume is impractical.

The bucket must exist before startup. Dependency-Track will verify its existence
and fail to start if it's not found.

```ini
dt.file-storage.provider=s3
dt.file-storage.s3.endpoint=https://s3.amazonaws.com
dt.file-storage.s3.bucket=dtrack-files
dt.file-storage.s3.access.key=<access-key>
dt.file-storage.s3.secret.key=<secret-key>
dt.file-storage.s3.region=us-east-1
```

Configuration:

- [`dt.file-storage.s3.endpoint`](../../reference/configuration/api-server.md#dtfile-storages3endpoint)
- [`dt.file-storage.s3.bucket`](../../reference/configuration/api-server.md#dtfile-storages3bucket)
- [`dt.file-storage.s3.access.key`](../../reference/configuration/api-server.md#dtfile-storages3accesskey)
- [`dt.file-storage.s3.secret.key`](../../reference/configuration/api-server.md#dtfile-storages3secretkey)
- [`dt.file-storage.s3.region`](../../reference/configuration/api-server.md#dtfile-storages3region)
- [`dt.file-storage.s3.compression.level`](../../reference/configuration/api-server.md#dtfile-storages3compressionlevel)
- [`dt.file-storage.s3.connect-timeout-ms`](../../reference/configuration/api-server.md#dtfile-storages3connect-timeout-ms)
- [`dt.file-storage.s3.read-timeout-ms`](../../reference/configuration/api-server.md#dtfile-storages3read-timeout-ms)
- [`dt.file-storage.s3.write-timeout-ms`](../../reference/configuration/api-server.md#dtfile-storages3write-timeout-ms)