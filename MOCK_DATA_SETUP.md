# Mock Data System Setup

This project includes a comprehensive mock data system that automatically populates the database with realistic CFB data when running in testing mode.

## 🚀 **Quick Setup**

### Method 1: Environment Variable
Set the environment variable before starting the backend:

```bash
export CURRENT_ENVIRONMENT=testing
./mvnw spring-boot:run
```

### Method 2: Using .env.testing file
1. Copy the provided `.env.testing` file to your environment
2. Source it before starting:
```bash
source backend/.env.testing
./mvnw spring-boot:run
```

### Method 3: Application Properties
Add to `application.yml` or set as environment variables:
```yaml
app:
  environment: testing
  mock-data:
    enabled: true
```

## 📊 **What Gets Created**

When `CURRENT_ENVIRONMENT=testing`, the system automatically creates:

### **Game Schedule & Results**
- **15 weeks** of CFB games for the current season
- **Past 8 weeks**: Completed games with realistic scores
- **Week 9**: Current week with in-progress/scheduled games  
- **Weeks 10-15**: Future scheduled games
- **Conference vs non-conference matchups** (70% conference games)
- **Realistic score generation** (14-45 points, blowouts, close games)

### **Conference Standings**
- Automatic calculation based on completed games
- Conference records (wins/losses within conference)
- Overall season records
- Conference rankings with proper tiebreakers
- Championship bid analysis for all teams

### **Team Data**
- Uses existing 120 CFB teams from `cfb_teams.sql`
- Teams grouped by realistic conferences (SEC, Big 12, etc.)
- Coach names and team metadata

## 🎮 **Frontend Integration**

The mock data seamlessly integrates with the frontend dashboard:

### **Dashboard Features**
- **Conference Championship Tracking**: Shows bid status, games needed
- **Recent Games**: Displays completed games with scores
- **Upcoming Games**: Shows scheduled future matchups
- **Season Progress**: Conference standings and bowl eligibility
- **Real-time Updates**: Championship scenarios update as games complete

### **Conference Standings**
- Live conference rankings
- Championship elimination/qualification status
- Magic number calculations (games needed to clinch)
- Conference vs overall records

## 🔧 **Manual Control**

### **Admin Endpoints**
Check environment status:
```bash
GET /api/v2/admin/environment
```

Manually recreate mock data:
```bash
POST /api/v2/admin/mock-data/create
```

### **Environment Check**
```bash
# Check if mock data is enabled
curl http://localhost:8080/api/v2/admin/environment

# Response:
{
  "environment": "testing",
  "mockDataEnabled": true,
  "isTestingEnvironment": true,
  "timestamp": 1703123456789
}
```

### **Force Recreation**
```bash
# Manually trigger mock data creation
curl -X POST http://localhost:8080/api/v2/admin/mock-data/create

# Response:
{
  "message": "Mock data created successfully",
  "duration": "2.3s",
  "environment": "testing",
  "timestamp": 1703123456789
}
```

## 📋 **Verification**

### **Check Data Creation**
1. **Teams**: Visit `/api/v2/teams` - should show 120+ teams
2. **Games**: Visit team selection → dashboard - should show recent/upcoming games
3. **Standings**: Dashboard should show conference rankings and championship status
4. **Logs**: Backend logs show detailed mock data creation process

### **Frontend Testing**
1. Register/login with any team
2. Dashboard shows:
   - Conference rank and record
   - Championship bid status ("Alive" or "Eliminated")  
   - Recent completed games with scores
   - Upcoming scheduled games
   - Conference championship analysis

### **Expected Logs**
```
INFO  - MOCK_DATA_START: Initializing mock data for testing environment
INFO  - MOCK_WEEK_COMPLETE: Created 15 weeks for year 2024
INFO  - MOCK_GAME_COMPLETE: Created 45 total games, 32 completed
INFO  - MOCK_STANDINGS_COMPLETE: Created standings for 120 teams
INFO  - MOCK_DATA_COMPLETE: Successfully initialized all mock data
```

## 🏈 **Realistic Data Features**

### **Game Results**
- **Score ranges**: 14-45 points per team
- **Blowouts**: 15% chance of 20+ point differential
- **Close games**: 30% chance of games within 7 points
- **Conference games**: 70% of matchups are within same conference

### **Season Progression**
- **Week 1-8**: Completed with final scores
- **Week 9**: Current week (some in-progress, some scheduled)
- **Week 10-15**: Future games (all scheduled)
- **Realistic dates**: Games spread across actual CFB season timeline

### **Championship Analysis**
- **Magic numbers**: Automatic calculation of games needed to clinch
- **Elimination scenarios**: Shows when teams are mathematically eliminated
- **Tiebreakers**: Conference record → Overall record → Team name
- **Real-time updates**: Changes as games are completed

## 🔄 **Development Workflow**

### **Local Development**
```bash
# Enable testing mode
export CURRENT_ENVIRONMENT=testing

# Start backend
./mvnw spring-boot:run

# Start frontend  
cd frontend && npm start

# System automatically creates mock data on startup
```

### **Production Safety**
- Mock data **only activates** when `CURRENT_ENVIRONMENT=testing`
- Production/development environments are **unaffected**
- Safe to deploy with mock data code included
- Environment variable **must be explicitly set** to activate

### **Database Reset**
```bash
# To reset and recreate mock data:
curl -X POST http://localhost:8080/api/v2/admin/mock-data/create

# This will:
# 1. Delete existing mock data for current year
# 2. Recreate fresh schedule and results  
# 3. Recalculate all standings
```

This comprehensive mock data system provides a realistic CFB experience for testing and development, with automatic conference championship tracking that enhances the user experience.