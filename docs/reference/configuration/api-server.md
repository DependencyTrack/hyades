<!--
  GENERATED. DO NOT EDIT.

  Generated with: --template ./scripts/config-docs.md.peb --output ./docs/reference/configuration/api-server.md ./hyades-apiserver/src/main/resources/application.properties
-->

## CORS

### alpine.cors.allow.credentials

Controls the content of the `Access-Control-Allow-Credentials` response header.  <br/>  Has no effect when [`alpine.cors.enabled`](#alpinecorsenabled) is `false`.  

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
      <td style="border-width: 0"><code>ALPINE_CORS_ALLOW_CREDENTIALS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.cors.allow.headers

Controls the content of the `Access-Control-Allow-Headers` response header.  <br/>  Has no effect when [`alpine.cors.enabled`](#alpinecorsenabled) is `false`.  

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
      <td style="border-width: 0"><code>Origin, Content-Type, Authorization, X-Requested-With, Content-Length, Accept, Origin, X-Api-Key, X-Total-Count, *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_CORS_ALLOW_HEADERS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.cors.allow.methods

Controls the content of the `Access-Control-Allow-Methods` response header.  <br/>  Has no effect when [`alpine.cors.enabled`](#alpinecorsenabled) is `false`.  

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
      <td style="border-width: 0"><code>GET POST PUT DELETE OPTIONS</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_CORS_ALLOW_METHODS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.cors.allow.origin

Controls the content of the `Access-Control-Allow-Origin` response header.  <br/>  Has no effect when [`alpine.cors.enabled`](#alpinecorsenabled) is `false`.  

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
      <td style="border-width: 0"><code>*</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_CORS_ALLOW_ORIGIN</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.cors.enabled

Defines whether [Cross Origin Resource Sharing](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)  (CORS) headers shall be included in REST API responses.  

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
      <td style="border-width: 0"><code>ALPINE_CORS_ENABLED</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.cors.expose.headers

Controls the content of the `Access-Control-Expose-Headers` response header.  <br/>  Has no effect when [`alpine.cors.enabled`](#alpinecorsenabled) is `false`.  

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
      <td style="border-width: 0"><code>Origin, Content-Type, Authorization, X-Requested-With, Content-Length, Accept, Origin, X-Api-Key, X-Total-Count</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_CORS_EXPOSE_HEADERS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.cors.max.age

Controls the content of the `Access-Control-Max-Age` response header.  <br/>  Has no effect when [`alpine.cors.enabled`](#alpinecorsenabled) is `false`.  

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
      <td style="border-width: 0"><code>3600</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_CORS_MAX_AGE</code></td>
    </tr>
  </tbody>
</table>




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


---

### alpine.database.password.file

Specifies the file to load the database password from.  If set, takes precedence over [`alpine.database.password`](#alpinedatabasepassword).  

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
      <td style="border-width: 0"><code>/var/run/secrets/database-password</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_DATABASE_PASSWORD_FILE</code></td>
    </tr>
  </tbody>
</table>


---

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


---

### alpine.database.pool.idle.timeout

This property controls the maximum amount of time that a connection is  allowed to sit idle in the pool.  

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


---

### alpine.database.pool.max.lifetime

This property controls the maximum lifetime of a connection in the pool.  An in-use connection will never be retired, only when it is closed will  it then be removed.  

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


---

### alpine.database.pool.max.size

This property controls the maximum size that the pool is allowed to reach,  including both idle and in-use connections.  

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


---

### alpine.database.pool.min.idle

This property controls the minimum number of idle connections in the pool.  This value should be equal to or less than [`alpine.database.pool.max.size`](#alpinedatabasepoolmaxsize).  Warning: If the value is less than [`alpine.database.pool.max.size`](#alpinedatabasepoolmaxsize),  [`alpine.database.pool.idle.timeout`](#alpinedatabasepoolidletimeout) will have no effect.  

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


---

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


---

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


---

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


---

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


---

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


---

### database.run.migrations

Defines whether database migrations should be executed on startup.  <br/><br/>  From v5.6.0 onwards, migrations are considered part of the initialization tasks.  Setting [`init.tasks.enabled`](#inittasksenabled) to `false` will disable migrations,  even if [`database.run.migrations`](#databaserunmigrations) is enabled.  

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
      <td style="border-width: 0"><code>DATABASE_RUN_MIGRATIONS</code></td>
    </tr>
  </tbody>
</table>


---

### database.run.migrations.only

Defines whether the application should exit upon successful execution of database migrations.  Enabling this option makes the application suitable for running as k8s init container.  Has no effect unless [`database.run.migrations`](#databaserunmigrations) is `true`.  <br/><br/>  From v5.6.0 onwards, usage of [`init.and.exit`](#initandexit) should be preferred.  

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
      <td style="border-width: 0"><code>DATABASE_RUN_MIGRATIONS_ONLY</code></td>
    </tr>
  </tbody>
</table>




## Development

### dev.services.enabled

Whether dev services shall be enabled.  <br/><br/>  When enabled, Dependency-Track will automatically launch containers for:  <ul>  <li>Frontend</li>  <li>Kafka</li>  <li>PostgreSQL</li>  </ul>  at startup, and configures itself to use them. They are disposed when  Dependency-Track stops. The containers are exposed on randomized ports,  which will be logged during startup.  <br/><br/>  Trying to enable dev services in a production build will prevent  the application from starting.  <br/><br/>  Note that the containers launched by the API server can not currently  be discovered and re-used by other Hyades services. This is a future  enhancement tracked in <https://github.com/DependencyTrack/hyades/issues/1188>.  

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
      <td style="border-width: 0"><code>DEV_SERVICES_ENABLED</code></td>
    </tr>
  </tbody>
</table>


---

### dev.services.image.frontend

The image to use for the frontend dev services container.  

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
      <td style="border-width: 0"><code>ghcr.io/dependencytrack/hyades-frontend:snapshot</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DEV_SERVICES_IMAGE_FRONTEND</code></td>
    </tr>
  </tbody>
</table>


---

### dev.services.image.kafka

The image to use for the Kafka dev services container.  

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
      <td style="border-width: 0"><code>docker.redpanda.com/vectorized/redpanda:v24.2.4</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DEV_SERVICES_IMAGE_KAFKA</code></td>
    </tr>
  </tbody>
</table>


---

### dev.services.image.postgres

The image to use for the PostgreSQL dev services container.  

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
      <td style="border-width: 0"><code>postgres:16</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DEV_SERVICES_IMAGE_POSTGRES</code></td>
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


---

### alpine.auth.jwt.ttl.seconds

Defines the number of seconds for which JWTs issued by Dependency-Track will be valid for.  

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
      <td style="border-width: 0"><code>604800</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_AUTH_JWT_TTL_SECONDS</code></td>
    </tr>
  </tbody>
</table>


---

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


---

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


---

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


---

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


---

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


---

### init.and.exit

Whether to only execute initialization tasks and exit.  

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
      <td style="border-width: 0"><code>INIT_AND_EXIT</code></td>
    </tr>
  </tbody>
</table>


---

### init.tasks.enabled

Whether to execute initialization tasks on startup.  Initialization tasks include:  <ul>  <li>Execution of database migrations</li>  <li>Populating the database with default objects (permissions, users, licenses, etc.)</li>  </ul>  

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
      <td style="border-width: 0"><code>INIT_TASKS_ENABLED</code></td>
    </tr>
  </tbody>
</table>


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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




## HTTP

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


---

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


---

### alpine.http.proxy.password.file

Specifies the file to load the HTTP proxy password from.  If set, takes precedence over [`alpine.http.proxy.password`](#alpinehttpproxypassword).  

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
      <td style="border-width: 0"><code>/var/run/secrets/http-proxy-password</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_PROXY_PASSWORD_FILE</code></td>
    </tr>
  </tbody>
</table>


---

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


---

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


---

### alpine.http.timeout.connection

Defines the connection timeout in seconds for outbound HTTP connections.  

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
      <td style="border-width: 0"><code>30</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_TIMEOUT_CONNECTION</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.http.timeout.pool

Defines the request timeout in seconds for outbound HTTP connections.  

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
      <td style="border-width: 0"><code>60</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_TIMEOUT_POOL</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.http.timeout.socket

Defines the socket / read timeout in seconds for outbound HTTP connections.  

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
      <td style="border-width: 0"><code>30</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_HTTP_TIMEOUT_SOCKET</code></td>
    </tr>
  </tbody>
</table>


---

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

### alpine.kafka.processor.epss.mirror.consumer.auto.offset.reset



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_CONSUMER_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.consumer.group.id



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_CONSUMER_GROUP_ID</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.max.batch.size



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
      <td style="border-width: 0"><code>500</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_MAX_BATCH_SIZE</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.max.concurrency



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_MAX_CONCURRENCY</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.processing.order



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_PROCESSING_ORDER</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.retry.initial.delay.ms



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_RETRY_INITIAL_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.retry.max.delay.ms



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_RETRY_MAX_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.retry.multiplier



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_RETRY_MULTIPLIER</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.epss.mirror.retry.randomization.factor



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_EPSS_MIRROR_RETRY_RANDOMIZATION_FACTOR</code></td>
    </tr>
  </tbody>
</table>


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

### alpine.kafka.processor.vuln.scan.result.consumer.auto.offset.reset



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_CONSUMER_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.consumer.group.id



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_CONSUMER_GROUP_ID</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.max.concurrency



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_MAX_CONCURRENCY</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.consumer.auto.offset.reset



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_CONSUMER_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.consumer.fetch.min.bytes



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
      <td style="border-width: 0"><code>524288</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_CONSUMER_FETCH_MIN_BYTES</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.consumer.group.id



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_CONSUMER_GROUP_ID</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.consumer.max.poll.records



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
      <td style="border-width: 0"><code>10000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_CONSUMER_MAX_POLL_RECORDS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.max.batch.size



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_MAX_BATCH_SIZE</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.max.concurrency



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
      <td style="border-width: 0"><code>1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_MAX_CONCURRENCY</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.processing.order



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
      <td style="border-width: 0"><code>unordered</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_PROCESSING_ORDER</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.retry.initial.delay.ms



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_RETRY_INITIAL_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.retry.max.delay.ms



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_RETRY_MAX_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.retry.multiplier



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_RETRY_MULTIPLIER</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processed.retry.randomization.factor



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSED_RETRY_RANDOMIZATION_FACTOR</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.processing.order



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_PROCESSING_ORDER</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.retry.initial.delay.ms



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_RETRY_INITIAL_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.retry.max.delay.ms



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_RETRY_MAX_DELAY_MS</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.retry.multiplier



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_RETRY_MULTIPLIER</code></td>
    </tr>
  </tbody>
</table>


---

### alpine.kafka.processor.vuln.scan.result.retry.randomization.factor



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
      <td style="border-width: 0"><code>ALPINE_KAFKA_PROCESSOR_VULN_SCAN_RESULT_RETRY_RANDOMIZATION_FACTOR</code></td>
    </tr>
  </tbody>
</table>


---

### dt.kafka.topic.prefix



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
      <td style="border-width: 0"><code>DT_KAFKA_TOPIC_PREFIX</code></td>
    </tr>
  </tbody>
</table>


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

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


---

### alpine.ldap.enabled

Defines if LDAP will be used for user authentication. If enabled,  `alpine.ldap.*` properties should be set accordingly.  

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


---

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


---

### alpine.ldap.groups.search.filter

Specifies the LDAP search filter used to search for groups by their name.  The `{SEARCH_TERM}` variable will be substituted at runtime.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>(&(objectClass=group)(objectCategory=Group)(cn=*{SEARCH_TERM}*))</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>(&(objectClass=groupOfUniqueNames)(cn=*{SEARCH_TERM}*))</code></li></ul>  

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


---

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


---

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


---

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


---

### alpine.ldap.user.groups.filter

Specifies the LDAP search filter to use to query a user and retrieve a list  of groups the user is a member of. The `{USER_DN}` variable will be substituted  with the actual value of the users DN at runtime.  <br/><br/>  Example (Microsoft Active Directory):  <ul><li><code>(&(objectClass=group)(objectCategory=Group)(member={USER_DN}))</code></li></ul>  Example (Microsoft Active Directory - with nested group support):  <ul><li><code>(member:1.2.840.113556.1.4.1941:={USER_DN})</code></li></ul>  Example (ApacheDS, Fedora 389 Directory, NetIQ/Novell eDirectory, etc):  <ul><li><code>(&(objectClass=groupOfUniqueNames)(uniqueMember={USER_DN}))</code></li></ul>  

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


---

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


---

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


---

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


---

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


---

### alpine.oidc.enabled

Defines if OpenID Connect will be used for user authentication.  If enabled, `alpine.oidc.*` properties should be set accordingly.  

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


---

### alpine.oidc.issuer

Defines the issuer URL to be used for OpenID Connect.  This issuer MUST support provider configuration via the `/.well-known/openid-configuration` endpoint.  See also:  <ul>  <li>https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata</li>  <li>https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig</li>  </ul>  

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


---

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


---

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


---

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


---

### alpine.oidc.username.claim

Defines the name of the claim that contains the username in the provider's userinfo endpoint.  Common claims are `name`, `username`, `preferred_username` or `nickname`.  See also:  <ul>  <li>https://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponse</li>  </ul>  

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


---

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

### task.component.metadata.maintenance.cron

Cron expression of the component metadata maintenance task.  <br/><br/>  The task deletes orphaned records from the `INTEGRITY_META_COMPONENT` and  `REPOSITORY_META_COMPONENT` tables.  

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
      <td style="border-width: 0"><code>TASK_COMPONENT_METADATA_MAINTENANCE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.component.metadata.maintenance.lock.max.duration

Maximum duration in ISO 8601 format for which the component metadata maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_COMPONENT_METADATA_MAINTENANCE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.component.metadata.maintenance.lock.min.duration

Minimum duration in ISO 8601 format for which the component metadata maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_COMPONENT_METADATA_MAINTENANCE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.defect.dojo.upload.cron

Cron expression of the DefectDojo upload task.  

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
      <td style="border-width: 0"><code>TASK_DEFECT_DOJO_UPLOAD_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.epss.mirror.cron

Cron expression of the EPSS mirroring task.  

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
      <td style="border-width: 0"><code>TASK_EPSS_MIRROR_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.fortify.ssc.upload.cron

Cron expression of the Fortify SSC upload task.  

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
      <td style="border-width: 0"><code>TASK_FORTIFY_SSC_UPLOAD_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.git.hub.advisory.mirror.cron

Cron expression of the vulnerability GitHub Advisories mirroring task.  

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
      <td style="border-width: 0"><code>TASK_GIT_HUB_ADVISORY_MIRROR_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.integrity.meta.initializer.cron

Cron expression of the integrity metadata initializer task.  

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
      <td style="border-width: 0"><code>TASK_INTEGRITY_META_INITIALIZER_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.integrity.meta.initializer.lock.max.duration

Maximum duration in ISO 8601 format for which the integrity metadata initializer task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_INTEGRITY_META_INITIALIZER_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.integrity.meta.initializer.lock.min.duration

Minimum duration in ISO 8601 format for which the integrity metadata initializer task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_INTEGRITY_META_INITIALIZER_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.internal.component.identification.cron

Cron expression of the internal component identification task.  

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
      <td style="border-width: 0"><code>TASK_INTERNAL_COMPONENT_IDENTIFICATION_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.internal.component.identification.lock.max.duration

Maximum duration in ISO 8601 format for which the internal component identification task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_INTERNAL_COMPONENT_IDENTIFICATION_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.internal.component.identification.lock.min.duration

Minimum duration in ISO 8601 format for which the internal component identification task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_INTERNAL_COMPONENT_IDENTIFICATION_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.kenna.security.upload.cron

Cron expression of the Kenna Security upload task.  

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
      <td style="border-width: 0"><code>TASK_KENNA_SECURITY_UPLOAD_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.ldap.sync.cron

Cron expression of the LDAP synchronization task.  

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
      <td style="border-width: 0"><code>TASK_LDAP_SYNC_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.ldap.sync.lock.max.duration

Maximum duration in ISO 8601 format for which the LDAP synchronization task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_LDAP_SYNC_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.ldap.sync.lock.min.duration

Minimum duration in ISO 8601 format for which the LDAP synchronization task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_LDAP_SYNC_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.metrics.maintenance.cron

Cron expression of the metrics maintenance task.  <br/><br/>  The task deletes records older than the configured metrics retention duration from the following tables:  <ul>  <li><code>DEPENDENCYMETRICS</code></li>  <li><code>PROJECTMETRICS</code></li>  <li><code>PORTFOLIOMETRICS</code></li>  </ul>  

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
      <td style="border-width: 0"><code>0 */3 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_MAINTENANCE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.metrics.maintenance.lock.max.duration

Maximum duration in ISO 8601 format for which the metrics maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_MAINTENANCE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.metrics.maintenance.lock.min.duration

Minimum duration in ISO 8601 format for which the metrics maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_METRICS_MAINTENANCE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.nist.mirror.cron

Cron expression of the NIST / NVD mirroring task.  

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
      <td style="border-width: 0"><code>TASK_NIST_MIRROR_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.osv.mirror.cron

Cron expression of the OSV mirroring task.  

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
      <td style="border-width: 0"><code>TASK_OSV_MIRROR_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.portfolio.metrics.update.cron

Cron expression of the portfolio metrics update task.  

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
      <td style="border-width: 0"><code>TASK_PORTFOLIO_METRICS_UPDATE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.portfolio.metrics.update.lock.max.duration

Maximum duration in ISO 8601 format for which the portfolio metrics update task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_PORTFOLIO_METRICS_UPDATE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.portfolio.metrics.update.lock.min.duration

Minimum duration in ISO 8601 format for which the portfolio metrics update task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_PORTFOLIO_METRICS_UPDATE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.repository.meta.analysis.cron

Cron expression of the portfolio repository metadata analysis task.  

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
      <td style="border-width: 0"><code>TASK_REPOSITORY_META_ANALYSIS_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.repository.meta.analysis.lock.max.duration

Maximum duration in ISO 8601 format for which the portfolio repository metadata analysis task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_REPOSITORY_META_ANALYSIS_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.repository.meta.analysis.lock.min.duration

Minimum duration in ISO 8601 format for which the portfolio repository metadata analysis task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_REPOSITORY_META_ANALYSIS_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

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


---

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


---

### task.tag.maintenance.cron

Cron expression of the tag maintenance task.  <br/><br/>  The task deletes orphaned tags that are not used anymore.  

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
      <td style="border-width: 0"><code>TASK_TAG_MAINTENANCE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.tag.maintenance.lock.max.duration

Maximum duration in ISO 8601 format for which the tag maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_TAG_MAINTENANCE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.tag.maintenance.lock.min.duration

Minimum duration in ISO 8601 format for which the tag maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_TAG_MAINTENANCE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.analysis.cron

Cron expression of the portfolio vulnerability analysis task.  

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
      <td style="border-width: 0"><code>TASK_VULNERABILITY_ANALYSIS_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.analysis.lock.max.duration

Maximum duration in ISO 8601 format for which the portfolio vulnerability analysis task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_ANALYSIS_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.analysis.lock.min.duration

Minimum duration in ISO 8601 format for which the portfolio vulnerability analysis task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_ANALYSIS_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.database.maintenance.cron

Cron expression of the vulnerability database maintenance task.  <br/><br/>  The task deletes orphaned records from the `VULNERABLESOFTWARE` table.  

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
      <td style="border-width: 0"><code>0 0 * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_DATABASE_MAINTENANCE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.database.maintenance.lock.max.duration

Maximum duration in ISO 8601 format for which the vulnerability database maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_DATABASE_MAINTENANCE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.database.maintenance.lock.min.duration

Minimum duration in ISO 8601 format for which the vulnerability database maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_DATABASE_MAINTENANCE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.metrics.update.cron

Cron expression of the vulnerability metrics update task.  

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
      <td style="border-width: 0"><code>TASK_VULNERABILITY_METRICS_UPDATE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.metrics.update.lock.max.duration

Maximum duration in ISO 8601 format for which the vulnerability metrics update task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_METRICS_UPDATE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.metrics.update.lock.min.duration

Minimum duration in ISO 8601 format for which the vulnerability metrics update task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT90S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_METRICS_UPDATE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.policy.fetch.cron

Cron expression of the vulnerability policy bundle fetch task.  

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
      <td style="border-width: 0"><code>TASK_VULNERABILITY_POLICY_FETCH_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.policy.fetch.lock.max.duration

Maximum duration in ISO 8601 format for which the vulnerability policy bundle fetch task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT5M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_POLICY_FETCH_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.policy.fetch.lock.min.duration

Minimum duration in ISO 8601 format for which the vulnerability policy bundle fetch task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT5S</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_POLICY_FETCH_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.scan.maintenance.cron

Cron expression of the vulnerability scan maintenance task.  <br/><br/>  The task deletes records older than the configured retention duration from the `VULNERABILITYSCAN` table.  

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
      <td style="border-width: 0"><code>0 * * * *</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_SCAN_MAINTENANCE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.scan.maintenance.lock.max.duration

Maximum duration in ISO 8601 format for which the vulnerability database maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT15M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_SCAN_MAINTENANCE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.vulnerability.scan.maintenance.lock.min.duration

Minimum duration in ISO 8601 format for which the vulnerability database maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_VULNERABILITY_SCAN_MAINTENANCE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.workflow.maintenance.cron

Cron expression of the workflow maintenance task.  <br/><br/>  The task:  <ul>  <li>Transitions workflow steps from <code>PENDING</code> to <code>TIMED_OUT</code> state</li>  <li>Transitions workflow steps from <code>TIMED_OUT</code> to <code>FAILED</code> state</li>  <li>Transitions children of <code>FAILED</code> steps to <code>CANCELLED</code> state</li>  <li>Deletes finished workflows according to the configured retention duration</li>  </ul>  

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
      <td style="border-width: 0"><code>TASK_WORKFLOW_MAINTENANCE_CRON</code></td>
    </tr>
  </tbody>
</table>


---

### task.workflow.maintenance.lock.max.duration

Maximum duration in ISO 8601 format for which the workflow maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover the task's execution duration.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT5M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_WORKFLOW_MAINTENANCE_LOCK_MAX_DURATION</code></td>
    </tr>
  </tbody>
</table>


---

### task.workflow.maintenance.lock.min.duration

Minimum duration in ISO 8601 format for which the workflow maintenance task will hold a lock.  <br/><br/>  The duration should be long enough to cover eventual clock skew across API server instances.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT1M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>TASK_WORKFLOW_MAINTENANCE_LOCK_MIN_DURATION</code></td>
    </tr>
  </tbody>
</table>




