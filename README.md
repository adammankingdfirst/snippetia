# 🚀 Snippetia - The Ultimate Developer Code Sharing Platform

> **Status**: ✅ **Refactored and Organized** - The project has been completely refactored with clean architecture, proper package structure, and all missing files generated.

## 🔧 **Recent Refactoring**

This project has been completely refactored and organized:

### ✅ **Backend Improvements**
- ✅ Removed duplicate `com.codeshare` package structure
- ✅ Consolidated all code under `com.snippetia` package
- ✅ Created missing service files (`AuthService`, `SnippetService`, `UserService`, etc.)
- ✅ Generated missing repository interfaces
- ✅ Added proper security configuration with JWT
- ✅ Created missing entity/model files
- ✅ Added exception handling classes
- ✅ Fixed all import issues and dependencies
- ✅ Added comprehensive test configuration
- ✅ Created application.properties with proper configuration

### ✅ **Frontend Improvements**
- ✅ Removed duplicate UI structures (`ui` vs `presentation`)
- ✅ Consolidated under clean `presentation` architecture
- ✅ Created missing domain models and DTOs
- ✅ Added proper theme and component structure
- ✅ Fixed all import issues and missing files
- ✅ Added proper navigation with Voyager
- ✅ Created platform-specific main functions (Android, Desktop, JS)
- ✅ Added proper dependency injection with Koin
- ✅ Created version catalog for dependency management

### ✅ **Project Structure**
- ✅ Clean separation of concerns
- ✅ Proper package organization
- ✅ No duplicate or redundant files
- ✅ All missing files generated
- ✅ Ready for development and deployment

## 🚀 Snippetia - The Ultimate Developer Code Sharing Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/snippetia/snippetia/workflows/CI/badge.svg)](https://github.com/snippetia/snippetia/actions)
[![Coverage](https://codecov.io/gh/snippetia/snippetia/branch/main/graph/badge.svg)](https://codecov.io/gh/snippetia/snippetia)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=snippetia&metric=security_rating)](https://sonarcloud.io/dashboard?id=snippetia)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=snippetia&metric=alert_status)](https://sonarcloud.io/dashboard?id=snippetia)

Snippetia is the world's most advanced, secure, and feature-rich platform where developers can create, share, discover, and collaborate on code snippets. Built with cutting-edge technologies and enterprise-grade security, it provides an unparalleled experience across web, mobile, and desktop platforms.

## ✨ **Key Features**

### 🌟 **Core Platform Features**
- **🔄 Multi-platform Support**: Native apps for Web, Android, iOS, and Desktop
- **📝 Advanced Code Editor**: Monaco-powered editor with 50+ language support
- **🔍 Intelligent Search**: AI-powered search with semantic understanding
- **👥 Social Collaboration**: Follow, like, fork, comment, and real-time collaboration
- **📊 Version Control**: Git-like versioning with diff visualization
- **🏷️ Smart Organization**: Categories, tags, and AI-powered auto-tagging
- **⚡ Code Execution**: Run and test snippets in secure sandboxed environments
- **🔗 Git Integration**: Seamless sync with GitHub, GitLab, Bitbucket
- **📱 Offline Support**: Full offline functionality with smart synchronization

### 🔐 **Enterprise Security**
- **🛡️ Multi-factor Authentication**: Email/password, OAuth2, WebAuthn (passkeys)
- **🔒 OAuth Providers**: GitHub, Discord, Twitch, Google, Microsoft, and more
- **🦠 Virus Scanning**: Real-time malware detection with VirusTotal integration
- **🔍 Security Analysis**: Static code analysis and vulnerability detection
- **🚨 Threat Detection**: AI-powered malicious code pattern recognition
- **👮 Role-based Access**: Granular permissions with User, Admin, Super Admin roles
- **🛡️ Rate Limiting**: Advanced DDoS protection and abuse prevention
- **🔐 End-to-end Encryption**: All sensitive data encrypted at rest and in transit

### 🤖 **AI-Powered Features**
- **💡 Code Suggestions**: Intelligent code completion and optimization
- **📖 Documentation Generation**: Auto-generate documentation from code
- **🔍 Code Analysis**: Quality scoring, complexity analysis, and best practices
- **🏷️ Auto-tagging**: Smart categorization and tag suggestions
- **🔧 Code Refactoring**: AI-assisted code improvements
- **🐛 Bug Detection**: Automated bug and vulnerability identification
- **📚 Learning Recommendations**: Personalized learning paths and resources

### 💰 **Monetization & Support**
- **☕ "Buy Me a Coffee"**: Support your favorite developers
- **💳 Integrated Payments**: Stripe and PayPal integration
- **👑 Premium Features**: Advanced analytics, private repositories, priority support
- **🎁 Sponsorship System**: Corporate sponsorship and developer funding
- **💎 NFT Integration**: Unique code snippet NFTs and collectibles

### 🔧 **Developer Tools Integration**
- **🔄 CI/CD Integration**: Jenkins, GitHub Actions, GitLab CI, Azure DevOps
- **📊 Analytics**: Comprehensive usage analytics and insights
- **🔗 API-first Design**: RESTful APIs with GraphQL support
- **🔌 Webhook Support**: Real-time notifications and integrations
- **📱 Mobile SDKs**: Native mobile development kits
- **🖥️ Desktop Apps**: Electron-based desktop applications
- **🌐 Browser Extensions**: Chrome, Firefox, Safari extensions

### 📈 **Advanced Analytics**
- **📊 Usage Metrics**: Detailed analytics for snippets and users
- **🎯 Trending Analysis**: Real-time trending snippets and technologies
- **👥 Community Insights**: Developer community analytics
- **📈 Performance Monitoring**: Application performance metrics
- **🔍 Search Analytics**: Search patterns and optimization insights
- **💹 Revenue Analytics**: Monetization and payment analytics

## 🏗️ **Architecture**

### 🔧 **Backend (Spring Boot + Kotlin)**
```
📦 Backend Architecture
├── 🎯 Domain-Driven Design (DDD)
├── 🏛️ Hexagonal Architecture
├── 🔄 CQRS + Event Sourcing
├── 🚀 Microservices Ready
├── 📊 PostgreSQL + MongoDB + Redis
├── 🔍 Elasticsearch + AI Search
├── 🔒 Advanced Security Layer
├── 📨 Event-Driven Architecture
├── 🧪 Comprehensive Testing
└── 📈 Observability & Monitoring
```

**Technologies:**
- **Framework**: Spring Boot 3.2 with Kotlin
- **Database**: PostgreSQL (primary), MongoDB (analytics), Redis (cache)
- **Search**: Elasticsearch with AI-powered semantic search
- **Security**: Spring Security + JWT + WebAuthn + OAuth2
- **Messaging**: Apache Kafka + RabbitMQ
- **Storage**: MinIO (S3-compatible) for file storage
- **Monitoring**: Prometheus + Grafana + Jaeger
- **Testing**: JUnit 5 + MockK + Testcontainers

### 🎨 **Frontend (Kotlin Multiplatform)**
```
📱 Frontend Architecture
├── 🎨 Compose Multiplatform UI
├── 🏗️ Clean Architecture
├── 🔄 MVI Pattern
├── 🌐 Cross-platform Sharing
├── 📱 Platform-specific Features
├── 🔄 Offline-first Design
├── 🎭 Material 3 Design System
├── 🚀 Performance Optimized
├── 🧪 Comprehensive Testing
└── ♿ Accessibility Support
```

**Platforms:**
- **Web**: Compose for Web with modern browser support
- **Android**: Native Android with Jetpack Compose
- **iOS**: Native iOS with Compose Multiplatform
- **Desktop**: JVM desktop with native look and feel

### ☁️ **Infrastructure**
```
🏗️ Infrastructure Stack
├── 🐳 Docker + Kubernetes
├── 🔄 CI/CD Pipelines
├── 🌐 CDN + Load Balancing
├── 🔒 SSL/TLS Encryption
├── 🛡️ WAF + DDoS Protection
├── 📊 Monitoring & Alerting
├── 💾 Automated Backups
├── 🔄 Auto-scaling
├── 🌍 Multi-region Deployment
└── 🚨 Disaster Recovery
```

## 🚀 **Quick Start**

### 📋 **Prerequisites**
- **Java 17+** ☕
- **Docker & Docker Compose** 🐳
- **Node.js 18+** (for web development) 🟢
- **Android Studio** (for Android development) 📱
- **Xcode** (for iOS development) 🍎

### ⚡ **One-Command Setup**
```bash
# Clone the repository
git clone https://github.com/snippetia/snippetia.git
cd snippetia

# Start the entire platform
docker-compose up -d

# Wait for services to be ready (about 2-3 minutes)
docker-compose logs -f backend
```

### 🌐 **Access Points**
- **🌍 Web App**: http://localhost:3000
- **🔧 API**: http://localhost:8080
- **📚 API Docs**: http://localhost:8080/swagger-ui.html
- **📊 Grafana**: http://localhost:3001 (admin/admin123)
- **🔍 Kibana**: http://localhost:5601
- **📈 Prometheus**: http://localhost:9090
- **🎯 Jaeger**: http://localhost:16686

## 🛠️ **Development Setup**

### 🔧 **Backend Development**
```bash
cd backend

# Run with development profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Generate test coverage report
./gradlew jacocoTestReport
```

### 🎨 **Frontend Development**
```bash
cd frontend

# Web development
./gradlew jsRun

# Desktop development
./gradlew runDesktop

# Android development
./gradlew assembleDebug

# iOS development (macOS only)
./gradlew iosSimulatorArm64Test
```

### 🧪 **Testing**
```bash
# Run all tests
./scripts/run-tests.sh

# Run specific test suites
./gradlew backend:test
./gradlew frontend:allTests

# Run E2E tests
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

## 🔧 **Configuration**

### 🌍 **Environment Variables**

#### 🔧 **Backend Configuration**
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=snippetia
DB_USERNAME=snippetia
DB_PASSWORD=your-secure-password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# Security
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
ENCRYPTION_KEY=your-32-character-encryption-key

# OAuth Providers
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
DISCORD_CLIENT_ID=your-discord-client-id
DISCORD_CLIENT_SECRET=your-discord-client-secret
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# AI Services
OPENAI_API_KEY=your-openai-api-key
HUGGINGFACE_API_KEY=your-huggingface-api-key

# Payment Processing
STRIPE_SECRET_KEY=your-stripe-secret-key
PAYPAL_CLIENT_ID=your-paypal-client-id
PAYPAL_CLIENT_SECRET=your-paypal-client-secret

# External Services
VIRUSTOTAL_API_KEY=your-virustotal-api-key
SENTRY_DSN=your-sentry-dsn
```

#### 🎨 **Frontend Configuration**
```env
# API Configuration
VITE_API_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws

# Payment
VITE_STRIPE_PUBLISHABLE_KEY=your-stripe-publishable-key

# Analytics
VITE_GOOGLE_ANALYTICS_ID=your-ga-id
VITE_MIXPANEL_TOKEN=your-mixpanel-token

# Monitoring
VITE_SENTRY_DSN=your-sentry-dsn
```

### 🔐 **OAuth Provider Setup**

#### 🐙 **GitHub OAuth**
1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Create a new OAuth App
3. Set Authorization callback URL: `http://localhost:8080/oauth2/callback/github`
4. Copy Client ID and Client Secret

#### 🎮 **Discord OAuth**
1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Go to OAuth2 settings
4. Add redirect URI: `http://localhost:8080/oauth2/callback/discord`
5. Copy Client ID and Client Secret

#### 🎯 **Twitch OAuth**
1. Go to [Twitch Developer Console](https://dev.twitch.tv/console)
2. Create a new application
3. Set OAuth Redirect URL: `http://localhost:8080/oauth2/callback/twitch`
4. Copy Client ID and Client Secret

## 🧪 **Testing Strategy**

### 🔬 **Backend Testing**
```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# Contract tests
./gradlew contractTest

# Performance tests
./gradlew performanceTest

# Security tests
./gradlew securityTest
```

### 🎨 **Frontend Testing**
```bash
# Unit tests
./gradlew commonTest

# UI tests
./gradlew androidConnectedTest

# Cross-platform tests
./gradlew allTests
```

### 🌐 **End-to-End Testing**
```bash
# Full E2E test suite
./scripts/e2e-tests.sh

# Specific platform tests
./scripts/test-web.sh
./scripts/test-mobile.sh
./scripts/test-desktop.sh
```

## 📊 **Monitoring & Observability**

### 📈 **Metrics & Dashboards**
- **Application Metrics**: Business KPIs, user engagement, code snippet analytics
- **System Metrics**: JVM, database, Redis, and infrastructure metrics
- **API Metrics**: Request rates, response times, error rates, and SLA monitoring
- **Security Metrics**: Authentication attempts, security events, and threat detection

### 🚨 **Alerting**
- **High Error Rate**: >5% error rate for 5 minutes
- **High Response Time**: >2s average response time
- **Database Issues**: Connection pool exhaustion or slow queries
- **Security Events**: Multiple failed login attempts or suspicious activity
- **Resource Usage**: High CPU, memory, or disk usage

### 📊 **Custom Dashboards**
- **Executive Dashboard**: High-level business metrics and KPIs
- **Developer Dashboard**: Code quality, deployment frequency, and team productivity
- **Operations Dashboard**: System health, performance, and infrastructure metrics
- **Security Dashboard**: Security events, compliance status, and threat intelligence

## 🔒 **Security Features**

### 🛡️ **Application Security**
- **Input Validation**: Comprehensive validation on all inputs with sanitization
- **SQL Injection Protection**: Parameterized queries and ORM best practices
- **XSS Protection**: Content Security Policy and output encoding
- **CSRF Protection**: CSRF tokens on all state-changing operations
- **Rate Limiting**: Intelligent rate limiting per user, IP, and endpoint
- **Security Headers**: HSTS, X-Frame-Options, X-Content-Type-Options

### 🔍 **Code Security Scanning**
- **Static Analysis**: SpotBugs, PMD, and SonarQube integration
- **Dependency Scanning**: OWASP Dependency Check and Snyk integration
- **Virus Scanning**: Real-time scanning with VirusTotal API
- **Content Filtering**: AI-powered malicious code pattern detection
- **License Compliance**: Automated license scanning and compliance checking

### 🔐 **Data Protection**
- **Encryption at Rest**: AES-256 encryption for all sensitive data
- **Encryption in Transit**: TLS 1.3 for all communications
- **Key Management**: Hardware Security Module (HSM) integration
- **Data Anonymization**: GDPR-compliant data anonymization
- **Audit Logging**: Comprehensive audit trails for all operations

## 🚀 **Deployment**

### 🐳 **Docker Deployment**
```bash
# Production deployment
docker-compose -f docker-compose.prod.yml up -d

# Scaling services
docker-compose up -d --scale backend=3 --scale frontend-web=2
```

### ☸️ **Kubernetes Deployment**
```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Scale deployment
kubectl scale deployment snippetia-backend --replicas=5
```

### ☁️ **Cloud Deployment**

#### 🌩️ **AWS Deployment**
```bash
# Deploy to AWS ECS
aws ecs create-service --cli-input-json file://aws/ecs-service.json

# Deploy to AWS EKS
eksctl create cluster -f aws/eks-cluster.yaml
```

#### 🌐 **Azure Deployment**
```bash
# Deploy to Azure Container Instances
az container create --resource-group snippetia --file azure/container-group.yaml
```

#### ☁️ **Google Cloud Deployment**
```bash
# Deploy to Google Cloud Run
gcloud run deploy --source . --platform managed --region us-central1
```

## 📈 **Performance Optimization**

### 🔧 **Backend Performance**
- **Database Optimization**: Query optimization, proper indexing, connection pooling
- **Caching Strategy**: Multi-level caching with Redis and application-level caching
- **Async Processing**: Background jobs for heavy operations with Kafka
- **Connection Pooling**: HikariCP for optimal database connections
- **JVM Tuning**: Optimized JVM settings for production workloads

### 🎨 **Frontend Performance**
- **Code Splitting**: Lazy loading of routes and components
- **Image Optimization**: WebP format with progressive loading
- **Bundle Optimization**: Tree shaking, minification, and compression
- **Caching**: Service worker for offline support and caching
- **Performance Monitoring**: Real User Monitoring (RUM) with Core Web Vitals

### 🌐 **Infrastructure Performance**
- **CDN**: Global content delivery network for static assets
- **Load Balancing**: Intelligent load balancing with health checks
- **Auto-scaling**: Horizontal and vertical auto-scaling based on metrics
- **Database Sharding**: Horizontal database scaling for large datasets
- **Microservices**: Service decomposition for independent scaling

## 🤝 **Contributing**

We welcome contributions from developers worldwide! Please see our [Contributing Guide](CONTRIBUTING.md) for detailed information.

### 🔄 **Development Workflow**
1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Make** your changes with proper tests
4. **Run** the test suite (`./scripts/run-tests.sh`)
5. **Commit** your changes (`git commit -m 'Add amazing feature'`)
6. **Push** to the branch (`git push origin feature/amazing-feature`)
7. **Submit** a pull request

### 📝 **Code Standards**
- **Backend**: Kotlin coding conventions with ktlint
- **Frontend**: Compose best practices and Material Design guidelines
- **Documentation**: Clear and comprehensive documentation
- **Testing**: Minimum 80% code coverage required
- **Security**: Security review required for all changes

### 🏆 **Recognition**
- **Contributors**: Featured in our Hall of Fame
- **Maintainers**: Special recognition and swag
- **Top Contributors**: Annual awards and conference invitations

## 📄 **License**

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 🆘 **Support & Community**

### 📚 **Documentation**
- **📖 User Guide**: [docs.snippetia.dev](https://docs.snippetia.dev)
- **🔧 API Documentation**: [api.snippetia.dev](https://api.snippetia.dev)
- **👨‍💻 Developer Docs**: [dev.snippetia.dev](https://dev.snippetia.dev)

### 💬 **Community**
- **🐛 Issues**: [GitHub Issues](https://github.com/snippetia/snippetia/issues)
- **💡 Discussions**: [GitHub Discussions](https://github.com/snippetia/snippetia/discussions)
- **💬 Discord**: [Community Discord](https://discord.gg/snippetia)
- **🐦 Twitter**: [@SnippetiaApp](https://twitter.com/SnippetiaApp)
- **📧 Email**: support@snippetia.dev

### 🎯 **Professional Support**
- **🏢 Enterprise Support**: enterprise@snippetia.dev
- **🔒 Security Issues**: security@snippetia.dev
- **📈 Partnership**: partnerships@snippetia.dev

## 🗺️ **Roadmap**

### 🎯 **Q1 2024**
- [ ] 📱 Mobile app beta release (iOS & Android)
- [ ] 🤖 Advanced AI code analysis and suggestions
- [ ] 👥 Team collaboration features and workspaces
- [ ] 🔧 Enhanced API rate limiting and quotas
- [ ] 🌐 Multi-language support (i18n)

### 🚀 **Q2 2024**
- [ ] 🧠 AI-powered code generation and completion
- [ ] 🔍 Advanced search with natural language queries
- [ ] 🛒 Integration marketplace for third-party tools
- [ ] ⚡ Performance optimizations and caching improvements
- [ ] 📊 Advanced analytics and reporting dashboard

### 🌟 **Q3 2024**
- [ ] 🏢 Enterprise features and SSO integration
- [ ] 📈 Advanced analytics with machine learning insights
- [ ] 🌍 Multi-region deployment and CDN
- [ ] 📱 Mobile app store release
- [ ] 🎮 Gamification and developer achievements

### 🔮 **Q4 2024**
- [ ] 🤖 AI pair programming assistant
- [ ] 🔗 Blockchain integration for code ownership
- [ ] 🎯 Advanced personalization and recommendations
- [ ] 🌐 Global developer conference and community events
- [ ] 🚀 IPO preparation and scaling initiatives

## 🙏 **Acknowledgments**

### 🏆 **Technology Partners**
- **☕ JetBrains** for Kotlin and IntelliJ IDEA
- **🍃 Spring Team** for the excellent Spring Boot framework
- **🎨 Google** for Material Design and Compose Multiplatform
- **🐳 Docker** for containerization technology
- **☸️ Kubernetes** for orchestration platform

### 🌟 **Open Source Heroes**
- **🔒 OWASP** for security best practices and tools
- **📊 Elastic** for search and analytics capabilities
- **📈 Prometheus** for monitoring and alerting
- **🎯 Grafana** for beautiful dashboards and visualization

### 💝 **Special Thanks**
- **🌍 Open Source Community** for amazing libraries and tools
- **👨‍💻 Beta Testers** for valuable feedback and bug reports
- **🎨 Design Community** for inspiration and design patterns
- **🔒 Security Researchers** for responsible disclosure and improvements

---

<div align="center">

**Built with ❤️ by the Snippetia Team**

[🌟 Star us on GitHub](https://github.com/snippetia/snippetia) • [🐦 Follow on Twitter](https://twitter.com/SnippetiaApp) • [💬 Join Discord](https://discord.gg/snippetia)

**Making code sharing beautiful, secure, and collaborative for developers worldwide** 🌍

</div>