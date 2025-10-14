#!/bin/bash

# Smart Expense Tracker - MySQL Setup Script
# This script sets up MySQL database for the expense tracker application

echo "🗄️ Setting up MySQL for Smart Expense Tracker..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}❌ MySQL is not installed. Installing via Homebrew...${NC}"
    
    if ! command -v brew &> /dev/null; then
        echo -e "${RED}❌ Homebrew is not installed. Please install Homebrew first:${NC}"
        echo "/bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
        exit 1
    fi
    
    echo "📦 Installing MySQL..."
    brew install mysql
    
    echo "🚀 Starting MySQL service..."
    brew services start mysql
    
    echo -e "${YELLOW}⚠️  Please run 'mysql_secure_installation' to secure your MySQL installation${NC}"
else
    echo -e "${GREEN}✅ MySQL is already installed${NC}"
fi

# Check if MySQL is running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "🚀 Starting MySQL service..."
    brew services start mysql
    sleep 3
fi

# Database setup
echo "📊 Setting up expense_tracker database..."

# Prompt for MySQL root password
echo -n "Enter MySQL root password (press Enter if no password): "
read -s MYSQL_PASSWORD
echo

# Create database and user
if [ -z "$MYSQL_PASSWORD" ]; then
    mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
SELECT 'Database and user created successfully!' as Result;
EOF
else
    mysql -u root -p$MYSQL_PASSWORD <<EOF
CREATE DATABASE IF NOT EXISTS expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
SELECT 'Database and user created successfully!' as Result;
EOF
fi

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Database setup completed successfully!${NC}"
    echo
    echo "📋 Database Configuration:"
    echo "  • Database: expense_tracker"
    echo "  • Username: expense_user"
    echo "  • Password: expense_password"
    echo "  • Host: localhost"
    echo "  • Port: 3306"
    echo
    echo "🎯 Next Steps:"
    echo "  1. Update application-prod.properties with your database credentials"
    echo "  2. Run: ./mvnw spring-boot:run -Dspring.profiles.active=prod"
    echo "  3. Or keep using dev profile with H2: ./mvnw spring-boot:run -Dspring.profiles.active=dev"
    echo
else
    echo -e "${RED}❌ Database setup failed. Please check MySQL installation and try again.${NC}"
    exit 1
fi
