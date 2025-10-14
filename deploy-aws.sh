#!/bin/bash

# Smart Expense Tracker - AWS Deployment Script
# This script deploys the application to AWS using CloudFormation and Elastic Beanstalk

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT=${1:-dev}
AWS_REGION=${2:-us-east-1}
STACK_NAME="expense-tracker-infrastructure-$ENVIRONMENT"
EB_APPLICATION_NAME="smart-expense-tracker"
EB_ENVIRONMENT_NAME="expense-tracker-$ENVIRONMENT"

echo -e "${BLUE}🚀 Deploying Smart Expense Tracker to AWS...${NC}"
echo -e "Environment: ${YELLOW}$ENVIRONMENT${NC}"
echo -e "Region: ${YELLOW}$AWS_REGION${NC}"

# Check prerequisites
echo -e "\n${BLUE}📋 Checking prerequisites...${NC}"

if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed. Please install it first.${NC}"
    exit 1
fi

if ! command -v eb &> /dev/null; then
    echo -e "${RED}❌ Elastic Beanstalk CLI is not installed. Please install it first.${NC}"
    exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}❌ AWS credentials not configured. Run 'aws configure' first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites check passed${NC}"

# Build application
echo -e "\n${BLUE}🔨 Building application...${NC}"
./mvnw clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Application build failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Application built successfully${NC}"

# Deploy infrastructure using CloudFormation
echo -e "\n${BLUE}🏗️  Deploying infrastructure...${NC}"

# Check if stack exists
if aws cloudformation describe-stacks --stack-name $STACK_NAME --region $AWS_REGION &> /dev/null; then
    echo -e "${YELLOW}📝 Stack $STACK_NAME already exists, updating...${NC}"
    STACK_ACTION="update-stack"
else
    echo -e "${YELLOW}📝 Creating new stack $STACK_NAME...${NC}"
    STACK_ACTION="create-stack"
fi

# Prompt for database password
echo -n "Enter database password for MySQL (min 8 characters): "
read -s DB_PASSWORD
echo

if [ ${#DB_PASSWORD} -lt 8 ]; then
    echo -e "${RED}❌ Password must be at least 8 characters long${NC}"
    exit 1
fi

# Deploy CloudFormation stack
aws cloudformation $STACK_ACTION \
    --stack-name $STACK_NAME \
    --template-body file://aws-infrastructure.yml \
    --parameters ParameterKey=Environment,ParameterValue=$ENVIRONMENT \
                 ParameterKey=KeyPairName,ParameterValue=expense-tracker-key \
                 ParameterKey=DBPassword,ParameterValue=$DB_PASSWORD \
    --capabilities CAPABILITY_IAM \
    --region $AWS_REGION

echo -e "${BLUE}⏳ Waiting for CloudFormation stack to complete...${NC}"
aws cloudformation wait stack-$STACK_ACTION-complete --stack-name $STACK_NAME --region $AWS_REGION

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Infrastructure deployed successfully${NC}"
else
    echo -e "${RED}❌ Infrastructure deployment failed${NC}"
    exit 1
fi

# Get stack outputs
DB_ENDPOINT=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $AWS_REGION \
    --query 'Stacks[0].Outputs[?OutputKey==`DatabaseEndpoint`].OutputValue' \
    --output text)

REDIS_ENDPOINT=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $AWS_REGION \
    --query 'Stacks[0].Outputs[?OutputKey==`RedisEndpoint`].OutputValue' \
    --output text)

echo -e "\n${BLUE}📊 Infrastructure Details:${NC}"
echo -e "Database Endpoint: ${YELLOW}$DB_ENDPOINT${NC}"
echo -e "Redis Endpoint: ${YELLOW}$REDIS_ENDPOINT${NC}"

# Initialize Elastic Beanstalk application
echo -e "\n${BLUE}🔧 Setting up Elastic Beanstalk...${NC}"

if ! eb list | grep -q $EB_APPLICATION_NAME; then
    echo -e "${YELLOW}📝 Initializing Elastic Beanstalk application...${NC}"
    eb init $EB_APPLICATION_NAME --platform "Corretto 17 running on 64bit Amazon Linux 2" --region $AWS_REGION
fi

# Create Elastic Beanstalk environment configuration
mkdir -p .ebextensions

cat > .ebextensions/01-environment.config << EOF
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: prod
    SERVER_PORT: 8080
    DB_HOST: $DB_ENDPOINT
    DB_NAME: expense_tracker
    DB_USERNAME: admin
    DB_PASSWORD: $DB_PASSWORD
    REDIS_HOST: $REDIS_ENDPOINT
    JWT_SECRET: \$(openssl rand -base64 32)
    
  aws:autoscaling:launchconfiguration:
    InstanceType: t3.medium
    IamInstanceProfile: aws-elasticbeanstalk-ec2-role
    
  aws:elasticbeanstalk:environment:
    LoadBalancerType: application
    ServiceRole: aws-elasticbeanstalk-service-role
    
  aws:autoscaling:asg:
    MinSize: 2
    MaxSize: 10
    
  aws:autoscaling:trigger:
    MeasureName: CPUUtilization
    Unit: Percent
    UpperThreshold: 70
    LowerThreshold: 20
    
  aws:elasticbeanstalk:healthreporting:system:
    SystemType: enhanced
    
  aws:elasticbeanstalk:application:
    Application Healthcheck URL: /actuator/health
EOF

# Deploy application to Elastic Beanstalk
echo -e "\n${BLUE}🚀 Deploying application to Elastic Beanstalk...${NC}"

if eb list | grep -q $EB_ENVIRONMENT_NAME; then
    echo -e "${YELLOW}📝 Environment $EB_ENVIRONMENT_NAME exists, deploying update...${NC}"
    eb deploy $EB_ENVIRONMENT_NAME
else
    echo -e "${YELLOW}📝 Creating new environment $EB_ENVIRONMENT_NAME...${NC}"
    eb create $EB_ENVIRONMENT_NAME --cname expense-tracker-$ENVIRONMENT
fi

# Wait for deployment to complete
echo -e "${BLUE}⏳ Waiting for deployment to complete...${NC}"
eb health $EB_ENVIRONMENT_NAME --refresh

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Application deployed successfully${NC}"
else
    echo -e "${RED}❌ Application deployment failed${NC}"
    exit 1
fi

# Get application URL
APP_URL=$(eb status $EB_ENVIRONMENT_NAME | grep "CNAME" | awk '{print $2}')

echo -e "\n${GREEN}🎉 Deployment completed successfully!${NC}"
echo -e "\n${BLUE}📱 Application Details:${NC}"
echo -e "Application URL: ${YELLOW}http://$APP_URL${NC}"
echo -e "Swagger UI: ${YELLOW}http://$APP_URL/swagger-ui.html${NC}"
echo -e "Health Check: ${YELLOW}http://$APP_URL/actuator/health${NC}"

echo -e "\n${BLUE}🔐 Database Access:${NC}"
echo -e "Endpoint: ${YELLOW}$DB_ENDPOINT:3306${NC}"
echo -e "Database: ${YELLOW}expense_tracker${NC}"
echo -e "Username: ${YELLOW}admin${NC}"

echo -e "\n${BLUE}📊 Monitoring:${NC}"
echo -e "CloudWatch Logs: AWS Console -> CloudWatch -> Log Groups -> /aws/elasticbeanstalk/$EB_ENVIRONMENT_NAME"
echo -e "Application Metrics: AWS Console -> CloudWatch -> Metrics -> AWS/ElasticBeanstalk"

echo -e "\n${BLUE}🛠️  Management Commands:${NC}"
echo -e "View logs: ${YELLOW}eb logs $EB_ENVIRONMENT_NAME${NC}"
echo -e "SSH to instance: ${YELLOW}eb ssh $EB_ENVIRONMENT_NAME${NC}"
echo -e "Scale application: ${YELLOW}eb scale <number> $EB_ENVIRONMENT_NAME${NC}"
echo -e "Deploy updates: ${YELLOW}eb deploy $EB_ENVIRONMENT_NAME${NC}"

echo -e "\n${GREEN}🎯 Deployment Summary:${NC}"
echo -e "✅ Infrastructure: CloudFormation stack deployed"
echo -e "✅ Database: MySQL RDS instance running"
echo -e "✅ Cache: Redis ElastiCache cluster active"
echo -e "✅ Application: Spring Boot app deployed on Elastic Beanstalk"
echo -e "✅ Load Balancer: Application Load Balancer configured"
echo -e "✅ Auto Scaling: Configured for 2-10 instances"

echo -e "\n${YELLOW}💡 Next Steps:${NC}"
echo -e "1. Test the application at: http://$APP_URL"
echo -e "2. Configure custom domain name (Route 53)"
echo -e "3. Set up SSL certificate (AWS Certificate Manager)"
echo -e "4. Configure monitoring alerts (CloudWatch)"
echo -e "5. Set up CI/CD pipeline (GitHub Actions)"

echo -e "\n${GREEN}Happy deploying! 🚀${NC}"
