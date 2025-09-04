#!/bin/bash

# Snippetia Deployment Script
# This script handles the complete deployment of Snippetia platform

set -e

echo "🚀 Starting Snippetia Deployment..."

# Check prerequisites
check_prerequisites() {
    echo "📋 Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo "❌ Docker Compose is not installed"
        exit 1
    fi
    
    echo "✅ Prerequisites check passed"
}

# Build application
build_application() {
    echo "🔨 Building application..."
    
    # Build backend
    echo "📦 Building backend..."
    cd backend
    ./gradlew clean build -x test
    cd ..
    
    # Build frontend
    echo "🎨 Building frontend..."
    cd frontend
    ./gradlew build
    cd ..
    
    echo "✅ Application built successfully"
}

# Deploy with Docker Compose
deploy_docker() {
    echo "🐳 Deploying with Docker Compose..."
    
    # Copy environment template if not exists
    if [ ! -f .env ]; then
        cp .env.example .env
        echo "⚠️  Please configure .env file with your settings"
    fi
    
    # Start services
    docker-compose up -d
    
    echo "✅ Services started successfully"
}

# Health check
health_check() {
    echo "🏥 Performing health checks..."
    
    # Wait for backend to be ready
    echo "⏳ Waiting for backend to be ready..."
    timeout 300 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 5; done'
    
    echo "✅ Backend is healthy"
    echo "✅ Deployment completed successfully!"
    
    echo ""
    echo "🌐 Access your application:"
    echo "   Backend API: http://localhost:8080"
    echo "   Swagger UI:  http://localhost:8080/swagger-ui.html"
    echo "   Grafana:     http://localhost:3000 (admin/admin)"
    echo ""
}

# Main deployment flow
main() {
    check_prerequisites
    build_application
    deploy_docker
    health_check
}

# Run deployment
main "$@"