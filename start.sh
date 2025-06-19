#!/bin/bash

# Football Dynasty - Start Script
# Usage: ./start.sh <profile>
# Example: ./start.sh testing

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to cleanup background processes
cleanup() {
    print_warning "Shutting down services..."
    if [[ -n $BACKEND_PID ]]; then
        print_status "Stopping backend (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null || true
    fi
    if [[ -n $FRONTEND_PID ]]; then
        print_status "Stopping frontend (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    print_success "Services stopped."
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Check if profile argument is provided
PROFILE=${1:-development}

print_status "Starting Football Dynasty with profile: $PROFILE"
print_status "======================================================"

# Validate profile
case $PROFILE in
    development|testing|production)
        print_success "Valid profile: $PROFILE"
        ;;
    *)
        print_error "Invalid profile: $PROFILE"
        print_error "Valid profiles: development, testing, production"
        exit 1
        ;;
esac

# Check if we're in the project root
if [[ ! -d "backend" ]] || [[ ! -d "frontend" ]]; then
    print_error "Must run from project root directory (should contain 'backend' and 'frontend' folders)"
    exit 1
fi

# Check for required tools
print_status "Checking dependencies..."

if ! command -v mvn &> /dev/null; then
    print_error "Maven not found. Please install Maven."
    exit 1
fi

if ! command -v npm &> /dev/null; then
    print_error "npm not found. Please install Node.js and npm."
    exit 1
fi

print_success "All dependencies found."

# Install frontend dependencies if needed
print_status "Checking frontend dependencies..."
cd frontend
if [[ ! -d "node_modules" ]]; then
    print_status "Installing frontend dependencies..."
    npm install
    print_success "Frontend dependencies installed."
else
    print_status "Frontend dependencies already installed."
fi
cd ..

# Start backend
print_status "Starting backend with profile: $PROFILE..."
cd backend

if [[ "$PROFILE" == "development" ]]; then
    mvn spring-boot:run > ../backend.log 2>&1 &
else
    mvn spring-boot:run -P $PROFILE > ../backend.log 2>&1 &
fi

BACKEND_PID=$!
cd ..

print_success "Backend started (PID: $BACKEND_PID)"
print_status "Backend logs: ./backend.log"

# Wait a moment for backend to start
print_status "Waiting for backend to initialize..."
sleep 3

# Start frontend
print_status "Starting frontend..."
cd frontend
npm start > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

print_success "Frontend started (PID: $FRONTEND_PID)"
print_status "Frontend logs: ./frontend.log"

# Display status
print_status "======================================================"
print_success "Football Dynasty is starting up!"
print_status "Backend:  http://localhost:8080 (Profile: $PROFILE)"
print_status "Frontend: http://localhost:3000"
print_status "API Docs: http://localhost:8080/api/v2/swagger-ui.html"
print_status "======================================================"
print_status "Backend PID: $BACKEND_PID"
print_status "Frontend PID: $FRONTEND_PID"
print_status ""
print_warning "Press Ctrl+C to stop both services"
print_status ""

# Wait for backend to be ready
print_status "Waiting for services to be ready..."
BACKEND_READY=false
FRONTEND_READY=false
MAX_ATTEMPTS=30
ATTEMPT=0

while [[ $ATTEMPT -lt $MAX_ATTEMPTS ]]; do
    ATTEMPT=$((ATTEMPT + 1))
    
    # Check backend
    if ! $BACKEND_READY && curl -s http://localhost:8080/api/v2/auth/test > /dev/null 2>&1; then
        BACKEND_READY=true
        print_success "Backend is ready! ✓"
    fi
    
    # Check frontend (just check if port 3000 is listening)
    if ! $FRONTEND_READY && curl -s http://localhost:3000 > /dev/null 2>&1; then
        FRONTEND_READY=true
        print_success "Frontend is ready! ✓"
    fi
    
    if $BACKEND_READY && $FRONTEND_READY; then
        break
    fi
    
    if [[ $ATTEMPT -eq 1 ]] || [[ $((ATTEMPT % 5)) -eq 0 ]]; then
        echo -n "."
    fi
    
    sleep 2
done

echo ""

if $BACKEND_READY && $FRONTEND_READY; then
    print_success "🎉 All services are ready!"
    
    # Show profile-specific information
    case $PROFILE in
        testing)
            print_status "🧪 Testing Profile Active:"
            print_status "  - Mock data will be generated automatically"
            print_status "  - Comprehensive CFB season with 15 weeks"
            print_status "  - 8 conferences, 64+ teams"
            ;;
        development)
            print_status "🛠️  Development Profile Active:"
            print_status "  - No mock data (clean environment)"
            print_status "  - Debug logging enabled"
            ;;
        production)
            print_status "🚀 Production Profile Active:"
            print_status "  - Optimized for performance"
            print_status "  - Minimal logging"
            ;;
    esac
    
    print_status ""
    print_status "🌐 Open http://localhost:3000 to access the application"
    
else
    if ! $BACKEND_READY; then
        print_error "Backend failed to start properly. Check ./backend.log for details."
    fi
    if ! $FRONTEND_READY; then
        print_error "Frontend failed to start properly. Check ./frontend.log for details."
    fi
    print_warning "You can still try accessing the services manually."
fi

# Keep script running and tail logs
print_status ""
print_status "🎛️  Options:"
print_status "  - Press Ctrl+C to stop both services"
print_status "  - Type 'logs' to view live logs"
print_status "  - Type 'status' to check service status"
print_status "  - Type 'quit' to stop services"
print_status ""

# Function to check service status
check_services() {
    local backend_running=false
    local frontend_running=false
    
    if kill -0 $BACKEND_PID 2>/dev/null; then
        backend_running=true
        print_success "Backend: Running (PID: $BACKEND_PID)"
    else
        print_error "Backend: Stopped (PID: $BACKEND_PID)"
    fi
    
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        frontend_running=true
        print_success "Frontend: Running (PID: $FRONTEND_PID)"
    else
        print_error "Frontend: Stopped (PID: $FRONTEND_PID)"
    fi
    
    if ! $backend_running || ! $frontend_running; then
        print_warning "Some services have stopped. Check log files for details:"
        print_status "  Backend logs:  ./backend.log"
        print_status "  Frontend logs: ./frontend.log"
        return 1
    fi
    return 0
}

# Interactive loop
while true; do
    echo -n "> "
    read -r command
    
    case $command in
        "logs")
            print_status "Showing live logs (Ctrl+C to return to menu)..."
            tail -f backend.log frontend.log 2>/dev/null || print_warning "Log files not found yet"
            ;;
        "status")
            check_services
            ;;
        "quit"|"exit"|"stop")
            break
            ;;
        "")
            # Just check if services are still running on empty input
            if ! check_services; then
                print_error "Services have stopped. Exiting..."
                break
            fi
            ;;
        *)
            print_status "Available commands: logs, status, quit"
            ;;
    esac
done

cleanup