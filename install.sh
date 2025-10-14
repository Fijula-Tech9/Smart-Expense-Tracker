#!/bin/bash

# Smart Expense Tracker - Installation Script
# This script sets up the development environment

echo "🚀 Smart Expense Tracker - Installation Setup"
echo "=============================================="

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+')
    echo "✅ Java $JAVA_VERSION found"
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "⚠️  Warning: Java 17+ recommended (current: $JAVA_VERSION)"
    fi
else
    echo "❌ Java not found. Please install Java 17+"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    echo "✅ Maven found"
    mvn -version | head -1
else
    echo "❌ Maven not found. Please install Maven 3.6+"
    exit 1
fi

# Check MySQL
if command -v mysql &> /dev/null; then
    echo "✅ MySQL found"
    mysql --version
else
    echo "❌ MySQL not found. Please install MySQL 8.0+"
    echo "   macOS: brew install mysql"
    echo "   Ubuntu: sudo apt install mysql-server"
    exit 1
fi

echo ""
echo "🗄️ Database Setup"
echo "=================="
echo "Please make sure MySQL is running and create the database:"
echo ""
echo "mysql -u root -p"
echo "CREATE DATABASE expense_tracker;"
echo "exit;"
echo ""

# Prompt for database credentials
read -p "Enter MySQL username (default: root): " DB_USER
DB_USER=${DB_USER:-root}

read -s -p "Enter MySQL password: " DB_PASS
echo ""

# Update application.properties
echo ""
echo "⚙️ Configuring application..."

# Backup original file
cp src/main/resources/application.properties src/main/resources/application.properties.backup

# Update credentials
sed -i.bak "s/spring.datasource.username=root/spring.datasource.username=$DB_USER/g" src/main/resources/application.properties
sed -i.bak "s/spring.datasource.password=password/spring.datasource.password=$DB_PASS/g" src/main/resources/application.properties

echo "✅ Database configuration updated"

echo ""
echo "🔨 Building application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting application..."
    echo "Access the application at:"
    echo "   API Health: http://localhost:8080/api/auth/health"
    echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo ""
    
    # Run the application
    mvn spring-boot:run
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
