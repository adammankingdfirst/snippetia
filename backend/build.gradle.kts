import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("org.flywaydb.flyway") version "9.22.3"
    id("jacoco")
    id("org.sonarqube") version "4.4.1.3373"
}

group = "com.snippetia"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    // Spring Boot Core
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    
    // Security & Authentication
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
    implementation("com.yubico:webauthn-server-core:2.5.0")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    
    // File Storage & Processing
    implementation("software.amazon.awssdk:s3:2.21.29")
    implementation("software.amazon.awssdk:sts:2.21.29")
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")
    
    // Code Analysis & Security
    implementation("com.github.spotbugs:spotbugs:4.8.1")
    implementation("org.owasp:dependency-check-core:8.4.3")
    implementation("net.sourceforge.pmd:pmd-core:6.55.0")
    implementation("org.sonarsource.sonarqube:sonar-plugin-api:9.17.0.78111")
    
    // Git Integration
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
    
    // AI Integration
    implementation("dev.langchain4j:langchain4j:0.24.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.24.0")
    implementation("dev.langchain4j:langchain4j-embeddings:0.24.0")
    
    // Code Execution & Sandboxing
    implementation("org.graalvm.sdk:graal-sdk:23.1.0")
    implementation("org.graalvm.js:js:23.1.0")
    implementation("org.graalvm.python:python:23.1.0")
    
    // Payment Processing
    implementation("com.stripe:stripe-java:24.1.0")
    implementation("com.paypal.sdk:checkout-sdk:2.0.0")
    
    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    
    // Monitoring & Observability
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    
    // Rate Limiting & Circuit Breaker
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-redis:7.6.0")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
    
    // Message Queue
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.springframework.kafka:spring-kafka")
    
    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:redis")
    testImplementation("org.testcontainers:elasticsearch")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.wiremock:wiremock-standalone:3.3.1")
    testImplementation("io.rest-assured:rest-assured:5.3.2")
    testImplementation("io.rest-assured:kotlin-extensions:5.3.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

jacoco {
    toolVersion = "0.8.8"
}

sonarqube {
    properties {
        property("sonar.projectKey", "snippetia-backend")
        property("sonar.organization", "snippetia")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}