#!/bin/bash

# Football Dynasty - Start Script
# Usage: 
#   ./start.sh <profile>           - Start both services
#   ./start.sh setup-database      - Setup PostgreSQL database
# 
# Examples: 
#   ./start.sh testing            - Start with testing profile
#   ./start.sh setup-database     - Install and configure database

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

# Function to detect OS
detect_os() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macos"
    elif [[ -f /etc/debian_version ]]; then
        echo "debian"
    elif [[ -f /etc/redhat-release ]]; then
        echo "redhat"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
        echo "windows"
    else
        echo "unknown"
    fi
}

# Function to install PostgreSQL
install_postgresql() {
    local os=$(detect_os)
    print_status "Detected OS: $os"
    
    case $os in
        "macos")
            print_status "Installing PostgreSQL via Homebrew..."
            if ! command -v brew &> /dev/null; then
                print_error "Homebrew not found. Please install Homebrew first:"
                print_error "  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
                exit 1
            fi
            brew install postgresql
            brew services start postgresql
            print_success "PostgreSQL installed and started via Homebrew"
            ;;
        "debian")
            print_status "Installing PostgreSQL on Debian/Ubuntu..."
            sudo apt update
            sudo apt install -y postgresql postgresql-contrib
            sudo systemctl start postgresql
            sudo systemctl enable postgresql
            print_success "PostgreSQL installed and started"
            ;;
        "redhat")
            print_status "Installing PostgreSQL on CentOS/RHEL/Fedora..."
            if command -v dnf &> /dev/null; then
                sudo dnf install -y postgresql postgresql-server postgresql-contrib
            else
                sudo yum install -y postgresql postgresql-server postgresql-contrib
            fi
            sudo postgresql-setup initdb 2>/dev/null || true
            sudo systemctl start postgresql
            sudo systemctl enable postgresql
            print_success "PostgreSQL installed and started"
            ;;
        "windows")
            print_error "Windows detected. Please manually install PostgreSQL:"
            print_error "  1. Download from https://www.postgresql.org/download/windows/"
            print_error "  2. Run the installer"
            print_error "  3. Add PostgreSQL bin directory to PATH"
            print_error "  4. Run this script again"
            exit 1
            ;;
        *)
            print_error "Unsupported OS. Please install PostgreSQL manually:"
            print_error "  Visit: https://www.postgresql.org/download/"
            exit 1
            ;;
    esac
}

# Function to setup database
setup_database() {
    print_status "Setting up Football Dynasty database..."
    print_status "======================================================"
    
    # Check if PostgreSQL is installed
    if ! command -v psql &> /dev/null; then
        print_warning "PostgreSQL not found. Installing..."
        install_postgresql
    else
        print_success "PostgreSQL found"
    fi
    
    # Check if PostgreSQL service is running
    if ! pg_isready -h localhost &> /dev/null; then
        print_warning "PostgreSQL service not running. Starting..."
        local os=$(detect_os)
        case $os in
            "macos")
                brew services start postgresql
                ;;
            "debian"|"redhat")
                sudo systemctl start postgresql
                ;;
        esac
        sleep 3
    fi
    
    # Verify PostgreSQL is running
    if ! pg_isready -h localhost &> /dev/null; then
        print_error "Failed to start PostgreSQL service"
        exit 1
    fi
    
    print_success "PostgreSQL service is running"
    
    # Set default database credentials
    DB_NAME="fd_db"
    DB_USER="fd_user"
    DEFAULT_PASSWORD="fd_password"
    
    # Prompt for database password
    echo ""
    print_status "Database Configuration:"
    print_status "  Database: $DB_NAME"
    print_status "  User: $DB_USER"
    echo ""
    echo -n "Enter password for database user (default: $DEFAULT_PASSWORD): "
    read -r DB_PASSWORD
    
    if [[ -z "$DB_PASSWORD" ]]; then
        DB_PASSWORD="$DEFAULT_PASSWORD"
    fi
    
    print_status "Creating database and user..."
    
    # Create database and user
    export PGPASSWORD=""  # Clear any existing password
    
    # Check if database already exists
    if psql -h localhost -U postgres -lqt | cut -d \| -f 1 | grep -qw "$DB_NAME"; then
        print_warning "Database '$DB_NAME' already exists"
    else
        psql -h localhost -U postgres -c "CREATE DATABASE $DB_NAME;" || {
            print_error "Failed to create database. You may need to:"
            print_error "  1. Set a password for postgres user: sudo -u postgres psql -c \"ALTER USER postgres PASSWORD 'your_password';\""
            print_error "  2. Or run: sudo -u postgres createdb $DB_NAME"
            exit 1
        }
        print_success "Database '$DB_NAME' created"
    fi
    
    # Check if user already exists
    if psql -h localhost -U postgres -t -c "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" | grep -q 1; then
        print_warning "User '$DB_USER' already exists"
        psql -h localhost -U postgres -c "ALTER USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" || {
            print_error "Failed to update user password"
            exit 1
        }
        print_success "Updated password for user '$DB_USER'"
    else
        psql -h localhost -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" || {
            print_error "Failed to create user"
            exit 1
        }
        print_success "User '$DB_USER' created"
    fi
    
    # Grant privileges
    psql -h localhost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;" || {
        print_error "Failed to grant privileges"
        exit 1
    }
    print_success "Privileges granted to '$DB_USER'"
    
    # Test connection
    print_status "Testing database connection..."
    export PGPASSWORD="$DB_PASSWORD"
    if psql -h localhost -d "$DB_NAME" -U "$DB_USER" -c "SELECT version();" > /dev/null 2>&1; then
        print_success "Database connection test successful!"
    else
        print_error "Database connection test failed"
        exit 1
    fi
    
    # Set up environment variable
    print_status "Setting up environment variable..."
    
    # Determine shell config file
    if [[ "$SHELL" == *"zsh"* ]]; then
        SHELL_CONFIG="$HOME/.zshrc"
    elif [[ "$SHELL" == *"bash"* ]]; then
        SHELL_CONFIG="$HOME/.bashrc"
    else
        SHELL_CONFIG="$HOME/.profile"
    fi
    
    # Add environment variable to shell config
    if grep -q "export DB_PASSWORD=" "$SHELL_CONFIG" 2>/dev/null; then
        print_warning "DB_PASSWORD already exists in $SHELL_CONFIG"
    else
        echo "" >> "$SHELL_CONFIG"
        echo "# Football Dynasty Database Configuration" >> "$SHELL_CONFIG"
        echo "export DB_PASSWORD=\"$DB_PASSWORD\"" >> "$SHELL_CONFIG"
        print_success "Added DB_PASSWORD to $SHELL_CONFIG"
    fi
    
    # Set for current session
    export DB_PASSWORD="$DB_PASSWORD"
    
    print_status "======================================================"
    print_success "🎉 Database setup completed successfully!"
    print_status ""
    print_status "📋 Configuration Summary:"
    print_status "  Database: $DB_NAME"
    print_status "  User: $DB_USER"
    print_status "  Password: $DB_PASSWORD"
    print_status "  Host: localhost"
    print_status "  Port: 5432"
    print_status ""
    print_status "🔧 Environment variable added to: $SHELL_CONFIG"
    print_status "💡 Restart your terminal or run: source $SHELL_CONFIG"
    print_status ""
    print_status "🚀 You can now start the application with:"
    print_status "  ./start.sh testing     # With mock data"
    print_status "  ./start.sh development # Clean environment"
    print_status ""
    
    return 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Check command/profile argument
COMMAND=${1:-development}

# Handle special commands
case $COMMAND in
    "setup-database")
        setup_database
        exit 0
        ;;
    "help"|"-h"|"--help")
        print_status "Football Dynasty - Start Script"
        print_status "======================================================"
        print_status "Usage:"
        print_status "  ./start.sh <profile>           - Start both services"
        print_status "  ./start.sh setup-database      - Setup PostgreSQL database"
        print_status "  ./start.sh help                - Show this help"
        print_status ""
        print_status "Profiles:"
        print_status "  development  - Clean environment (default)"
        print_status "  testing      - With comprehensive mock data"
        print_status "  production   - Production optimized"
        print_status ""
        print_status "Examples:"
        print_status "  ./start.sh setup-database     - First time setup"
        print_status "  ./start.sh testing            - Start with mock data"
        print_status "  ./start.sh development        - Start clean"
        exit 0
        ;;
esac

# Treat as profile for normal startup
PROFILE=$COMMAND

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
        print_error "Run './start.sh help' for usage information"
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