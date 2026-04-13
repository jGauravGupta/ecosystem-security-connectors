# OpenID Connect — Proxy and Reverse Proxy Setup

This guide explains how to configure the Payara OpenID Connect security connector when your application server is deployed behind a forward proxy or a reverse proxy.

## Overview

When Payara is running behind a proxy, the **redirect URI** registered with the OIDC provider refers to the proxy's public hostname and port, not the application server's internal address.
By default the connector builds the redirect URI from the incoming `HttpServletRequest`, which will reflect the internal server address.
To bridge this gap the connector provides `ProxyDefinition`, which tells the connector to use the proxy's hostname and port when constructing and validating the redirect URI.

## Configuration

### 1. Annotation — `@ProxyDefinition` inside `@OpenIdAuthenticationDefinition`

The `proxyDefinition` attribute of `@OpenIdAuthenticationDefinition` accepts a `@ProxyDefinition` annotation that sets the public proxy hostname and port.

```java
@OpenIdAuthenticationDefinition(
    providerURI    = "https://idp.example.com/",
    clientId       = "${oidc.clientId}",
    clientSecret   = "${oidc.clientSecret}",
    redirectURI    = "https://proxy.example.com/myapp/oidc/callback",
    proxyDefinition = @ProxyDefinition(
        hostName = "proxy.example.com",
        port     = "443"
    )
)
@ApplicationScoped
public class SecurityConfig {}
```

| `@ProxyDefinition` attribute | Description |
|---|---|
| `hostName` | Public hostname of the reverse proxy (e.g. `proxy.example.com`) |
| `port`     | Public port of the reverse proxy (e.g. `443`, `8082`). Leave empty to omit the port from the constructed URL |

### 2. MicroProfile Config (`microprofile-config.properties`)

All proxy settings can be supplied through MicroProfile Config instead of (or to override) the annotation:

```properties
# Public proxy hostname
payara.security.openid.proxyHostname=proxy.example.com

# Public proxy port (optional – omit or leave blank to not include a port)
payara.security.openid.proxyPort=443
```

These properties are resolved with the same precedence rules as all other `payara.security.openid.*` properties (annotation → MP Config → system property).

---

## How It Works

When a proxy hostname is configured and the raw `request.getRequestURL()` does not match `redirectURI`, the connector reconstructs the URL using the proxy host and port:

```
<scheme>://<proxyHostName>[:<proxyPort>]<requestURI>
```

This reconstructed URL is then compared against the configured `redirectURI`.
If they match, the OAuth 2.0 callback is accepted even though the raw server URL is different.

---

## Examples

### Example 1 — NGINX Reverse Proxy (HTTP)

**Scenario:** Payara listens on `http://127.0.0.1:8081` and NGINX exposes it as `http://proxy.local:8082`.

#### NGINX configuration

```nginx
server {
    listen 8082;
    server_name proxy.local;

    location / {
        proxy_pass         http://127.0.0.1:8081;
        proxy_http_version 1.1;

        proxy_set_header Host              $host;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host  $host:$server_port;
        proxy_set_header X-Forwarded-Port  $server_port;
    }
}
```

#### OIDC provider callback / logout URLs

Register these exact URLs in your OIDC provider (e.g. Keycloak, Auth0):

* **Callback URL:** `http://proxy.local:8082/myapp/oidc/callback`
* **Logout URL:** `http://proxy.local:8082/myapp`

#### Application security bean

```java
@OpenIdAuthenticationDefinition(
    providerURI     = "https://idp.example.com/",
    clientId        = "my-client-id",
    clientSecret    = "my-client-secret",
    redirectURI     = "http://proxy.local:8082/myapp/oidc/callback",
    scope           = { "openid", "profile", "email" },
    proxyDefinition = @ProxyDefinition(
        hostName = "proxy.local",
        port     = "8082"
    )
)
@ApplicationScoped
public class SecurityConfig {}
```

#### `microprofile-config.properties` (alternative to annotation)

```properties
payara.security.openid.proxyHostname=proxy.local
payara.security.openid.proxyPort=8082
```

---

### Example 2 — NGINX Reverse Proxy (HTTPS / TLS termination)

**Scenario:** NGINX terminates TLS on port 443 and forwards plain HTTP to Payara on port 8080.

#### NGINX configuration

```nginx
server {
    listen 443 ssl;
    server_name secure.example.com;

    ssl_certificate     /etc/nginx/certs/server.crt;
    ssl_certificate_key /etc/nginx/certs/server.key;

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host              $host;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Host  $host;
        proxy_set_header X-Forwarded-Port  443;
    }
}
```

#### Application security bean

```java
@OpenIdAuthenticationDefinition(
    providerURI     = "https://idp.example.com/",
    clientId        = "my-client-id",
    clientSecret    = "my-client-secret",
    redirectURI     = "https://secure.example.com/myapp/oidc/callback",
    scope           = { "openid", "profile", "email" },
    proxyDefinition = @ProxyDefinition(
        hostName = "secure.example.com",
        port     = "443"          // can be left empty for the default HTTPS port
    )
)
@ApplicationScoped
public class SecurityConfig {}
```

#### `microprofile-config.properties` (alternative to annotation)

```properties
payara.security.openid.proxyHostname=secure.example.com
payara.security.openid.proxyPort=443
```

---

### Example 3 — Azure / Google / Cloud provider shortcut annotations

`@AzureAuthenticationDefinition` and `@GoogleAuthenticationDefinition` expose the same `proxyDefinition` attribute:

```java
@AzureAuthenticationDefinition(
    tenantId     = "${azure.tenantId}",
    clientId     = "${azure.clientId}",
    clientSecret = "${azure.clientSecret}",
    redirectURI  = "https://proxy.example.com/myapp/oidc/callback",
    proxyDefinition = @ProxyDefinition(
        hostName = "proxy.example.com",
        port     = "443"
    )
)
@ApplicationScoped
public class AzureSecurityConfig {}
```

---

## Troubleshooting

### "OpenID Redirect URL … does not match with constructed proxy URL"

The INFO log message reports four values:

| Placeholder | Meaning |
|---|---|
| `{0}` — Redirect URL | The `redirectURI` configured on the annotation / MP Config |
| `{1}` — Constructed proxy URL | `<scheme>://<proxyHostName>:<proxyPort><requestURI>` built by the connector |
| `{2}` — Actual request URL | The raw URL seen by Payara (`request.getRequestURL()`) |
| `{3}:{4}` — Proxy | The configured proxy hostname and port |

**Common causes and fixes:**

| Symptom | Likely cause | Fix |
|---|---|---|
| `{0}` and `{1}` differ | `proxyDefinition` hostname/port don't match `redirectURI` | Make sure `hostName`/`port` in `@ProxyDefinition` match the host and port in `redirectURI` |
| Constructed URL uses `http` but redirect URI uses `https` | Payara receives the plain HTTP request after TLS termination | Set `X-Forwarded-Proto: https` in the proxy, or configure Payara's network listener as `https` |
| Port is missing from the constructed URL | `port` attribute is empty but `redirectURI` includes a port | Set the `port` attribute explicitly |
| Redirect URI registered in the provider doesn't match | Misconfigured provider | Re-register the exact public URL in the OIDC provider |
