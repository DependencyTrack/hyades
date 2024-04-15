<!--
  GENERATED. DO NOT EDIT.

  Generated with: -t ./scripts/config-docs.md.peb -o ./docs/reference/configuration/api-server.md ../hyades-apiserver/src/main/resources/application.properties
  Generated on:   2024-04-15T16:46:33.894941605+02:00[Europe/Berlin]
-->

## Database

### alpine.database.password

Specifies the password to use when authenticating to the database.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>dtrack</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.enabled

Specifies if the database connection pool is enabled.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>true</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.idle.timeout

This property controls the maximum amount of time that a connection is  allowed to sit idle in the pool.  The property can be set globally for both transactional and non-transactional  connection pools, or for each pool type separately. When both global and pool-specific  properties are set, the pool-specific properties take precedence.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>300000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_IDLE_TIMEOUT</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.max.lifetime

This property controls the maximum lifetime of a connection in the pool.  An in-use connection will never be retired, only when it is closed will  it then be removed.  The property can be set globally for both transactional and non-transactional  connection pools, or for each pool type separately. When both global and pool-specific  properties are set, the pool-specific properties take precedence.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>600000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_MAX_LIFETIME</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.max.size

This property controls the maximum size that the pool is allowed to reach,  including both idle and in-use connections.  The property can be set globally for both transactional and non-transactional  connection pools, or for each pool type separately. When both global and pool-specific  properties are set, the pool-specific properties take precedence.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>20</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_MAX_SIZE</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.min.idle

This property controls the minimum number of idle connections in the pool.  This value should be equal to or less than [`alpine.database.pool.max.size`](#alpinedatabasepoolmaxsize).  Warning: If the value is less than [`alpine.database.pool.max.size`](#alpinedatabasepoolmaxsize),  [`alpine.database.pool.idle.timeout`](#alpinedatabasepoolidletimeout) will have no effect.  The property can be set globally for both transactional and non-transactional  connection pools, or for each pool type separately. When both global and pool-specific  properties are set, the pool-specific properties take precedence.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>10</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_MIN_IDLE</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.nontx.idle.timeout



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.idle.timeout}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_NONTX_IDLE_TIMEOUT</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.nontx.max.lifetime



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.max.lifetime}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_NONTX_MAX_LIFETIME</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.nontx.max.size



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.max.size}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_NONTX_MAX_SIZE</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.nontx.min.idle



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.min.idle}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_NONTX_MIN_IDLE</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.tx.idle.timeout



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.idle.timeout}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_TX_IDLE_TIMEOUT</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.tx.max.lifetime



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.max.lifetime}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_TX_MAX_LIFETIME</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.tx.max.size



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.max.size}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_TX_MAX_SIZE</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.pool.tx.min.idle



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.pool.min.idle}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_POOL_TX_MIN_IDLE</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.url

Specifies the JDBC URL to use when connecting to the database.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>jdbc:postgresql://localhost:5432/dtrack</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_URL</code></td>
    </tr>
  </tbody>
</table>

### alpine.database.username

Specifies the username to use when authenticating to the database.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>dtrack</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_USERNAME</code></td>
    </tr>
  </tbody>
</table>

### database.migration.password

Defines the database password for executing migrations.  If not set, the value of [`alpine.database.password`](#alpinedatabasepassword) will be used.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.password}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DATABASE_MIGRATION_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### database.migration.url

Defines the database JDBC URL to use when executing migrations.  If not set, the value of [`alpine.database.url`](#alpinedatabaseurl) will be used.  Should generally not be set, unless TLS authentication is used,  and custom connection variables are required.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.url}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DATABASE_MIGRATION_URL</code></td>
    </tr>
  </tbody>
</table>

### database.migration.username

Defines the database user for executing migrations.  If not set, the value of [`alpine.database.username`](#alpinedatabaseusername) will be used.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.database.username}</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DATABASE_MIGRATION_USERNAME</code></td>
    </tr>
  </tbody>
</table>

### run.migrations

Defines whether database migrations should be executed on startup.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>true</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>RUN_MIGRATIONS</code></td>
    </tr>
  </tbody>
</table>



## General

### alpine.api.key.prefix

Defines the prefix to be used for API keys. A maximum prefix length of 251  characters is supported. The prefix may also be left empty.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>odt_</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_API_KEY_PREFIX</code></td>
    </tr>
  </tbody>
</table>

### alpine.bcrypt.rounds

Specifies the number of bcrypt rounds to use when hashing a user's password.  The higher the number the more secure the password, at the expense of  hardware resources and additional time to generate the hash.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>14</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_BCRYPT_ROUNDS</code></td>
    </tr>
  </tbody>
</table>

### alpine.data.directory

Defines the path to the data directory. This directory will hold logs,  keys, and any database or index files along with application-specific  files or directories.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>~/.dependency-track</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATA_DIRECTORY</code></td>
    </tr>
  </tbody>
</table>

### alpine.private.key.path

Defines the paths to the public-private key pair to be used for signing and verifying digital signatures.  The keys will be generated upon first startup if they do not exist.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.data.directory}/keys/private.key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>/var/run/secrets/private.key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_PRIVATE_KEY_PATH</code></td>
    </tr>
  </tbody>
</table>

### alpine.public.key.path

Defines the paths to the public-private key pair to be used for signing and verifying digital signatures.  The keys will be generated upon first startup if they do not exist.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.data.directory}/keys/public.key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>/var/run/secrets/public.key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_PUBLIC_KEY_PATH</code></td>
    </tr>
  </tbody>
</table>

### alpine.secret.key.path

Defines the path to the secret key to be used for data encryption and decryption.  The key will be generated upon first startup if it does not exist.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${alpine.data.directory}/keys/secret.key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_SECRET_KEY_PATH</code></td>
    </tr>
  </tbody>
</table>

### bom.upload.processing.trx.flush.threshold

Defines the number of write operations to perform during BOM processing before changes are flushed to the database.  Smaller values may lower memory usage of the API server, whereas higher values will improve performance as fewer  network round-trips to the database are necessary.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>10000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>BOM_UPLOAD_PROCESSING_TRX_FLUSH_THRESHOLD</code></td>
    </tr>
  </tbody>
</table>

### integrity.check.enabled



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>INTEGRITY_CHECK_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### integrity.initializer.enabled

Specifies whether the Integrity Initializer shall be enabled.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>INTEGRITY_INITIALIZER_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### tmp.delay.bom.processed.notification

Delays the BOM_PROCESSED notification until the vulnerability analysis associated with a given BOM upload  is completed. The intention being that it is then "safe" to query the API for any identified vulnerabilities.  This is specifically for cases where polling the /api/v1/bom/token/<TOKEN> endpoint is not feasible.  THIS IS A TEMPORARY FUNCTIONALITY AND MAY BE REMOVED IN FUTURE RELEASES WITHOUT FURTHER NOTICE.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TMP_DELAY_BOM_PROCESSED_NOTIFICATION</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.analysis.enabled

Defines whether vulnerability policy analysis is enabled.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_ANALYSIS_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.bundle.auth.password

For nginx server, if username and bearer token both are provided, basic auth will be used,  else the auth header will be added based on the not null values  Defines the password to be used for basic authentication against the service hosting the policy bundle.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_BUNDLE_AUTH_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.bundle.auth.username

Defines the username to be used for basic authentication against the service hosting the policy bundle.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_BUNDLE_AUTH_USERNAME</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.bundle.bearer.token

Defines the token to be used as bearerAuth against the service hosting the policy bundle.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_BUNDLE_BEARER_TOKEN</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.bundle.source.type

Defines the type of source from which policy bundles are being fetched from.  Required when [`vulnerability.policy.bundle.url`](#vulnerabilitypolicybundleurl) is set.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[nginx, s3]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>NGINX</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_BUNDLE_SOURCE_TYPE</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.bundle.url

Defines where to fetch the policy bundle from.For S3, just the base url needs to be provided with port  For nginx, the whole url with bundle name needs to be given  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>http://example.com:80/bundles/bundle.zip</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_BUNDLE_URL</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.s3.access.key

S3 related details. Access key, secret key, bucket name and bundle names are mandatory if S3 is chosen. Region is optional  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_S3_ACCESS_KEY</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.s3.bucket.name



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_S3_BUCKET_NAME</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.s3.bundle.name



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_S3_BUNDLE_NAME</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.s3.region



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_S3_REGION</code></td>
    </tr>
  </tbody>
</table>

### vulnerability.policy.s3.secret.key



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>VULNERABILITY_POLICY_S3_SECRET_KEY</code></td>
    </tr>
  </tbody>
</table>

### workflow.retention.duration

Defines the duration for how long workflow data is being retained, after all steps transitioned into a non-terminal  state (CANCELLED, COMPLETED, FAILED, NOT_APPLICABLE).  The duration must be specified in ISO8601 notation (https://en.wikipedia.org/wiki/ISO_8601#Durations).  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>P3D</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>WORKFLOW_RETENTION_DURATION</code></td>
    </tr>
  </tbody>
</table>

### workflow.step.timeout.duration

Defines the duration for how long a workflow step is allowed to remain in PENDING state  after being started. If this duration is exceeded, workflow steps will transition into the TIMED_OUT state.  If they remain in TIMED_OUT for the same duration, they will transition to the FAILED state.  The duration must be specified in ISO8601 notation (https://en.wikipedia.org/wiki/ISO_8601#Durations).  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1H</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>WORKFLOW_STEP_TIMEOUT_DURATION</code></td>
    </tr>
  </tbody>
</table>



## HTTP Proxy

### alpine.http.proxy.address

HTTP proxy address. If set, then [`alpine.http.proxy.port`](#alpinehttpproxyport) must be set too.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>proxy.example.com</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_PROXY_ADDRESS</code></td>
    </tr>
  </tbody>
</table>

### alpine.http.proxy.password



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_PROXY_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### alpine.http.proxy.port



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>8888</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_PROXY_PORT</code></td>
    </tr>
  </tbody>
</table>

### alpine.http.proxy.username



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_PROXY_USERNAME</code></td>
    </tr>
  </tbody>
</table>

### alpine.no.proxy



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>localhost,127.0.0.1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_NO_PROXY</code></td>
    </tr>
  </tbody>
</table>



## Kafka

### alpine.kafka.processor.repo.meta.analysis.result.consumer.auto.offset.reset



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[earliest, latest, none]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>earliest</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_CONSUMER_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.consumer.group.id



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>dtrack-apiserver-processor</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_CONSUMER_GROUP_ID</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.max.concurrency



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>-1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_MAX_CONCURRENCY</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.processing.order



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[key, partition, unordered]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_PROCESSING_ORDER</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.retry.initial.delay.ms



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>1000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_RETRY_INITIAL_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.retry.max.delay.ms



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>180000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_RETRY_MAX_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.retry.multiplier



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>2</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_RETRY_MULTIPLIER</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.repo.meta.analysis.result.retry.randomization.factor



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>double</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0.3</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_REPO_META_ANALYSIS_RESULT_RETRY_RANDOMIZATION_FACTOR</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.consumer.auto.offset.reset



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[earliest, latest, none]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>earliest</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_CONSUMER_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.consumer.group.id



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>dtrack-apiserver-processor</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_CONSUMER_GROUP_ID</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.max.concurrency



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>-1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_MAX_CONCURRENCY</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.processing.order



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[key, partition, unordered]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>partition</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_PROCESSING_ORDER</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.retry.initial.delay.ms



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>3000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_RETRY_INITIAL_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.retry.max.delay.ms



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>180000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_RETRY_MAX_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.retry.multiplier



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>2</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_RETRY_MULTIPLIER</code></td>
    </tr>
  </tbody>
</table>

### alpine.kafka.processor.vuln.mirror.retry.randomization.factor



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>double</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0.3</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_MIRROR_RETRY_RANDOMIZATION_FACTOR</code></td>
    </tr>
  </tbody>
</table>

### application.id



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>dtrack-apiserver</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>APPLICATION_ID</code></td>
    </tr>
  </tbody>
</table>

### kafka.auto.offset.reset



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[earliest, latest, none]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>earliest</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>

### kafka.bootstrap.servers



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>localhost:9092</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_BOOTSTRAP_SERVERS</code></td>
    </tr>
  </tbody>
</table>

### kafka.keystore.password



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_KEYSTORE_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### kafka.keystore.path



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_KEYSTORE_PATH</code></td>
    </tr>
  </tbody>
</table>

### kafka.mtls.enabled



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_MTLS_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### kafka.num.stream.threads



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>3</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_NUM_STREAM_THREADS</code></td>
    </tr>
  </tbody>
</table>

### kafka.security.protocol



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[PLAINTEXT, SASL_SSL_PLAINTEXT, SASL_PLAINTEXT, SSL]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_SECURITY_PROTOCOL</code></td>
    </tr>
  </tbody>
</table>

### kafka.streams.deserialization.exception.threshold.count

Defines the number of deserialization errors deemed to be acceptable in a given time frame.  Until the threshold is reached, records failing deserialization will be logged and skipped.  When the threshold is exceeded, further consumption is stopped.  The interval must be specified in ISO8601 duration notation (https://en.wikipedia.org/wiki/ISO_8601#Durations).  The default threshold is 5 errors per 30min.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_DESERIALIZATION_EXCEPTION_THRESHOLD_COUNT</code></td>
    </tr>
  </tbody>
</table>

### kafka.streams.deserialization.exception.threshold.interval



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT30M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_DESERIALIZATION_EXCEPTION_THRESHOLD_INTERVAL</code></td>
    </tr>
  </tbody>
</table>

### kafka.streams.production.exception.threshold.count

Defines the number of production errors deemed to be acceptable in a given time frame.  Until the threshold is reached, records failing to be produced will be logged and skipped.  When the threshold is exceeded, further production is stopped.  Only certain types of errors will be treated this way; Unexpected errors will cause a  stop of production immediately.  The interval must be specified in ISO8601 duration notation (https://en.wikipedia.org/wiki/ISO_8601#Durations).  The default threshold is 5 errors per 30min.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_PRODUCTION_EXCEPTION_THRESHOLD_COUNT</code></td>
    </tr>
  </tbody>
</table>

### kafka.streams.production.exception.threshold.interval



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT30M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_PRODUCTION_EXCEPTION_THRESHOLD_INTERVAL</code></td>
    </tr>
  </tbody>
</table>

### kafka.streams.transient.processing.exception.threshold.count

Defines the number of times record processing will be retried in case of unhandled, yet transient errors.  Until the threshold is reached, records fetched since the last successful offset commit will be attempted to be re-processed.  When the threshold is exceeded, further processing is stopped.  Only transient errors will be treated this way; Unexpected or non-transient errors will cause a stop of processing immediately.  The interval must be specified in ISO8601 duration notation (https://en.wikipedia.org/wiki/ISO_8601#Durations).  The default threshold is 50 errors per 30min.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>50</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_TRANSIENT_PROCESSING_EXCEPTION_THRESHOLD_COUNT</code></td>
    </tr>
  </tbody>
</table>

### kafka.streams.transient.processing.exception.threshold.interval



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT30M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_TRANSIENT_PROCESSING_EXCEPTION_THRESHOLD_INTERVAL</code></td>
    </tr>
  </tbody>
</table>

### kafka.tls.enabled



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_TLS_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### kafka.topic.prefix



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_TOPIC_PREFIX</code></td>
    </tr>
  </tbody>
</table>

### kafka.truststore.password



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_TRUSTSTORE_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### kafka.truststore.path



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_TRUSTSTORE_PATH</code></td>
    </tr>
  </tbody>
</table>



## LDAP

### alpine.ldap.attribute.mail

Specifies the LDAP attribute used to store a users email address  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>mail</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_ATTRIBUTE_MAIL</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.attribute.name

Specifies the Attribute that identifies a users ID.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>userPrincipalName</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>uid</code></li></ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>userPrincipalName</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_ATTRIBUTE_NAME</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.auth.username.format

Specifies if the username entered during login needs to be formatted prior  to asserting credentials against the directory. For Active Directory, the  userPrincipal attribute typically ends with the domain, whereas the  samAccountName attribute and other directory server implementations do not.  The %s variable will be substituted with the username asserted during login.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>%s@example.com</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>%s</code></li></ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>%s@example.com</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_AUTH_USERNAME_FORMAT</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.basedn

Specifies the base DN that all queries should search from  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>dc=example,dc=com</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_BASEDN</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.bind.password

If anonymous access is not permitted, specify a password for the username  used to bind.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_BIND_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.bind.username

If anonymous access is not permitted, specify a username with limited access  to the directory, just enough to perform searches. This should be the fully  qualified DN of the user.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_BIND_USERNAME</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.enabled

Defines if LDAP will be used for user authentication. If enabled,  <code>alpine.ldap.*</code> properties should be set accordingly.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.groups.filter

Specifies the LDAP search filter used to retrieve all groups from the directory.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>(&(objectClass=group)(objectCategory=Group))</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>(&(objectClass=groupOfUniqueNames))</code></li></ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>(&(objectClass=group)(objectCategory=Group))</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_GROUPS_FILTER</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.groups.search.filter

Specifies the LDAP search filter used to search for groups by their name.  The <code>{SEARCH_TERM}</code> variable will be substituted at runtime.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>(&(objectClass=group)(objectCategory=Group)(cn=*{SEARCH_TERM}*))</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>(&(objectClass=groupOfUniqueNames)(cn=*{SEARCH_TERM}*))</code></li></ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>(&(objectClass=group)(objectCategory=Group)(cn=*{SEARCH_TERM}*))</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_GROUPS_SEARCH_FILTER</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.security.auth

Specifies the LDAP security authentication level to use. Its value is one of  the following strings: "none", "simple", "strong". If this property is empty  or unspecified, the behaviour is determined by the service provider.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[none, simple, strong]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>simple</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_SECURITY_AUTH</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.server.url

Specifies the LDAP server URL.  <br/><br/>  Examples (Microsoft Active Directory):  <ul>  <li><code>ldap://ldap.example.com:3268</code></li>  <li><code>ldaps://ldap.example.com:3269</code></li>  </ul>  Examples (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul>  <li><code>ldap://ldap.example.com:389</code></li>  <li><code>ldaps://ldap.example.com:636</code></li>  </ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_SERVER_URL</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.team.synchronization

This option will ensure that team memberships for LDAP users are dynamic and  synchronized with membership of LDAP groups. When a team is mapped to an LDAP  group, all local LDAP users will automatically be assigned to the team if  they are a member of the group the team is mapped to. If the user is later  removed from the LDAP group, they will also be removed from the team. This  option provides the ability to dynamically control user permissions via an  external directory.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_TEAM_SYNCHRONIZATION</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.user.groups.filter

Specifies the LDAP search filter to use to query a user and retrieve a list  of groups the user is a member of. The <code>{USER_DN}</code> variable will be substituted  with the actual value of the users DN at runtime.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>(&(objectClass=group)(objectCategory=Group)(member={USER_DN}))</code></li></ul>  Example (Microsoft Active Directory - with nested group support):  <ul><li><code>(member:1.2.840.113556.1.4.1941:={USER_DN})</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>(&(objectClass=groupOfUniqueNames)(uniqueMember={USER_DN}))</code></li></ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>(member:1.2.840.113556.1.4.1941:={USER_DN})</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_USER_GROUPS_FILTER</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.user.provisioning

Specifies if mapped LDAP accounts are automatically created upon successful  authentication. When a user logs in with valid credentials but an account has  not been previously provisioned, an authentication failure will be returned.  This allows admins to control specifically which ldap users can access the  system and which users cannot. When this value is set to true, a local ldap  user will be created and mapped to the ldap account automatically. This  automatic provisioning only affects authentication, not authorization.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_USER_PROVISIONING</code></td>
    </tr>
  </tbody>
</table>

### alpine.ldap.users.search.filter

Specifies the LDAP search filter used to search for users by their name.  The <code>{SEARCH_TERM}</code> variable will be substituted at runtime.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>(&(objectClass=group)(objectCategory=Group)(cn=*{SEARCH_TERM}*))</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>(&(objectClass=inetOrgPerson)(cn=*{SEARCH_TERM}*))</code></li></ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>(&(objectClass=user)(objectCategory=Person)(cn=*{SEARCH_TERM}*))</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_LDAP_USERS_SEARCH_FILTER</code></td>
    </tr>
  </tbody>
</table>



## Observability

### alpine.metrics.auth.password

Defines the password required to access metrics.  Has no effect when [`alpine.metrics.auth.username`](#alpinemetricsauthusername) is not set.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_METRICS_AUTH_PASSWORD</code></td>
    </tr>
  </tbody>
</table>

### alpine.metrics.auth.username

Defines the username required to access metrics.  Has no effect when [`alpine.metrics.auth.password`](#alpinemetricsauthpassword) is not set.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_METRICS_AUTH_USERNAME</code></td>
    </tr>
  </tbody>
</table>

### alpine.metrics.enabled

Defines whether Prometheus metrics will be exposed.  If enabled, metrics will be available via the /metrics endpoint.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_METRICS_ENABLED</code></td>
    </tr>
  </tbody>
</table>



## OpenID Connect

### alpine.oidc.client.id

Defines the client ID to be used for OpenID Connect.  The client ID should be the same as the one configured for the frontend,  and will only be used to validate ID tokens.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_CLIENT_ID</code></td>
    </tr>
  </tbody>
</table>

### alpine.oidc.enabled

Defines if OpenID Connect will be used for user authentication.  If enabled, <code>alpine.oidc.*</code> properties should be set accordingly.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_ENABLED</code></td>
    </tr>
  </tbody>
</table>

### alpine.oidc.issuer

Defines the issuer URL to be used for OpenID Connect.  This issuer MUST support provider configuration via the <code>/.well-known/openid-configuration</code> endpoint.  See also:  <ul>  <li>https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata</li>  <li>https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig</li>  </ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_ISSUER</code></td>
    </tr>
  </tbody>
</table>

### alpine.oidc.team.synchronization

This option will ensure that team memberships for OpenID Connect users are dynamic and  synchronized with membership of OpenID Connect groups or assigned roles. When a team is  mapped to an OpenID Connect group, all local OpenID Connect users will automatically be  assigned to the team if they are a member of the group the team is mapped to. If the user  is later removed from the OpenID Connect group, they will also be removed from the team. This  option provides the ability to dynamically control user permissions via the identity provider.  Note that team synchronization is only performed during user provisioning and after successful  authentication.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_TEAM_SYNCHRONIZATION</code></td>
    </tr>
  </tbody>
</table>

### alpine.oidc.teams.claim

Defines the name of the claim that contains group memberships or role assignments in the provider's userinfo endpoint.  The claim must be an array of strings. Most public identity providers do not support group or role management.  When using a customizable / on-demand hosted identity provider, name, content, and inclusion in the userinfo endpoint  will most likely need to be configured.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>groups</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_TEAMS_CLAIM</code></td>
    </tr>
  </tbody>
</table>

### alpine.oidc.user.provisioning

Specifies if mapped OpenID Connect accounts are automatically created upon successful  authentication. When a user logs in with a valid access token but an account has  not been previously provisioned, an authentication failure will be returned.  This allows admins to control specifically which OpenID Connect users can access the  system and which users cannot. When this value is set to true, a local OpenID Connect  user will be created and mapped to the OpenID Connect account automatically. This  automatic provisioning only affects authentication, not authorization.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_USER_PROVISIONING</code></td>
    </tr>
  </tbody>
</table>

### alpine.oidc.username.claim

Defines the name of the claim that contains the username in the provider's userinfo endpoint.  Common claims are <code>name</code>, <code>username</code>, <code>preferred_username</code> or <code>nickname</code>.  See also:  <ul>  <li>https://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponse</li>  </ul>  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>name</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_OIDC_USERNAME_CLAIM</code></td>
    </tr>
  </tbody>
</table>



## Task Execution

### alpine.worker.thread.multiplier

Defines a multiplier that is used to calculate the number of threads used  by the event subsystem. This property is only used when [`alpine.worker.threads`](#alpineworkerthreads)  is set to 0. A machine with 4 cores and a multiplier of 4, will use (at most)  16 worker threads.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>4</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_WORKER_THREAD_MULTIPLIER</code></td>
    </tr>
  </tbody>
</table>

### alpine.worker.threads

Defines the number of worker threads that the event subsystem will consume.  Events occur asynchronously and are processed by the Event subsystem. This  value should be large enough to handle most production situations without  introducing much delay, yet small enough not to pose additional load on an  already resource-constrained server.  A value of 0 will instruct Alpine to allocate 1 thread per CPU core. This  can further be tweaked using the [`alpine.worker.thread.multiplier`](#alpineworkerthreadmultiplier) property.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_WORKER_THREADS</code></td>
    </tr>
  </tbody>
</table>



## Task Scheduling

### integrityMetaInitializer.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>INTEGRITYMETAINITIALIZER_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### integrityMetaInitializer.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>INTEGRITYMETAINITIALIZER_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.componentIdentification.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_COMPONENTIDENTIFICATION_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.componentIdentification.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_COMPONENTIDENTIFICATION_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.cron.componentIdentification

Schedule task every 6 hrs at 25th min  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>25 */6 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_COMPONENTIDENTIFICATION</code></td>
    </tr>
  </tbody>
</table>

### task.cron.defectdojo.sync

Schedule task every 24 hrs at 02:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 2 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_DEFECTDOJO_SYNC</code></td>
    </tr>
  </tbody>
</table>

### task.cron.fortify.ssc.sync

Schedule task every 24 hrs at 02:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 2 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_FORTIFY_SSC_SYNC</code></td>
    </tr>
  </tbody>
</table>

### task.cron.integrityInitializer

Schedule task at 0 min past every 12th hr  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 */12 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_INTEGRITYINITIALIZER</code></td>
    </tr>
  </tbody>
</table>

### task.cron.kenna.sync

Schedule task every 24 hrs at 02:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 2 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_KENNA_SYNC</code></td>
    </tr>
  </tbody>
</table>

### task.cron.ldapSync

Schedule task every 6 hrs at 0th min  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 */6 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_LDAPSYNC</code></td>
    </tr>
  </tbody>
</table>

### task.cron.metrics.portfolio

Schedule task for 10th minute of every hour  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>10 * * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_METRICS_PORTFOLIO</code></td>
    </tr>
  </tbody>
</table>

### task.cron.metrics.vulnerability

Schedule task for 40th minute of every hour  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>40 * * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_METRICS_VULNERABILITY</code></td>
    </tr>
  </tbody>
</table>

### task.cron.mirror.github

Schedule task every 24 hrs at 02:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 2 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_MIRROR_GITHUB</code></td>
    </tr>
  </tbody>
</table>

### task.cron.mirror.nist

Schedule task every 24 hrs at 04:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 4 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_MIRROR_NIST</code></td>
    </tr>
  </tbody>
</table>

### task.cron.mirror.osv

Schedule task every 24 hrs at 03:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 3 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_MIRROR_OSV</code></td>
    </tr>
  </tbody>
</table>

### task.cron.repoMetaAnalysis

Schedule task every 24 hrs at 01:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 1 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_REPOMETAANALYSIS</code></td>
    </tr>
  </tbody>
</table>

### task.cron.vulnAnalysis

Schedule task every 24hrs at 06:00 UTC  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>0 6 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_VULNANALYSIS</code></td>
    </tr>
  </tbody>
</table>

### task.cron.vulnScanCleanUp

Schedule task at 8:05 UTC on Wednesday every week  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5 8 * * 4</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_VULNSCANCLEANUP</code></td>
    </tr>
  </tbody>
</table>

### task.cron.vulnerability.policy.bundle.fetch

Schedule task every 5 minutes  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>*/5 * * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_VULNERABILITY_POLICY_BUNDLE_FETCH</code></td>
    </tr>
  </tbody>
</table>

### task.cron.workflow.state.cleanup

Schedule task every 15 minutes  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>cron</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>*/15 * * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_CRON_WORKFLOW_STATE_CLEANUP</code></td>
    </tr>
  </tbody>
</table>

### task.ldapSync.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_LDAPSYNC_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.ldapSync.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_LDAPSYNC_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.metrics.portfolio.lockAtLeastForInMillis

Specifies minimum amount of time for which the lock should be kept.  Its main purpose is to prevent execution from multiple nodes in case of really short tasks and clock difference between the nodes.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_PORTFOLIO_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.metrics.portfolio.lockAtMostForInMillis

Specifies how long the lock should be kept in case the executing node dies.  This is just a fallback, under normal circumstances the lock is released as soon the tasks finishes.  Set lockAtMostFor to a value which is much longer than normal execution time. Default value is 15min  Lock will be extended dynamically till task execution is finished  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_PORTFOLIO_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.metrics.vulnerability.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_VULNERABILITY_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.metrics.vulnerability.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_VULNERABILITY_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.mirror.epss.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_MIRROR_EPSS_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.mirror.epss.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_MIRROR_EPSS_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.portfolio.repoMetaAnalysis.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_PORTFOLIO_REPOMETAANALYSIS_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.portfolio.repoMetaAnalysis.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_PORTFOLIO_REPOMETAANALYSIS_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.portfolio.vulnAnalysis.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>90000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_PORTFOLIO_VULNANALYSIS_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.portfolio.vulnAnalysis.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_PORTFOLIO_VULNANALYSIS_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.scheduler.initial.delay

Scheduling tasks after 3 minutes (3*60*1000) of starting application  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>180000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_SCHEDULER_INITIAL_DELAY</code></td>
    </tr>
  </tbody>
</table>

### task.scheduler.polling.interval

Cron expressions for tasks have the precision of minutes so polling every minute  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>60000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_SCHEDULER_POLLING_INTERVAL</code></td>
    </tr>
  </tbody>
</table>

### task.workflow.state.cleanup.lockAtLeastForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_WORKFLOW_STATE_CLEANUP_LOCKATLEASTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>

### task.workflow.state.cleanup.lockAtMostForInMillis



<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>900000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_WORKFLOW_STATE_CLEANUP_LOCKATMOSTFORINMILLIS</code></td>
    </tr>
  </tbody>
</table>


