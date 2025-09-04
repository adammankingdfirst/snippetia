package com.snippetia.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.math.BigDecimal

/**
 * Advanced Enterprise Models for Global Scale Operations
 */

@Entity
@Table(name = "ai_models")
data class AIModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val modelId: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val version: String,
    
    @Enumerated(EnumType.STRING)
    val type: ModelType,
    
    @Enumerated(EnumType.STRING)
    val status: ModelStatus,
    
    @ElementCollection
    @CollectionTable(name = "ai_model_capabilities")
    val capabilities: Set<String> = emptySet(),
    
    @ElementCollection
    @CollectionTable(name = "ai_model_languages")
    val supportedLanguages: Set<String> = emptySet(),
    
    @Column(nullable = false)
    val accuracy: Double = 0.0,
    
    @Column(nullable = false)
    val latency: Long = 0L, // in milliseconds
    
    @Column(nullable = false)
    val throughput: Double = 0.0, // requests per second
    
    @Column(nullable = false)
    val costPerRequest: BigDecimal = BigDecimal.ZERO,
    
    @Column(columnDefinition = "TEXT")
    val configuration: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val metadata: String? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "global_regions")
data class GlobalRegion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val regionId: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val latitude: Double,
    
    @Column(nullable = false)
    val longitude: Double,
    
    @Enumerated(EnumType.STRING)
    val status: RegionStatus,
    
    @Column(nullable = false)
    val capacity: Int,
    
    @Column(nullable = false)
    val currentLoad: Double = 0.0,
    
    @Column(nullable = false)
    val costPerHour: BigDecimal,
    
    @ElementCollection
    @CollectionTable(name = "region_capabilities")
    val capabilities: Set<String> = emptySet(),
    
    @ElementCollection
    @CollectionTable(name = "region_compliance")
    val complianceFrameworks: Set<String> = emptySet(),
    
    @Column(columnDefinition = "TEXT")
    val configuration: String? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "security_incidents")
data class SecurityIncident(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val incidentId: String,
    
    @Enumerated(EnumType.STRING)
    val type: IncidentType,
    
    @Enumerated(EnumType.STRING)
    val severity: IncidentSeverity,
    
    @Enumerated(EnumType.STRING)
    val status: IncidentStatus,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_user_id")
    val affectedUser: User? = null,
    
    @ElementCollection
    @CollectionTable(name = "incident_affected_systems")
    val affectedSystems: Set<String> = emptySet(),
    
    @Column(columnDefinition = "TEXT")
    val detectionMethod: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val responseActions: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val forensicData: String? = null,
    
    val detectedAt: LocalDateTime,
    
    val resolvedAt: LocalDateTime? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "ml_experiments")
data class MLExperiment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val experimentId: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Enumerated(EnumType.STRING)
    val status: ExperimentStatus,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    val createdBy: User,
    
    @Column(columnDefinition = "TEXT")
    val configuration: String,
    
    @Column(columnDefinition = "TEXT")
    val hyperparameters: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val metrics: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val results: String? = null,
    
    @Column(nullable = false)
    val accuracy: Double = 0.0,
    
    @Column(nullable = false)
    val precision: Double = 0.0,
    
    @Column(nullable = false)
    val recall: Double = 0.0,
    
    @Column(nullable = false)
    val f1Score: Double = 0.0,
    
    val startedAt: LocalDateTime? = null,
    
    val completedAt: LocalDateTime? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "model_deployments")
data class ModelDeployment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val deploymentId: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    val model: AIModel,
    
    @Enumerated(EnumType.STRING)
    val deploymentType: DeploymentType,
    
    @Enumerated(EnumType.STRING)
    val status: DeploymentStatus,
    
    @Column(nullable = false)
    val trafficPercentage: Double = 0.0,
    
    @Column(nullable = false)
    val targetRegions: String, // JSON array of region IDs
    
    @Column(columnDefinition = "TEXT")
    val configuration: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val monitoringMetrics: String? = null,
    
    val deployedAt: LocalDateTime? = null,
    
    val rollbackAt: LocalDateTime? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "performance_metrics")
data class PerformanceMetric(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val entityType: String, // model, region, service, etc.
    
    @Column(nullable = false)
    val entityId: String,
    
    @Column(nullable = false)
    val metricName: String,
    
    @Column(nullable = false)
    val metricValue: Double,
    
    @Column(nullable = false)
    val unit: String,
    
    @Column(columnDefinition = "TEXT")
    val tags: String? = null, // JSON object for additional metadata
    
    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "compliance_audits")
data class ComplianceAudit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val auditId: String,
    
    @Enumerated(EnumType.STRING)
    val framework: ComplianceFramework,
    
    @Enumerated(EnumType.STRING)
    val status: AuditStatus,
    
    @Column(nullable = false)
    val scope: String, // What was audited
    
    @Column(nullable = false)
    val complianceScore: Double,
    
    @Column(columnDefinition = "TEXT")
    val findings: String, // JSON array of findings
    
    @Column(columnDefinition = "TEXT")
    val recommendations: String, // JSON array of recommendations
    
    @Column(columnDefinition = "TEXT")
    val evidence: String? = null, // JSON object with evidence
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id")
    val auditor: User,
    
    val auditDate: LocalDateTime,
    
    val nextAuditDate: LocalDateTime? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "feature_flags")
data class FeatureFlag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val flagKey: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(nullable = false)
    val enabled: Boolean = false,
    
    @Column(nullable = false)
    val rolloutPercentage: Double = 0.0,
    
    @ElementCollection
    @CollectionTable(name = "feature_flag_environments")
    val environments: Set<String> = emptySet(),
    
    @ElementCollection
    @CollectionTable(name = "feature_flag_user_segments")
    val userSegments: Set<String> = emptySet(),
    
    @Column(columnDefinition = "TEXT")
    val conditions: String? = null, // JSON object for complex conditions
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    val createdBy: User,
    
    val expiresAt: LocalDateTime? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "api_rate_limits")
data class ApiRateLimit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null,
    
    @Column(nullable = false)
    val identifier: String, // user ID, API key, IP address, etc.
    
    @Enumerated(EnumType.STRING)
    val identifierType: IdentifierType,
    
    @Column(nullable = false)
    val endpoint: String,
    
    @Column(nullable = false)
    val requestsPerMinute: Int,
    
    @Column(nullable = false)
    val requestsPerHour: Int,
    
    @Column(nullable = false)
    val requestsPerDay: Int,
    
    @Column(nullable = false)
    val currentMinuteCount: Int = 0,
    
    @Column(nullable = false)
    val currentHourCount: Int = 0,
    
    @Column(nullable = false)
    val currentDayCount: Int = 0,
    
    val lastResetMinute: LocalDateTime = LocalDateTime.now(),
    
    val lastResetHour: LocalDateTime = LocalDateTime.now(),
    
    val lastResetDay: LocalDateTime = LocalDateTime.now(),
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "system_configurations")
data class SystemConfiguration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val configKey: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val configValue: String,
    
    @Column(nullable = false)
    val configType: String, // string, number, boolean, json, etc.
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(nullable = false)
    val environment: String = "production",
    
    @Column(nullable = false)
    val encrypted: Boolean = false,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    val updatedBy: User? = null,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

// Enums for the advanced models
enum class ModelType {
    TRANSFORMER, CNN, RNN, ENSEMBLE, CUSTOM, GPT, BERT, LLAMA
}

enum class ModelStatus {
    TRAINING, VALIDATING, READY, DEPLOYED, DEPRECATED, FAILED
}

enum class RegionStatus {
    ACTIVE, MAINTENANCE, DEGRADED, OFFLINE
}

enum class IncidentType {
    SECURITY_BREACH, DATA_LEAK, UNAUTHORIZED_ACCESS, MALWARE, 
    DDOS_ATTACK, INSIDER_THREAT, COMPLIANCE_VIOLATION, OTHER
}

enum class IncidentSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class IncidentStatus {
    DETECTED, INVESTIGATING, CONTAINED, RESOLVED, CLOSED
}

enum class ExperimentStatus {
    CREATED, RUNNING, COMPLETED, FAILED, CANCELLED
}

enum class DeploymentType {
    BLUE_GREEN, CANARY, ROLLING, RECREATE
}

enum class DeploymentStatus {
    PENDING, DEPLOYING, DEPLOYED, ROLLING_BACK, ROLLED_BACK, FAILED
}

enum class ComplianceFramework {
    GDPR, CCPA, HIPAA, SOC2, ISO27001, PCI_DSS, NIST
}

enum class AuditStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, FAILED
}

enum class IdentifierType {
    USER_ID, API_KEY, IP_ADDRESS, SESSION_ID
}