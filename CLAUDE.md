# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Football Dynasty is a modern management dashboard built with Spring Boot (backend) and React TypeScript (frontend), featuring a glassmorphism UI design. The application manages football teams, games, standings, and achievements through a RESTful API with a comprehensive achievement system including configurable rewards and approval workflows.

## Architecture

**Backend** (`backend/`): Spring Boot 3.2 with Java 17
- Follows standard Spring MVC pattern: Entity → Repository → Service → Controller → DTO
- Uses MapStruct for DTO mapping
- PostgreSQL database with JPA/Hibernate
- API context path: `/api/v2`
- OpenAPI/Swagger documentation available
- Spring Security with JWT authentication
- MockDataService for testing environment data generation
- Sentry integration for error tracking and performance monitoring

**Frontend** (`frontend/`): React 18 with TypeScript
- Material-UI v5 with custom glassmorphism theme
- TanStack Query for server state management  
- React Router v6 for routing
- Framer Motion for animations
- Custom UI components in `components/ui/` (GlassCard, GradientButton)
- Authentication context with team selection
- Sentry integration for frontend monitoring
- Playwright for E2E testing

## Development Commands

### Backend
```bash
cd backend
mvn spring-boot:run                    # Start development server (port 8080) - uses development profile
mvn spring-boot:run -P testing        # Run with testing profile (includes mock data)
mvn spring-boot:run -P production     # Run with production profile
mvn clean package                     # Build JAR
mvn test                              # Run tests
# Note: Ctrl+C shutdown is optimized for fast termination (~5s)
```

### Frontend  
```bash
cd frontend
npm start                   # Start development server (port 3000)
npm run build              # Production build
npm test                   # Run unit tests
npm run type-check         # TypeScript type checking
npm run test:e2e           # Run Playwright E2E tests
npm run test:e2e:ui        # Run Playwright with UI mode
npm run test:e2e:debug     # Debug Playwright tests
```

### Integrated Development
```bash
# Start both backend and frontend with one command
./start.sh testing         # With mock data
./start.sh development     # Clean environment  
./start.sh production      # Production mode
```

## Development Memories
- use ./start.sh to run our backend and frontend!
- Dont start the services unless I tell you to

## Environment Setup

**Database**: PostgreSQL database `fd_db` with user `fd_user`
**Environment Variables**: 
- `DB_PASSWORD`: Database password (required)
- `REACT_APP_API_URL`: Frontend API base URL (defaults to `http://localhost:8080/api/v2`)

**Maven Profiles**:
- `development` (default): No mock data, debug logging enabled
- `testing`: Mock data enabled, debug logging, automatically generates comprehensive mock CFB season data
- `production`: No mock data, minimal logging, production-optimized settings

**Testing Environment**: Use `-P testing` profile to automatically generate comprehensive mock CFB season data including 15 weeks of games, realistic scores, conference standings, and championship scenarios.

## Key Patterns

**API Structure**: All endpoints follow REST conventions with pagination support
- Teams: `/teams` - CRUD, search, conference filtering
- Games: `/games` - CRUD, team/week filtering, score updates
  - `/games/team/{teamId}` - All games for a team
  - `/games/recent?teamId={id}` - Recent completed games  
  - `/games/upcoming?teamId={id}` - Future scheduled games
- Standings: `/standings` - year/conference/team filtering
- Achievements: `/achievements` - CRUD, type filtering, completion tracking
- Conference Championship: `/conference-championship/bid/{teamId}/{year}` - Championship analysis

**Entity Relationships**: All entities use UUID primary keys. When building queries that reference team relationships, convert string IDs to UUID in the controller layer before passing to repository methods.

**Frontend State**: Use TanStack Query for all API calls, custom hooks in `hooks/`
- API responses are direct arrays/objects, not wrapped in `.data` properties
- Dashboard component fetches team-specific data using selected team context
- All API calls should handle loading and error states

**Styling**: Leverage custom theme with glassmorphism components, avoid inline styles
**Types**: All TypeScript interfaces defined in `types/index.ts`

## Achievement System Architecture

The application includes a comprehensive achievement system with configurable rewards and approval workflows:

**Core Components**:
- **Achievement Management**: CRUD operations, rarity-based system (COMMON → LEGENDARY), completion tracking
- **Approval Workflow**: Request-based completion system where non-admin users submit requests for admin review
- **Configurable Rewards**: YAML-driven reward system with trait boosts and game restarts based on achievement rarity

**Key Entities**:
- `Achievement`: Core achievement tracking with types (WINS, SEASON, CHAMPIONSHIP, STATISTICS, GENERAL)
- `AchievementRequest`: Approval workflow with PENDING/APPROVED/REJECTED status
- `AchievementReward`: Two reward types (TRAIT_BOOST, GAME_RESTART) with 40+ defined traits

**Service Architecture**:
- `AchievementService`: CRUD, statistics, completion management
- `InboxService`: Admin request review system with duplicate prevention  
- `AchievementRewardService`: Configurable reward initialization and distribution
- `AchievementRewardConfig`: YAML-based reward configuration system

**Admin Endpoints**: 
- `/admin/inbox/requests` - Manage pending achievement requests
- `/admin/rewards/statistics` - Reward distribution analytics
- `/admin/rewards/clear` - Clear all rewards
- `/admin/rewards/reset` - Reinitialize rewards with current configuration

**Completion Flow**: Non-admin users submit requests → Admin reviews in inbox → Approval triggers achievement completion and reward distribution

## Mock Data System

The application includes a comprehensive mock data generation system for testing:

- **Activation**: Set `CURRENT_ENVIRONMENT=testing` or `MOCK_DATA_ENABLED=true`
- **Scope**: Generates complete CFB season with 8 conferences, 64+ teams, 15 weeks of games
- **Game Types**: Realistic scoring patterns (blowouts, close games, overtime, defensive battles)
- **Timeline**: Past 8 weeks completed, current week in progress, future weeks scheduled
- **Conference Logic**: 80% intra-conference matchups, rivalry games, bowl eligibility scenarios

## Documentation

- API documentation: `http://localhost:8080/api/v2/swagger-ui.html` (when backend running)
- Design system uses glassmorphism with gradient backgrounds and backdrop blur effects
- Postman collection available: `Football_Dynasty_API_Collection.postman_collection.json`

## Sentry Integration

Both backend and frontend include Sentry for error tracking and performance monitoring:

**Backend**: Manual Sentry initialization in `SentryConfig.java` with environment-based configuration
**Frontend**: Use Sentry patterns from RULES.md:
- Error tracking: `Sentry.captureException(error)`
- Custom spans: `Sentry.startSpan()` for UI actions and API calls
- Structured logging: `logger.debug()`, `logger.info()`, `logger.error()`

## Testing Strategy

**Backend**: JUnit integration tests with `@SpringBootTest`
**Frontend**: 
- Unit tests with Jest and React Testing Library
- E2E tests with Playwright (standings navigation, user workflows)
- TypeScript type checking with `npm run type-check`

## Coding Principles

- Try the least destructive action first
- Implement comprehensive error handling with Sentry integration
- Follow Spring MVC patterns for backend development
- Use TanStack Query for frontend API state management

## Workflow Guidance

- For each new page make sure to make a JIRA epic & tickets (story and tasks) before implementing. After you make those epics, just say "completed epic" and dont implement it yet.

## Important Guidelines

Refer to RULES.md for Sentry implementation patterns and comprehensive project guidelines.