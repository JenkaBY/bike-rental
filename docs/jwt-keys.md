# JWT Signing Keys & Rotation

The authorization server signs JWT **access tokens** and **ID tokens** with an asymmetric **RSA key pair (RS256)**: the
private key signs, the public key verifies. The public key is published at `/oauth2/jwks` so resource servers and SPAs
can verify tokens without ever seeing the private key. Refresh tokens are **opaque random strings** (stored in the DB),
not JWTs — they are not signed by this key.

## Why persistent keys matter

If no key locations are configured, the server **generates a fresh key pair on every startup**. That invalidates every
previously issued token after a restart (you will see logout/verification failures such as `invalid_token`). For any
environment that survives restarts — or runs more than one instance — provide a **persistent, shared** key pair.

Configuration (`app.security.jwt`, bound from env):

| Property                    | Env var                     | Meaning                                              |
|-----------------------------|-----------------------------|------------------------------------------------------|
| `key-id`                    | `JWT_KEY_ID`                | `kid` of the active signing key (published in JWKS)  |
| `private-key-location`      | `JWT_PRIVATE_KEY_LOCATION`  | PKCS#8 PEM, Spring `Resource` URI (`file:` / `classpath:`) |
| `public-key-location`       | `JWT_PUBLIC_KEY_LOCATION`   | X.509 (SubjectPublicKeyInfo) PEM                     |
| `previous-keys[n].key-id`   | `APP_SECURITY_JWT_PREVIOUSKEYS_n_KEYID` | `kid` of a retired key kept for verification |
| `previous-keys[n].public-key-location` | `APP_SECURITY_JWT_PREVIOUSKEYS_n_PUBLICKEYLOCATION` | retired public key PEM |

Leaving the private/public locations empty falls back to the ephemeral generated key (dev convenience only).

## Generate a key pair

`openssl` produces exactly the formats the loader expects — PKCS#8 private (`BEGIN PRIVATE KEY`) and X.509 public
(`BEGIN PUBLIC KEY`):

```bash
mkdir -p keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/jwt-private.pem
openssl rsa -in keys/jwt-private.pem -pubout -out keys/jwt-public.pem
```

Never commit the private key. `.gitignore` covers it: `*.pem` (any PEM, anywhere),
`service/src/main/resources/keys/`, and `docker/keys/`.

## Local development

Locations accept any Spring `Resource` URI — `file:` or `classpath:`. Pick one:

**`file:` (absolute path recommended).** `file:` URIs resolve relative to the **working directory**, which differs
between launchers (IntelliJ's `$MODULE_WORKING_DIR$` and `:service:bootRun` both resolve to the `service/` subproject,
not the repo root). To avoid that ambiguity, use an absolute path:

```properties
JWT_KEY_ID=bike-rental-identity
JWT_PRIVATE_KEY_LOCATION=file:/absolute/path/to/keys/jwt-private.pem
JWT_PUBLIC_KEY_LOCATION=file:/absolute/path/to/keys/jwt-public.pem
```

**`classpath:` (working-directory independent — convenient in the IDE).** Put the pair under
`service/src/main/resources/keys/` and reference it on the classpath. This resolves the same way from the IDE run
config, `:service:bootRun`, and the packaged JAR:

```properties
JWT_PRIVATE_KEY_LOCATION=classpath:keys/jwt-private.pem
JWT_PUBLIC_KEY_LOCATION=classpath:keys/jwt-public.pem
```

> **Warning — local dev only.** A `classpath:` key is **packaged into the build artifact** (fat JAR / Docker image).
> That is acceptable locally (the folder is git-ignored, so CI never sees the key and the deployed JAR won't contain
> it), but **never point a deployed environment at a `classpath:` private key**. For Docker/Render use `file:` with a
> mounted volume / Secret File so the key stays outside the artifact.

## Docker Compose

The compose file mounts `docker/keys` into the container at `/run/secrets` (read-only). Put the pair there:

```bash
mkdir -p docker/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out docker/keys/jwt-private.pem
openssl rsa -in docker/keys/jwt-private.pem -pubout -out docker/keys/jwt-public.pem
```

and keep the defaults from `docker/.env.example`:

```properties
JWT_PRIVATE_KEY_LOCATION=file:/run/secrets/jwt-private.pem
JWT_PUBLIC_KEY_LOCATION=file:/run/secrets/jwt-public.pem
```

## Render.com (dev deployment)

Render has no persistent disk on the free tier, but it has **Secret Files**, which are mounted read-only at
`/etc/secrets/<filename>` and survive restarts, cold starts, and redeploys.

1. Generate the pair locally (as above).
2. Service → **Environment → Secret Files**: add `jwt-private.pem` and `jwt-public.pem` (paste the PEM contents).
3. Service → **Environment Variables**:
   ```
   JWT_PRIVATE_KEY_LOCATION=file:/etc/secrets/jwt-private.pem
   JWT_PUBLIC_KEY_LOCATION=file:/etc/secrets/jwt-public.pem
   ```

The same secret files are mounted into every instance of the service, so verification stays consistent if you scale
out.

## Key validity & rotation

An RSA key has **no built-in expiry** — rotation is a policy choice for hygiene and to limit blast radius if a key
leaks. Recommended: rotate **every 3–6 months**, and **immediately** on suspected compromise.

**The overlap window is small.** Only access tokens (TTL 15 min) and ID tokens (~30 min) are RSA-signed; refresh tokens
are opaque and unaffected. So a rotation is **seamless — no forced re-login** — and the retired public key only needs
to stay published for ~1 hour (longer than the longest-lived signed token) before it can be dropped.

### Rotation procedure (overlap, zero downtime)

1. **Generate a new pair** with a new `kid` (e.g. `bike-rental-identity-2`):
   ```bash
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/jwt-private-2.pem
   openssl rsa -in keys/jwt-private-2.pem -pubout -out keys/jwt-public-2.pem
   ```
2. **Promote the new key, keep the old public key for verification.** Update the secret files/env so the *active* key is
   the new pair, and the *previous* (now retired) public key is exposed:
   ```
   JWT_KEY_ID=bike-rental-identity-2
   JWT_PRIVATE_KEY_LOCATION=file:/etc/secrets/jwt-private-2.pem
   JWT_PUBLIC_KEY_LOCATION=file:/etc/secrets/jwt-public-2.pem
   APP_SECURITY_JWT_PREVIOUSKEYS_0_KEYID=bike-rental-identity
   APP_SECURITY_JWT_PREVIOUSKEYS_0_PUBLICKEYLOCATION=file:/etc/secrets/jwt-public.pem
   ```
3. **Deploy.** New tokens are signed with `bike-rental-identity-2`; `/oauth2/jwks` now publishes both keys, so tokens
   signed by either `kid` still verify.
4. **Wait the overlap window (~1 hour).** All tokens minted before the switch have expired.
5. **Remove the retired key** — delete the `APP_SECURITY_JWT_PREVIOUSKEYS_0_*` vars and the old secret file, then
   deploy. Rotation is complete.

The active key is always the only one carrying a private part, so the encoder signs with it deterministically (it stamps
the active `kid` into each token's JWS header); the `previous-keys` entries are verification-only.

### Production note

For a real production cluster, back the keys with a secrets manager / KMS / Vault and automate rotation (e.g. a
DB- or Vault-backed key store with `active`/`retired` status that the `JWKSource` reads). Spring Authorization Server
does not ship automatic rotation — the manual overlap procedure above is the dev/staging baseline.
