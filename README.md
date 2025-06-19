# Football Dynasty - Modern Management Dashboard

A modern, revamped version of the Football Dynasty management system built with Spring Boot and React, featuring a sleek filled design aesthetic with glassmorphism UI components.

## 🚀 Features

### Backend (Spring Boot)
- **Modern Architecture**: Spring Boot 3.2 with Java 17
- **RESTful API**: Comprehensive REST endpoints with OpenAPI documentation
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security integration ready
- **Validation**: Bean validation with custom error handling
- **Documentation**: Swagger/OpenAPI 3 integration
- **Mapping**: MapStruct for efficient DTO mapping

### Frontend (React + TypeScript)
- **Modern React**: React 18 with TypeScript
- **Design System**: Custom glassmorphism UI with filled components
- **State Management**: TanStack Query for server state management
- **Routing**: React Router v6
- **Animations**: Framer Motion for smooth interactions
- **UI Framework**: Material-UI v5 with custom theme
- **Responsive**: Mobile-first responsive design

### Design Features
- **Glassmorphism**: Modern glass-like UI components
- **Gradient Backgrounds**: Beautiful gradient overlays
- **Smooth Animations**: Framer Motion powered interactions
- **Dark Theme**: Modern dark theme with vibrant accents
- **Filled Components**: Modern filled button and card designs
- **Backdrop Blur**: CSS backdrop-filter for glass effects

## 🏗️ Architecture

### Backend Structure
```
backend/
├── src/main/java/com/footballdynasty/
│   ├── entity/          # JPA entities
│   ├── repository/      # Data repositories
│   ├── service/         # Business logic
│   ├── controller/      # REST controllers
│   ├── dto/            # Data transfer objects
│   ├── mapper/         # MapStruct mappers
│   └── exception/      # Exception handling
└── src/main/resources/
    └── application.yml # Configuration
```

### Frontend Structure
```
frontend/
├── src/
│   ├── components/
│   │   ├── ui/         # Reusable UI components
│   │   └── layout/     # Layout components
│   ├── pages/          # Page components
│   ├── services/       # API services
│   ├── types/          # TypeScript types
│   ├── theme/          # MUI theme configuration
│   └── hooks/          # Custom hooks
└── public/             # Static assets
```

## 🛠️ Setup & Installation

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Maven 3.6+

### Backend Setup

1. **Database Setup**
   ```sql
   CREATE DATABASE fd_db;
   CREATE USER fd_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE fd_db TO fd_user;
   ```

2. **Environment Configuration**
   ```bash
   export DB_PASSWORD=your_password
   ```

3. **Run the Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080/api/v2`
   Swagger UI: `http://localhost:8080/api/v2/swagger-ui.html`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Development Server**
   ```bash
   npm start
   ```

   The app will be available at `http://localhost:3000`

## 🎨 Design System

### Color Palette
- **Primary**: Blue gradients (`#1e88e5` to `#42a5f5`)
- **Secondary**: Teal gradients (`#26a69a` to `#4db6ac`)
- **Background**: Dark blue gradients (`#0a0e27` to `#242744`)
- **Surface**: Glass-like overlays with backdrop blur

### Components
- **GlassCard**: Glassmorphism card component with blur effects
- **GradientButton**: Filled buttons with gradient backgrounds
- **Custom Theme**: Extended MUI theme with glass and gradient palettes

### Animations
- **Hover Effects**: Scale and translate animations
- **Page Transitions**: Staggered entrance animations
- **Loading States**: Smooth skeleton loading

## 📊 Features Implemented

### Dashboard
- ✅ Modern glassmorphism design
- ✅ Team statistics overview
- ✅ Recent and upcoming games
- ✅ Season progress indicators
- ✅ Animated components

### API Endpoints
- ✅ Teams CRUD operations
- ✅ Games management
- ✅ Standings tracking
- ✅ Achievements system
- ✅ Search and pagination

### UI Components
- ✅ Responsive sidebar navigation
- ✅ Modern header with glass effects
- ✅ Card-based layouts
- ✅ Gradient buttons and chips
- ✅ Progress indicators

## 🚀 Deployment

### Backend Deployment
```bash
cd backend
mvn clean package
java -jar target/football-dynasty-api-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment
```bash
cd frontend
npm run build
# Deploy the build/ folder to your hosting service
```

## 🔧 Development

### Adding New Features
1. **Backend**: Create entity → repository → service → controller → DTO
2. **Frontend**: Add types → API service → UI components → pages

### Styling Guidelines
- Use the custom theme for consistent colors
- Leverage GlassCard for containers
- Use GradientButton for primary actions
- Apply motion animations for interactions

## 📝 API Documentation

When running the backend, visit `http://localhost:8080/api/v2/swagger-ui.html` for interactive API documentation.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Follow the established patterns
4. Test your changes
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

---

*Built with ❤️ using Spring Boot, React, and modern web technologies*