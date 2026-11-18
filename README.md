# Student Management System – Full Stack Deployment Demo

A complete, working student management application built to demonstrate deploying a
full stack app with **GitHub**, **Vercel** (frontend), **Render** (backend) and
**Neon** (PostgreSQL).

---

## 1. Project overview

Users register, log in, and manage **their own** student records:

- Registration, login, logout, session refresh, and the current-user endpoint, with JWTs in **HttpOnly cookies**
- Student CRUD and search by name/email, with ownership enforced on the backend
- Dashboard with live statistics from PostgreSQL (totals, recent registrations, per-department counts)
- Responsive React UI: sidebar layout, tables that become cards on mobile, toasts, and confirmation dialogs

## 2. Architecture

```
                 HTTPS                          HTTPS (JSON + cookies)                 SSL (JDBC)
┌──────────┐  ─────────────▶ ┌───────────────────────┐ ─────────────▶ ┌───────────────────────────┐ ──────────▶ ┌──────────────────┐
│ Browser  │                  │  Vercel               │                │  Render (Docker)          │             │  Neon PostgreSQL │
│          │  ◀───────────── │  React + Vite (static)│ ◀───────────── │  Spring Boot 3 / Java 21  │ ◀────────── │  users, students │
└──────────┘   index.html,JS  └───────────────────────┘  access_token  │  /api/**  JWT, BCrypt,    │   Flyway    │  refresh_tokens  │
                                VITE_API_URL ───────────▶ refresh_token│  JPA, Flyway, HikariCP    │  migrations └──────────────────┘
                                                          (HttpOnly)   └───────────────────────────┘
```

Request flow: React → Axios (`withCredentials: true`) → Spring Security (CORS check → CSRF header
check → JWT cookie filter) → Controller → Service (ownership checks) → JPA Repository → PostgreSQL.

### Authentication design

| Cookie          | Content                          | Lifetime | Path        |
|-----------------|----------------------------------|----------|-------------|
| `access_token`  | Signed JWT (HS256)               | 15 min   | `/api`      |
| `refresh_token` | Random 256-bit value (opaque)    | 7 days   | `/api/auth` |

- Both cookies are `HttpOnly`. JavaScript never sees a token, and nothing is stored in localStorage or sessionStorage.
- Only the SHA-256 **hash** of each refresh token is stored in the database. Refresh **rotates** the token, and logout deletes it.
- When an API call returns 401, the Axios interceptor calls `/api/auth/refresh` once and retries the request.
- **CSRF:** every POST/PUT/DELETE must send `X-Requested-With`. A plain HTML form can't add that header, and the CORS policy rejects cross-origin scripts that try. Cookie settings depend on the environment:
  - Local: `SameSite=Lax`, not `Secure`, because it runs over http on localhost.
  - Production: `SameSite=None; Secure`, because Vercel and Render are different sites on HTTPS.

### Ownership

Every student row has an `owner_id`. All repository queries filter by the logged-in user's id,
so another user's student returns **404 Not Found**. That doesn't reveal whether the record exists.
Student email addresses are unique **per user**.

## 3. Technology stack

| Layer    | Technologies |
|----------|--------------|
| Frontend | React 18, Vite 5, JavaScript, React Router 6, Axios, plain CSS |
| Backend  | Java 21, Spring Boot 3.5 (Web, Data JPA, Security, Validation), JJWT, BCrypt, Maven (wrapper included) |
| Database | PostgreSQL (Neon in production), UUID primary keys, Flyway migrations, HikariCP pool |
| Testing  | JUnit 5, MockMvc, Spring Security Test, embedded PostgreSQL (zonky) |

```
student-management-system/
├── frontend/                 React app (deploy to Vercel)
│   ├── src/{components,pages,services,context,hooks,styles,utils}
│   ├── vercel.json           SPA routing fallback
│   └── .env.example
├── backend/                  Spring Boot API (deploy to Render)
│   ├── src/main/java/com/demo/student/{config,controller,dto,entity,repository,service,security,exception}
│   ├── src/main/resources/db/migration/   Flyway SQL scripts
│   ├── Dockerfile            used by Render
│   ├── mvnw / mvnw.cmd       Maven wrapper (no Maven install needed)
│   └── .env.example
├── render.yaml               optional Render Blueprint
└── README.md
```

## 4. Local setup – prerequisites

- **Java 21** (`java -version`)
- **Node.js 18+** (`node -v`)
- **PostgreSQL 14+**, installed locally. Optional: see the no-database option in section 6.
- Git

Maven isn't required, because the repo ships `mvnw` / `mvnw.cmd`.

## 5. PostgreSQL setup (local)

```sql
-- in psql or pgAdmin, connected as the postgres user
CREATE DATABASE student_management;
```

You don't need to create any tables. **Flyway** creates the schema on first startup from
`backend/src/main/resources/db/migration`:

| Script | Creates |
|--------|---------|
| `V1__create_users_table.sql` | `users` (unique email) |
| `V2__create_students_table.sql` | `students` (FK to owner, unique `(owner_id, email)`, indexes) |
| `V3__create_refresh_tokens_table.sql` | `refresh_tokens` (hashed tokens) |

Hibernate runs with `ddl-auto=validate`, so it only checks that the entities match the migrated schema.

## 6. Backend startup

```bash
cd backend
cp .env.example .env        # Windows PowerShell: Copy-Item .env.example .env
# edit .env: set DB_PASSWORD (and DB_USERNAME if not "postgres")

./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

`backend/.env` is loaded automatically and ignored by git. The API runs at `http://localhost:8080`.
To check it:

```bash
curl http://localhost:8080/api/health
# {"success":true,"message":"Service is healthy","data":{"status":"UP","database":"UP"},...}
```

**No PostgreSQL installed?** You can run the backend against a temporary embedded PostgreSQL.
The data is discarded when it stops:

```bash
./mvnw spring-boot:test-run
```

**Run the tests.** These use a real embedded PostgreSQL and run the Flyway migrations:

```bash
./mvnw test
```

## 7. Frontend startup

```bash
cd frontend
cp .env.example .env        # VITE_API_URL=http://localhost:8080
npm install
npm run dev                 # http://localhost:5173
```

Production build: `npm run build`, which writes to `frontend/dist`.

## 8. Environment variables

### Backend (Render / `backend/.env`)

| Variable | Required in prod | Example | Notes |
|----------|------------------|---------|-------|
| `DB_URL` | yes | `jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require` | JDBC format (starts with `jdbc:`) |
| `DB_USERNAME` | yes | `neondb_owner` | |
| `DB_PASSWORD` | yes | *(secret)* | |
| `JWT_SECRET` | yes | *(≥ 32 random characters)* | `openssl rand -base64 48` |
| `FRONTEND_URL` | yes | `https://your-app.vercel.app` | Exact origin, no trailing slash; comma-separate several |
| `PORT` | auto | `8080` | Render sets this |
| `SPRING_PROFILES_ACTIVE` | yes | `prod` | Already set by the Dockerfile |
| `COOKIE_SAME_SITE` | no | `None` (prod default) / `Lax` (local default) | |
| `DB_POOL_SIZE` | no | `5` | HikariCP maximum pool size |
| `JWT_ACCESS_MINUTES` / `JWT_REFRESH_DAYS` | no | `15` / `7` | Token lifetimes |

With the `prod` profile, the app **refuses to start** if `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`JWT_SECRET` or `FRONTEND_URL` is missing, and the error message names the missing variables.

### Frontend (Vercel / `frontend/.env`)

| Variable | Example |
|----------|---------|
| `VITE_API_URL` | `https://student-management-api.onrender.com` (no `/api`, no trailing slash) |

> Vite embeds `VITE_*` variables **at build time**. After changing one on Vercel, you must **redeploy**.

## 9. API endpoints

All responses use the same JSON envelope:

```json
{ "success": true,  "message": "Student created successfully", "data": { ... }, "timestamp": "..." }
{ "success": false, "message": "Validation failed", "errors": { "email": "Email is required" }, "timestamp": "..." }
```

| Method | Endpoint | Auth | Description | Success |
|--------|----------|------|-------------|---------|
| POST | `/api/auth/register` | – | Create account and log in (sets cookies) | 201 |
| POST | `/api/auth/login` | – | Log in (sets cookies) | 200 |
| POST | `/api/auth/logout` | – | Revoke refresh token, clear cookies | 200 |
| POST | `/api/auth/refresh` | refresh cookie | Rotate tokens | 200 |
| GET | `/api/auth/me` | ✔ | Current user | 200 |
| POST | `/api/students` | ✔ | Create student | 201 |
| GET | `/api/students` | ✔ | List my students (newest first) | 200 |
| GET | `/api/students/{id}` | ✔ | Get one of my students | 200 |
| PUT | `/api/students/{id}` | ✔ | Update | 200 |
| DELETE | `/api/students/{id}` | ✔ | Delete | 200 |
| GET | `/api/students/search?keyword=` | ✔ | Search my students by name or email | 200 |
| GET | `/api/dashboard/stats` | ✔ | Dashboard statistics | 200 |
| GET | `/api/health` | – | App + database health | 200 / 503 |

Error codes:

- 400: validation error or bad UUID
- 401: not logged in, or bad credentials
- 403: missing `X-Requested-With` header, or a CORS origin that isn't allowed
- 404: not found, or owned by another user
- 409: duplicate email
- 500: unexpected error. Details are logged only, never sent to the client.

Example with curl (the cookie jar plays the role of the browser):

```bash
curl -c jar.txt -H "Content-Type: application/json" -H "X-Requested-With: XMLHttpRequest" \
  -d '{"email":"you@example.com","password":"Password123"}' http://localhost:8080/api/auth/login
curl -b jar.txt http://localhost:8080/api/students
```

Student request body:

```json
{
  "fullName": "Priya Sharma", "email": "priya@uni.edu", "phoneNumber": "+91 98765 43210",
  "department": "Computer Science", "gender": "Female", "dateOfBirth": "2004-02-10", "address": "Pune"
}
```

`gender` must be one of `Male`, `Female` or `Other`. `dateOfBirth` uses the format `yyyy-MM-dd` and must be in the past.

## 10. GitHub preparation

```bash
cd student-management-system
git init
git add .
git status            # make sure NO .env files, node_modules or target/ are listed
git commit -m "Student Management System"
git branch -M main
git remote add origin https://github.com/<your-user>/student-management-system.git
git push -u origin main
```

- `.gitignore` excludes `.env*` except `.env.example`. It also excludes `node_modules/`, `dist/` and `target/`.
- `.gitattributes` keeps `mvnw` with LF line endings, so the Render (Linux) build works when you commit from Windows.

## 11. Neon database setup

1. Sign up at <https://neon.tech> and create a project. Pick the region closest to your Render region.
2. Open **Dashboard → Connect**. Choose the **direct** connection, not the pooled `-pooler` host. Flyway and HikariCP work best with direct connections.
3. Neon shows a URL like `postgresql://neondb_owner:PASSWORD@ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require`.
   Split it into the three variables Spring needs:
   - `DB_URL` = `jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require`
   - `DB_USERNAME` = `neondb_owner`
   - `DB_PASSWORD` = `PASSWORD`
4. Don't create any tables. Flyway runs the migrations on the backend's first start.

> `sslmode=require` encrypts the connection, and Neon requires it.

## 12. Render deployment (backend)

Render has no native Java runtime, so Java apps run with **Docker**. `backend/Dockerfile` runs the
same steps you would run by hand:

```
build:  ./mvnw clean package -DskipTests
start:  java -jar target/student-management.jar     (with SPRING_PROFILES_ACTIVE=prod)
```

Steps:

1. Go to <https://render.com>, then **New → Web Service**, and connect your GitHub repo.
2. Settings:
   - **Root Directory:** `backend`
   - **Runtime / Language:** `Docker` (Render detects `backend/Dockerfile`)
   - **Instance type:** Free is fine for a demo
   - **Health Check Path:** `/api/health`
3. Add the **environment variables**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` and `FRONTEND_URL`.
   For `FRONTEND_URL`, use your Vercel URL. If you don't have it yet, use a placeholder and update it after step 13.
4. Click **Create Web Service**. In the logs you should see `Successfully applied 3 migrations` and `Started StudentManagementApplication`.
5. Open `https://<your-service>.onrender.com/api/health`. It should report `"database":"UP"`.

You can also use **New → Blueprint** with the included `render.yaml`, which generates `JWT_SECRET` for you.

> Render's free tier sleeps after about 15 minutes idle. The first request after that can take about a minute. The login page shows a "server is waking up" notice when this happens.

## 13. Vercel deployment (frontend)

1. Go to <https://vercel.com>, then **Add New → Project**, and import the same GitHub repo.
2. Settings:
   - **Root Directory:** `frontend`
   - **Framework Preset:** Vite
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
3. **Environment Variables:** `VITE_API_URL` = `https://<your-service>.onrender.com`
4. Click **Deploy**. Copy the production URL, for example `https://student-management-demo.vercel.app`.
5. Back on Render, set `FRONTEND_URL` to exactly that URL, with no trailing slash, and save. Render redeploys.

`frontend/vercel.json` rewrites every path to `index.html`, so refreshing `/students/123` works.

> **Note:** Vercel preview deployments use different URLs. Add each one to `FRONTEND_URL` (comma-separated) if you want previews to log in.

### Optional: same-origin proxy (for browsers that block third-party cookies)

Some browsers block cross-site cookies by default, for example Safari and some privacy modes. In those browsers, login on `vercel.app` won't stick because the cookie belongs to `onrender.com`. To fix this, proxy the API through Vercel so the cookies become first-party:

1. Replace `frontend/vercel.json` with:
   ```json
   {
     "rewrites": [
       { "source": "/api/:path*", "destination": "https://<your-service>.onrender.com/api/:path*" },
       { "source": "/(.*)", "destination": "/index.html" }
     ]
   }
   ```
2. On Vercel, set `VITE_API_URL=/`. On Render, set `COOKIE_SAME_SITE=Lax`. Then redeploy both.

## 14. Production testing

1. Check `https://<render-service>.onrender.com/api/health`. Expect `status: UP` and `database: UP`.
2. Open the Vercel URL and **register** a user. You should land on the dashboard.
3. In DevTools, open **Application → Cookies** for the Render domain. You should see `access_token` and `refresh_token`, both `HttpOnly` and `Secure` with `SameSite=None`. **Local Storage** should be empty.
4. **Add** a few students. The dashboard counts and the department chart should update.
5. **Search**, **edit**, and **delete** a student. Deleting should ask for confirmation first.
6. Refresh the browser on `/students`. You should stay logged in, and the page should load (SPA fallback).
7. **Log out**, which asks for confirmation. Then visit `/dashboard`; you should be redirected to `/login`.
8. Ownership: register a second user in a private window. That user sees none of the first user's students. Opening a URL like `/students/<first user's id>` shows "Student not found".
9. Protection: `curl https://<render-service>.onrender.com/api/students` should return 401 JSON.
10. In Neon's **SQL Editor**, run `SELECT full_name, email FROM students;` to see the stored rows. Passwords are BCrypt hashes in `users.password_hash`.

### Troubleshooting

| Symptom | Fix |
|---------|-----|
| Browser console shows a CORS error | `FRONTEND_URL` on Render must exactly match the Vercel origin (`https://`, no trailing slash) |
| Login succeeds but the next request returns 401 | Third-party cookies are blocked. Use the same-origin proxy above |
| Frontend calls `localhost:8080` in production | `VITE_API_URL` isn't set on Vercel. Set it and **redeploy** |
| Render log: `Missing required environment variable(s)` | Add the listed variables in Render |
| Render log: `Connection refused` / SSL error | Check `DB_URL` starts with `jdbc:postgresql://` and ends with `?sslmode=require` |
| `./mvnw: Permission denied` (on Linux/macOS, locally) | `chmod +x backend/mvnw` |
