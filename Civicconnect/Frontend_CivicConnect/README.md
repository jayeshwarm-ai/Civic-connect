# Citizen Service Frontend (React + Vite)

Migrated from Create React App to **React + Vite** and reorganized into a clean **module-based structure**. All application functionality, routes, and styling are unchanged.

## Run

```bash
npm install
npm run dev      # http://localhost:3000
npm run build    # production build → ./build
npm run preview  # preview the production build
```

## Project structure

```
citizen-service-frontend/
├── index.html              # Vite entry HTML
├── vite.config.js          # Vite config (port 3000, outDir 'build')
├── package.json
└── src/
    ├── main.jsx            # App entry (was index.js)
    ├── App.jsx             # Routes + role-based redirects (was App.js)
    ├── App.css             # Global styles
    ├── components/
    │   └── Header.jsx      # Role-aware top navigation
    ├── context/
    │   └── AuthContext.jsx # JWT auth provider
    ├── services/
    │   └── api.js          # Axios client + all backend endpoints
    └── modules/            # ────── Feature modules ──────
        ├── auth/                   # Login, Register, Reset Password
        ├── citizen/                # Profile, Documents, Help (CITIZEN role)
        ├── admin/                  # Dashboard, Citizens, Pending Docs, Staff
        ├── service-requests/       # Submit / View / Edit / Assign / Update / Officer dashboard
        ├── resolutions/            # Resolution workflow steps
        ├── compliance/             # Compliance records + audits
        ├── feedback/               # Citizen feedback + officer leaderboard
        ├── reports/                # Reporting dashboard
        ├── notifications/          # Notification center
        └── common/                 # Public landing page
```

## Module breakdown

| Module             | Pages                                                                                                  | Used by roles                                            |
| ------------------ | ------------------------------------------------------------------------------------------------------ | -------------------------------------------------------- |
| `auth`             | LoginPage, RegisterPage, ResetPasswordPage                                                             | Public (unauthenticated)                                 |
| `citizen`          | ProfilePage, ProfileEditPage, DocumentsPage, HelpPage                                                  | CITIZEN                                                  |
| `admin`            | AdminDashboard, CitizenDetailPage, PendingDocumentsPage, StaffManagementPage                           | CITY_ADMINISTRATOR (some shared with DEPT_HEAD/OFFICER)  |
| `service-requests` | My, Submit, Edit, Detail, AllRequests, AssignOfficer, OfficerDashboard, UpdateStatus                   | CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMIN    |
| `resolutions`      | MyResolutionsPage, CreateResolutionPage, ResolutionDetailPage                                          | SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR     |
| `compliance`       | ComplianceDashboard, CreateComplianceRecord, ComplianceRecordDetail, CreateAudit, AuditDetail          | COMPLIANCE_OFFICER, CITY_ADMINISTRATOR                   |
| `feedback`         | MyFeedbackPage, SubmitFeedbackPage, LeaderboardPage                                                    | CITIZEN (submit), Staff roles (leaderboard)              |
| `reports`          | ReportsDashboardPage                                                                                   | DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER  |
| `notifications`    | NotificationsPage                                                                                      | All authenticated roles                                  |
| `common`           | LandingPage                                                                                            | Public                                                   |

## Module convention

Every module folder exposes its pages via a barrel `index.js`:

```js
// src/modules/auth/index.js
export { default as LoginPage } from './LoginPage';
export { default as RegisterPage } from './RegisterPage';
export { default as ResetPasswordPage } from './ResetPasswordPage';
```

This lets `App.jsx` import cleanly:

```js
import { LoginPage, RegisterPage, ResetPasswordPage } from './modules/auth';
```

To add a new page to a module:
1. Create `src/modules/<module>/NewPage.jsx`.
2. Add `export { default as NewPage } from './NewPage';` to that module's `index.js`.
3. Import it in `App.jsx` and add a `<Route>`.

## What was preserved (zero functional changes)

- `services/api.js` — same `http://localhost:9999` base URL, all 60+ endpoints.
- `context/AuthContext.jsx` — same JWT decode/login/logout logic.
- `components/Header.jsx` — same role-based navigation for all 5 roles.
- All routes in `App.jsx` — every path, role check, and redirect identical.
- `App.css` — full stylesheet, byte-for-byte.
- All 33 page components — only their location and import paths to shared code changed.
