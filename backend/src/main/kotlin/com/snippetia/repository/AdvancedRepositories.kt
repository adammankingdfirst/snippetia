package com.snippetia.repository

import com.snippetia.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Advanced Enterprise Repositories for Global Scale Operations
 */

@Repository
interface AIModelRepository : JpaRepository<AIModel, Long> {
    
    fun findByModelId(modelId: String): AIModel?
    
    fun findByStatus(status: ModelStatus): List<AIModel>
    
    fun findByType(type: ModelType): List<AIModel>
    
    @Query("SELECT m FROM AIModel m WHERE m.status = :status AND :capability MEMBER OF m.capabilities")
    fun findByStatusAndCapability(@Param("status") status: ModelStatus, @Param("capability") capability: String): List<AIModel>
    
    @Query("SELECT m FROM AIModel m WHERE :language MEMBER OF m.supportedLanguages AND m.status = 'READY'")
    fun findByLanguageSupport(@Param("language") language: String): List<AIModel>
    
    @Query("SELECT m FROM AIModel m WHERE m.accuracy >= :minAccuracy AND m.latency <= :maxLatency ORDER BY m.accuracy DESC")
    fun findOptimalModels(@Param("minAccuracy") minAccuracy: Double, @Param("maxLatency") maxLatency: Long): List<AIModel>
    
    @Query("SELECT m FROM AIModel m WHERE m.status = 'READY' ORDER BY (m.accuracy * 0.6 + (1000.0 / m.latency) * 0.4) DESC")
    fun findBestPerformingModels(): List<AIModel>
}

@Repository
interface GlobalRegionRepository : JpaRepository<GlobalRegion, Long> {
    
    fun findByRegionId(regionId: String): GlobalRegion?
    
    fun findByStatus(status: RegionStatus): List<GlobalRegion>
    
    @Query("SELECT r FROM GlobalRegion r WHERE r.status = 'ACTIVE' AND r.currentLoad < :maxLoad")
    fun findAvailableRegions(@Param("maxLoad") maxLoad: Double): List<GlobalRegion>
    
    @Query("SELECT r FROM GlobalRegion r WHERE :capability MEMBER OF r.capabilities AND r.status = 'ACTIVE'")
    fun findByCapability(@Param("capability") capability: String): List<GlobalRegion>
    
    @Query("SELECT r FROM GlobalRegion r WHERE :framework MEMBER OF r.complianceFrameworks")
    fun findByComplianceFramework(@Param("framework") framework: String): List<GlobalRegion>
    
    @Query("""
        SELECT r FROM GlobalRegion r 
        WHERE r.status = 'ACTIVE' 
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * 
                  cos(radians(r.longitude) - radians(:lon)) + 
                  sin(radians(:lat)) * sin(radians(r.latitude))))
    """)
    fun findNearestRegions(@Param("lat") latitude: Double, @Param("lon") longitude: Double): List<GlobalRegion>
    
    @Query("SELECT r FROM GlobalRegion r WHERE r.status = 'ACTIVE' ORDER BY r.costPerHour ASC")
    fun findMostCostEffectiveRegions(): List<GlobalRegion>
}

@Repository
interface SecurityIncidentRepository : JpaRepository<SecurityIncident, Long> {
    
    fun findByIncidentId(incidentId: String): SecurityIncident?
    
    fun findByStatus(status: IncidentStatus): List<SecurityIncident>
    
    fun findBySeverity(severity: IncidentSeverity): List<SecurityIncident>
    
    fun findByType(type: IncidentType): List<SecurityIncident>
    
    @Query("SELECT i FROM SecurityIncident i WHERE i.affectedUser = :user ORDER BY i.detectedAt DESC")
    fun findByAffectedUser(@Param("user") user: User): List<SecurityIncident>
    
    @Query("SELECT i FROM SecurityIncident i WHERE i.detectedAt >= :since AND i.status != 'CLOSED'")
    fun findActiveIncidentsSince(@Param("since") since: LocalDateTime): List<SecurityIncident>
    
    @Query("SELECT i FROM SecurityIncident i WHERE i.severity IN ('HIGH', 'CRITICAL') AND i.status != 'RESOLVED'")
    fun findCriticalOpenIncidents(): List<SecurityIncident>
    
    @Query("SELECT COUNT(i) FROM SecurityIncident i WHERE i.detectedAt >= :since")
    fun countIncidentsSince(@Param("since") since: LocalDateTime): Long
    
    @Query("SELECT i.type, COUNT(i) FROM SecurityIncident i WHERE i.detectedAt >= :since GROUP BY i.type")
    fun getIncidentTypeStatistics(@Param("since") since: LocalDateTime): List<Array<Any>>
}

@Repository
interface MLExperimentRepository : JpaRepository<MLExperiment, Long> {
    
    fun findByExperimentId(experimentId: String): MLExperiment?
    
    fun findByStatus(status: ExperimentStatus): List<MLExperiment>
    
    fun findByCreatedBy(user: User): List<MLExperiment>
    
    @Query("SELECT e FROM MLExperiment e WHERE e.createdBy = :user ORDER BY e.createdAt DESC")
    fun findByCreatedByOrderByCreatedAtDesc(@Param("user") user: User): List<MLExperiment>
    
    @Query("SELECT e FROM MLExperiment e WHERE e.status = 'COMPLETED' ORDER BY e.accuracy DESC")
    fun findBestPerformingExperiments(): List<MLExperiment>
    
    @Query("SELECT e FROM MLExperiment e WHERE e.startedAt >= :since AND e.status = 'COMPLETED'")
    fun findCompletedExperimentsSince(@Param("since") since: LocalDateTime): List<MLExperiment>
    
    @Query("SELECT AVG(e.accuracy) FROM MLExperiment e WHERE e.status = 'COMPLETED' AND e.completedAt >= :since")
    fun getAverageAccuracySince(@Param("since") since: LocalDateTime): Double?
    
    @Query("SELECT COUNT(e) FROM MLExperiment e WHERE e.createdBy = :user AND e.createdAt >= :since")
    fun countExperimentsByUserSince(@Param("user") user: User, @Param("since") since: LocalDateTime): Long
}

@Repository
interface ModelDeploymentRepository : JpaRepository<ModelDeployment, Long> {
    
    fun findByDeploymentId(deploymentId: String): ModelDeployment?
    
    fun findByModel(model: AIModel): List<ModelDeployment>
    
    fun findByStatus(status: DeploymentStatus): List<ModelDeployment>
    
    fun findByDeploymentType(type: DeploymentType): List<ModelDeployment>
    
    @Query("SELECT d FROM ModelDeployment d WHERE d.status = 'DEPLOYED' AND d.trafficPercentage > 0")
    fun findActiveDeployments(): List<ModelDeployment>
    
    @Query("SELECT d FROM ModelDeployment d WHERE d.model = :model AND d.status = 'DEPLOYED'")
    fun findActiveDeploymentsByModel(@Param("model") model: AIModel): List<ModelDeployment>
    
    @Query("SELECT d FROM ModelDeployment d WHERE d.deploymentType = 'CANARY' AND d.status = 'DEPLOYED'")
    fun findActiveCanaryDeployments(): List<ModelDeployment>
    
    @Query("SELECT SUM(d.trafficPercentage) FROM ModelDeployment d WHERE d.status = 'DEPLOYED'")
    fun getTotalTrafficPercentage(): Double?
}

@Repository
interface PerformanceMetricRepository : JpaRepository<PerformanceMetric, Long> {
    
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): List<PerformanceMetric>
    
    fun findByMetricName(metricName: String): List<PerformanceMetric>
    
    @Query("SELECT m FROM PerformanceMetric m WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.timestamp >= :since ORDER BY m.timestamp DESC")
    fun findMetricsSince(@Param("entityType") entityType: String, @Param("entityId") entityId: String, @Param("since") since: LocalDateTime): List<PerformanceMetric>
    
    @Query("SELECT AVG(m.metricValue) FROM PerformanceMetric m WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.metricName = :metricName AND m.timestamp >= :since")
    fun getAverageMetricValue(@Param("entityType") entityType: String, @Param("entityId") entityId: String, @Param("metricName") metricName: String, @Param("since") since: LocalDateTime): Double?
    
    @Query("SELECT MAX(m.metricValue) FROM PerformanceMetric m WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.metricName = :metricName AND m.timestamp >= :since")
    fun getMaxMetricValue(@Param("entityType") entityType: String, @Param("entityId") entityId: String, @Param("metricName") metricName: String, @Param("since") since: LocalDateTime): Double?
    
    @Query("SELECT m FROM PerformanceMetric m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    fun findAllMetricsSince(@Param("since") since: LocalDateTime): List<PerformanceMetric>
    
    @Query("DELETE FROM PerformanceMetric m WHERE m.timestamp < :before")
    fun deleteMetricsBefore(@Param("before") before: LocalDateTime): Int
}

@Repository
interface ComplianceAuditRepository : JpaRepository<ComplianceAudit, Long> {
    
    fun findByAuditId(auditId: String): ComplianceAudit?
    
    fun findByFramework(framework: ComplianceFramework): List<ComplianceAudit>
    
    fun findByStatus(status: AuditStatus): List<ComplianceAudit>
    
    fun findByAuditor(auditor: User): List<ComplianceAudit>
    
    @Query("SELECT a FROM ComplianceAudit a WHERE a.framework = :framework ORDER BY a.auditDate DESC")
    fun findLatestAuditsByFramework(@Param("framework") framework: ComplianceFramework): List<ComplianceAudit>
    
    @Query("SELECT a FROM ComplianceAudit a WHERE a.status = 'COMPLETED' AND a.complianceScore < :threshold")
    fun findFailedAudits(@Param("threshold") threshold: Double): List<ComplianceAudit>
    
    @Query("SELECT AVG(a.complianceScore) FROM ComplianceAudit a WHERE a.framework = :framework AND a.status = 'COMPLETED' AND a.auditDate >= :since")
    fun getAverageComplianceScore(@Param("framework") framework: ComplianceFramework, @Param("since") since: LocalDateTime): Double?
    
    @Query("SELECT a FROM ComplianceAudit a WHERE a.nextAuditDate <= :date AND a.status = 'COMPLETED'")
    fun findAuditsDueBy(@Param("date") date: LocalDateTime): List<ComplianceAudit>
}

@Repository
interface FeatureFlagRepository : JpaRepository<FeatureFlag, Long> {
    
    fun findByFlagKey(flagKey: String): FeatureFlag?
    
    fun findByEnabled(enabled: Boolean): List<FeatureFlag>
    
    fun findByCreatedBy(user: User): List<FeatureFlag>
    
    @Query("SELECT f FROM FeatureFlag f WHERE :environment MEMBER OF f.environments")
    fun findByEnvironment(@Param("environment") environment: String): List<FeatureFlag>
    
    @Query("SELECT f FROM FeatureFlag f WHERE f.enabled = true AND (f.expiresAt IS NULL OR f.expiresAt > :now)")
    fun findActiveFlags(@Param("now") now: LocalDateTime): List<FeatureFlag>
    
    @Query("SELECT f FROM FeatureFlag f WHERE f.expiresAt <= :now AND f.enabled = true")
    fun findExpiredFlags(@Param("now") now: LocalDateTime): List<FeatureFlag>
    
    @Query("SELECT f FROM FeatureFlag f WHERE :userSegment MEMBER OF f.userSegments AND f.enabled = true")
    fun findByUserSegment(@Param("userSegment") userSegment: String): List<FeatureFlag>
}

@Repository
interface ApiRateLimitRepository : JpaRepository<ApiRateLimit, Long> {
    
    fun findByIdentifierAndIdentifierType(identifier: String, identifierType: IdentifierType): ApiRateLimit?
    
    fun findByUser(user: User): List<ApiRateLimit>
    
    fun findByEndpoint(endpoint: String): List<ApiRateLimit>
    
    @Query("SELECT r FROM ApiRateLimit r WHERE r.identifier = :identifier AND r.identifierType = :type AND r.endpoint = :endpoint")
    fun findByIdentifierAndEndpoint(@Param("identifier") identifier: String, @Param("type") identifierType: IdentifierType, @Param("endpoint") endpoint: String): ApiRateLimit?
    
    @Query("SELECT r FROM ApiRateLimit r WHERE r.currentMinuteCount >= r.requestsPerMinute OR r.currentHourCount >= r.requestsPerHour OR r.currentDayCount >= r.requestsPerDay")
    fun findExceededLimits(): List<ApiRateLimit>
    
    @Query("SELECT COUNT(r) FROM ApiRateLimit r WHERE r.currentDayCount >= r.requestsPerDay")
    fun countDailyLimitExceeded(): Long
}

@Repository
interface SystemConfigurationRepository : JpaRepository<SystemConfiguration, Long> {
    
    fun findByConfigKey(configKey: String): SystemConfiguration?
    
    fun findByEnvironment(environment: String): List<SystemConfiguration>
    
    fun findByConfigType(configType: String): List<SystemConfiguration>
    
    fun findByEncrypted(encrypted: Boolean): List<SystemConfiguration>
    
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configKey LIKE :pattern")
    fun findByConfigKeyPattern(@Param("pattern") pattern: String): List<SystemConfiguration>
    
    @Query("SELECT c FROM SystemConfiguration c WHERE c.environment = :environment AND c.configKey LIKE :keyPattern")
    fun findByEnvironmentAndKeyPattern(@Param("environment") environment: String, @Param("keyPattern") keyPattern: String): List<SystemConfiguration>
    
    @Query("SELECT c FROM SystemConfiguration c WHERE c.updatedBy = :user ORDER BY c.updatedAt DESC")
    fun findByUpdatedByOrderByUpdatedAtDesc(@Param("user") user: User): List<SystemConfiguration>
}

/**
 * Custom repository interfaces for complex queries
 */
interface CustomAIModelRepository {
    fun findOptimalModelForTask(
        language: String,
        taskType: String,
        minAccuracy: Double,
        maxLatency: Long
    ): AIModel?
    
    fun getModelPerformanceStatistics(modelId: String, since: LocalDateTime): Map<String, Double>
}

interface CustomGlobalRegionRepository {
    fun findBestRegionForRequest(
        clientLatitude: Double,
        clientLongitude: Double,
        requiredCapabilities: Set<String>,
        complianceRequirements: Set<String>
    ): GlobalRegion?
    
    fun getRegionLoadStatistics(since: LocalDateTime): Map<String, Double>
}

interface CustomSecurityIncidentRepository {
    fun getSecurityTrends(since: LocalDateTime): Map<String, Any>
    
    fun findSimilarIncidents(
        incidentType: IncidentType,
        affectedSystems: Set<String>,
        limit: Int
    ): List<SecurityIncident>
}

interface CustomPerformanceMetricRepository {
    fun getAggregatedMetrics(
        entityType: String,
        metricName: String,
        aggregationType: String, // AVG, MAX, MIN, SUM
        groupBy: String, // HOUR, DAY, WEEK
        since: LocalDateTime
    ): Map<String, Double>
    
    fun detectAnomalies(
        entityType: String,
        entityId: String,
        metricName: String,
        threshold: Double,
        since: LocalDateTime
    ): List<PerformanceMetric>
}