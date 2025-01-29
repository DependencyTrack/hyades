| Status   | Date       | Author(s)                            |
|:---------|:-----------|:-------------------------------------|
| Proposed | 2025-01-29 | [@nscuro](https://github.com/nscuro) |

## Context

In [ADR-001], we surfaced the complication of transmitting large payloads (BOMs, notifications, analysis results).

Neither message brokers, nor RDBMSes are meant to store large blobs of data.
The introduction of a Postgres-based workflow orchestration solution (see [ADR-002]) does not change this reality.

To ensure our core systems stay performant, we should fall back to storing files externally,
and instead only pass references to those files around. This strategy is followed by messaging
services such as AWS SNS, which offloads payloads to S3 if they exceed a size of `256KB`.

We do not need fully-fledged filesystem capabilities. Pragmatically speaking, all we need is
a glorified key-value store. An obvious choice would be to delegate this to an object store
such as S3. However, we recognize that not all users are able or willing to deploy additional
infrastructure.

Thus, at minimum, the following storage solutions must be supported:

1. **Local filesystem**. This option is viable for users running single-node clusters, or those with
   access to reasonably fast network storage. This should be the default, as it does not require any
   additional setup.
2. **S3-compatible object storage**. This option is viable for users running multi-node clusters,
   operating in the cloud, and / or without access to network storage. It could also be required
   to support very large clusters with increased storage requirements.

Optionally, the following solutions may be offered as well:

* **In-memory**. For unit tests, integration tests, and single-node demo clusters.
* **Non-S3-compatible object storage**. Like Azure Blob Storage and similar proprietary offerings.

To reduce networking and storage costs, as well as network latencies,
files *should* be compressed *prior* to sending them over the wire.

When retrieving files from storage, providers *should* verify their integrity, 
to prevent processing of files that have been tampered with.

## Decision

To communicate file references, we will leverage metadata objects. Metadata objects will hold a unique
reference to a file, within the context of a given storage provider. To enable use cases such as encryption
and integrity verification, we allow providers to attach additional metadata.

Since our primary means of internal communication is based on Protobuf, we will define the file metadata
in this format. It will allow us to easily attach it to other Protobuf messages.

```protobuf linenums="1"
syntax = "proto3";

// Metadata of a file stored by a storage provider.
message FileMetadata {
  // Unique identifier of the file.
  string key = 1;

  // Name of the storage provider that hosts the file.
  string storage_name = 2;

  // Additional metadata of the storage provider,
  // i.e. values used for integrity verification.
  map<string, string> storage_metadata = 3;
}
```

The API surface will evolve around the `FileStorage` interface, which exposes methods to
store, retrieve, and delete files:

```java linenums="1"
package org.dependencytrack.storage;

import org.dependencytrack.plugin.api.ExtensionPoint;
import org.dependencytrack.proto.storage.v1alpha1.FileMetadata;

import java.io.IOException;
import java.util.Collection;

public interface FileStorage extends ExtensionPoint {

    /**
     * Persist data to a file in storage.
     * <br/>
     * Storage providers may transparently perform additional steps,
     * such as encryption and compression.
     * 
     * @param name Name of the file. This name is not guaranteed to be reflected
     *             in storage as-is. It may be modified or changed entirely.
     * @param content Data to store.
     * @return Metadata of the stored file.
     * @throws IOException When storing the file failed.
     */
    FileMetadata store(String name, byte[] content) throws IOException;

    /**
     * Retrieves a file from storage.
     * <br/>
     * Storage providers may transparently perform additional steps,
     * such as integrity verification, decryption and decompression.
     * <br/>
     * Trying to retrieve a file from a different storage provider
     * is an illegal operation and yields an exception.
     * 
     * @param fileMetadata Metadata of the file to retrieve.
     * @return The file's content.
     * @throws IOException When retrieving the file failed.
     */
    byte[] get(FileMetadata fileMetadata) throws IOException;

    /**
     * Deletes a file from storage.
     * <br/>
     * Trying to delete a file from a different storage provider
     * is an illegal operation and yields an exception.
     * 
     * @param fileMetadata Metadata of the file to delete.
     * @return {@code true} when the file was deleted, otherwise {@code false}.
     * @throws IOException When deleting the file failed.
     */
    boolean delete(FileMetadata fileMetadata) throws IOException;

    /**
     * Some providers support batch deletes.
     * 
     * @see #delete(FileMetadata)
     * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObjects.html">S3 DeleteObjects API</a>
     */
    void deleteMany(Collection<FileMetadata> fileMetadata) throws IOException;

}
```

To support multiple, configurable providers, we will leverage the plugin mechanism introduced in [hyades-apiserver/#805].  
The mechanism, once its API is published as separate Maven artifact, will allow users to develop their own storage
providers if required, without requiring changes to the Dependency-Track codebase.

This allows storage providers to be configured as follows:

```properties linenums="1"
# Defines the file storage extension to use.
# When not set, an enabled extension will be chosen based on its priority.
# It is recommended to explicitly configure an extension for predictable behavior.
#
# @category:     Storage
# @type:         enum
# @valid-values: [local, memory]
file.storage.default.extension=

# Whether the local file storage extension shall be enabled.
#
# @category: Storage
# @type:     boolean
file.storage.extension.local.enabled=true

# Defines the local directory where files shall be stored.
# Has no effect unless file.storage.extension.local.enabled is `true`.
#
# @category: Storage
# @default:  ${alpine.data.directory}/storage
# @type:     string
file.storage.extension.local.directory=

# Whether the in-memory file storage extension shall be enabled.
#
# @category: Storage
# @type:     boolean
file.storage.extension.memory.enabled=false
```

Application code can interact with `FileStorage` via the `PluginManager`:

```java linenums="1"
package org.dependencytrack.foobar;

import org.dependencytrack.plugin.PluginManager;
import org.dependencytrack.proto.storage.v1alpha1.FileMetadata;
import org.dependencytrack.storage.FileStorage;

class Foo {
    
    void bar() {
        try (var fileStorage = PluginManager.getInstance().getExtension(FileStorage.class)) {
            FileMetadata fileMetadata = fileStorage.store("filename", "content".getBytes());
            
            byte[] fileContent = fileStorage.get(fileMetadata);
            
            fileStorage.delete(fileMetadata);
        }
    }
    
}
```

## Consequences

* There is a non-zero chance of orphaned files remaining in storage. Crashes or service outages on either end
  can prevent Dependency-Track from deleting files if they're no longer needed. Some storage providers such as
  AWS S3 allow retention policies to be configured. This is not true for local file storage, however.
  As a consequence, storage providers should make an effort to make the creation timestamp of files obvious,
  i.e. as part of the file's name, if relying on the file system's metadata is not possible.
* Storage operations are not atomic with database operations. This is an acceptable tradeoff,
  because it does not impact the integrity of the system. Application code is expected to gracefully
  deal with missing files, and perform compensating actions accordingly. Since file storage is not the
  primary system of record, files existing without the application knowing about them is not an issue.

[ADR-001]: 001-drop-kafka-dependency.md
[ADR-002]: 002-workflow-orchestration.md

[hyades-apiserver/#805]: https://github.com/DependencyTrack/hyades-apiserver/pull/805