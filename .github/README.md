# GitHub Actions CI/CD Pipeline

This repository includes comprehensive GitHub Actions workflows for continuous integration and testing.

## Workflows Overview

### 🔧 Backend CI (`backend-ci.yml`)
**Triggers:** Changes to `backend/` directory
- **Java 17** setup with Maven
- **PostgreSQL 15** test database
- **Test Coverage** with JaCoCo (minimum 30% threshold)
- **Code Quality** checks:
  - Checkstyle (Google standards)
  - SpotBugs (bug detection)
  - PMD (code analysis)
- **Codecov** integration for coverage reports
- **PR Comments** with coverage details

### ⚛️ Frontend CI (`frontend-ci.yml`) 
**Triggers:** Changes to `frontend/` directory
- **Node.js 18** setup with npm
- **TypeScript** type checking
- **ESLint** linting (max 10 warnings)
- **Jest** unit tests with coverage
- **Playwright** E2E tests
- **Security Audit** with npm audit
- **Coverage Thresholds:**
  - Lines: 40%
  - Statements: 40%
  - Functions: 30%
  - Branches: 25%

### 🔄 Full Stack CI (`full-ci.yml`)
**Triggers:** Push to `main`, PRs, daily schedule
- **Path-based filtering** (runs only affected parts)
- **Matrix testing** (multiple Java/Node versions)
- **Integration tests** (full stack E2E)
- **Security auditing** for both frontend and backend
- **Deployment readiness** checks

## Features

### 📊 Test Coverage
- **Backend:** JaCoCo reports with XML output for Codecov
- **Frontend:** Jest coverage with LCOV reports
- **PR Comments:** Automatic coverage reporting on pull requests
- **Thresholds:** Configurable minimum coverage requirements

### 🔍 Code Quality
- **Linting:** ESLint for frontend, Checkstyle for backend
- **Static Analysis:** SpotBugs, PMD for Java code
- **Type Safety:** TypeScript strict checking
- **Security:** npm audit, dependency checks

### 🚀 Performance
- **Caching:** Maven and npm dependencies cached
- **Parallel Jobs:** Frontend and backend run simultaneously  
- **Smart Triggers:** Only run when relevant files change
- **Matrix Testing:** Test multiple environments

### 🛡️ Security
- **Dependency Scanning:** npm audit with high severity threshold
- **Secret Management:** Uses GitHub secrets for tokens
- **Database Security:** Isolated test database per job

## Configuration

### Required Secrets
Add these to your GitHub repository secrets:

```bash
CODECOV_TOKEN=<your-codecov-token>
SENTRY_DSN=<your-sentry-dsn>
```

### Environment Variables
The workflows use these environment variables:
- `NODE_VERSION: '18'` - Node.js version
- `JAVA_VERSION: '17'` - Java version
- `SPRING_PROFILES_ACTIVE` - Spring Boot profile

### Coverage Thresholds
Current thresholds can be adjusted in workflow files:

**Backend (JaCoCo):**
```xml
<minimum>0.30</minimum> <!-- 30% line coverage -->
```

**Frontend (Jest):**
```bash
MIN_LINES=40      # 40% line coverage
MIN_STATEMENTS=40 # 40% statement coverage  
MIN_FUNCTIONS=30  # 30% function coverage
MIN_BRANCHES=25   # 25% branch coverage
```

## Artifacts

Each workflow produces downloadable artifacts:
- **Backend:** Test reports, coverage reports, JAR files
- **Frontend:** Build artifacts, test results, Playwright reports
- **Integration:** E2E test results and screenshots

## Status Badges

Add these badges to your README:

```markdown
[![Backend CI](https://github.com/footballdynasty/fd-Mono/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/footballdynasty/fd-Mono/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/footballdynasty/fd-Mono/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/footballdynasty/fd-Mono/actions/workflows/frontend-ci.yml)
[![Full CI](https://github.com/footballdynasty/fd-Mono/actions/workflows/full-ci.yml/badge.svg)](https://github.com/footballdynasty/fd-Mono/actions/workflows/full-ci.yml)
[![codecov](https://codecov.io/gh/footballdynasty/fd-Mono/branch/main/graph/badge.svg)](https://codecov.io/gh/footballdynasty/fd-Mono)
```

## Local Development

### Running Tests Locally

**Backend:**
```bash
cd backend
mvn test jacoco:report
mvn checkstyle:check
mvn spotbugs:check
```

**Frontend:**
```bash
cd frontend
npm test -- --coverage
npm run type-check
npx eslint src --ext .ts,.tsx
```

### Coverage Reports
- **Backend:** `backend/target/site/jacoco/index.html`
- **Frontend:** `frontend/coverage/lcov-report/index.html`

## Troubleshooting

### Common Issues
1. **Database Connection:** Ensure PostgreSQL service is healthy
2. **Node Version:** Use Node 18+ for frontend builds
3. **Memory Limits:** Large test suites may need increased heap size
4. **Timeouts:** E2E tests may need longer timeout values

### Debugging
- Check workflow logs in GitHub Actions tab
- Download artifacts for detailed reports
- Run tests locally to reproduce issues
- Verify all required secrets are configured