package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
@Transactional
class VcsService(
    private val repositoryRepository: RepositoryRepository,
    private val commitRepository: CommitRepository,
    private val userRepository: UserRepository,
    private val webClient: WebClient.Builder
) {

    @Value("\${app.github.token:}")
    private lateinit var githubToken: String

    private val githubClient by lazy {
        webClient
            .baseUrl("https://api.github.com")
            .defaultHeader("Authorization", "Bearer $githubToken")
            .defaultHeader("Accept", "application/vnd.github.v3+json")
            .build()
    }

    fun syncUserRepositories(userId: Long, githubUsername: String): Mono<List<Repository>> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return githubClient.get()
            .uri("/users/$githubUsername/repos?per_page=100")
            .retrieve()
            .bodyToFlux(GitHubRepoResponse::class.java)
            .collectList()
            .map { githubRepos ->
                githubRepos.map { githubRepo ->
                    val existingRepo = repositoryRepository.findByFullName(githubRepo.full_name)
                    
                    if (existingRepo != null) {
                        updateRepositoryFromGitHub(existingRepo, githubRepo)
                    } else {
                        createRepositoryFromGitHub(user, githubRepo)
                    }
                }
            }
            .doOnNext { repositories ->
                repositoryRepository.saveAll(repositories)
            }
    }

    fun getRepositoryCommits(repositoryId: Long, pageable: Pageable): Page<CommitResponse> {
        val repository = repositoryRepository.findById(repositoryId)
            .orElseThrow { ResourceNotFoundException("Repository not found") }

        val commits = commitRepository.findByRepositoryOrderByCreatedAtDesc(repository, pageable)
        return commits.map { mapToCommitResponse(it) }
    }

    fun syncRepositoryCommits(repositoryId: Long): Mono<List<Commit>> {
        val repository = repositoryRepository.findById(repositoryId)
            .orElseThrow { ResourceNotFoundException("Repository not found") }

        val owner = repository.fullName.split("/")[0]
        val repo = repository.fullName.split("/")[1]

        return githubClient.get()
            .uri("/repos/$owner/$repo/commits?per_page=100")
            .retrieve()
            .bodyToFlux(GitHubCommitResponse::class.java)
            .collectList()
            .map { githubCommits ->
                githubCommits.mapNotNull { githubCommit ->
                    val authorUser = findOrCreateUserFromGitHub(githubCommit.author)
                    
                    val existingCommit = commitRepository.findBySha(githubCommit.sha)
                    if (existingCommit == null) {
                        Commit(
                            sha = githubCommit.sha,
                            repository = repository,
                            author = authorUser,
                            message = githubCommit.commit.message,
                            treeSha = githubCommit.commit.tree.sha,
                            branch = repository.defaultBranch,
                            additions = githubCommit.stats?.additions ?: 0,
                            deletions = githubCommit.stats?.deletions ?: 0,
                            changedFiles = githubCommit.files?.size ?: 0
                        )
                    } else null
                }
            }
            .doOnNext { newCommits ->
                if (newCommits.isNotEmpty()) {
                    commitRepository.saveAll(newCommits)
                }
            }
    }

    fun createWebhook(repositoryId: Long, webhookUrl: String): Mono<WebhookResponse> {
        val repository = repositoryRepository.findById(repositoryId)
            .orElseThrow { ResourceNotFoundException("Repository not found") }

        val owner = repository.fullName.split("/")[0]
        val repo = repository.fullName.split("/")[1]

        val webhookRequest = GitHubWebhookRequest(
            name = "web",
            active = true,
            events = listOf("push", "pull_request", "issues"),
            config = GitHubWebhookConfig(
                url = webhookUrl,
                content_type = "json",
                insecure_ssl = "0"
            )
        )

        return githubClient.post()
            .uri("/repos/$owner/$repo/hooks")
            .bodyValue(webhookRequest)
            .retrieve()
            .bodyToMono(GitHubWebhookResponse::class.java)
            .map { response ->
                WebhookResponse(
                    id = response.id,
                    url = response.config.url,
                    events = response.events,
                    active = response.active
                )
            }
    }

    fun handleWebhookEvent(payload: GitHubWebhookPayload): Mono<Void> {
        return when (payload.action) {
            "opened", "synchronize" -> handlePullRequestEvent(payload)
            "opened" -> handleIssueEvent(payload)
            else -> handlePushEvent(payload)
        }
    }

    fun getUserRepositories(userId: Long, pageable: Pageable): Page<RepositoryResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val repositories = repositoryRepository.findByOwnerOrderByUpdatedAtDesc(user, pageable)
        return repositories.map { mapToRepositoryResponse(it) }
    }

    fun getRepositoryStats(repositoryId: Long): RepositoryStatsResponse {
        val repository = repositoryRepository.findById(repositoryId)
            .orElseThrow { ResourceNotFoundException("Repository not found") }

        val commitCount = commitRepository.countByRepository(repository)
        val recentCommits = commitRepository.findByRepositoryOrderByCreatedAtDesc(
            repository, 
            org.springframework.data.domain.PageRequest.of(0, 10)
        )

        return RepositoryStatsResponse(
            totalCommits = commitCount,
            starCount = repository.starCount,
            forkCount = repository.forkCount,
            watchCount = repository.watchCount,
            issueCount = repository.issueCount,
            pullRequestCount = repository.pullRequestCount,
            primaryLanguage = repository.primaryLanguage,
            sizeKb = repository.sizeKb,
            lastPushAt = repository.lastPushAt,
            recentCommits = recentCommits.content.map { mapToCommitResponse(it) }
        )
    }

    private fun createRepositoryFromGitHub(user: User, githubRepo: GitHubRepoResponse): Repository {
        return Repository(
            name = githubRepo.name,
            fullName = githubRepo.full_name,
            description = githubRepo.description,
            owner = user,
            isPrivate = githubRepo.private,
            defaultBranch = githubRepo.default_branch ?: "main",
            cloneUrl = githubRepo.clone_url,
            sshUrl = githubRepo.ssh_url,
            sizeKb = githubRepo.size,
            starCount = githubRepo.stargazers_count.toLong(),
            forkCount = githubRepo.forks_count.toLong(),
            watchCount = githubRepo.watchers_count.toLong(),
            primaryLanguage = githubRepo.language,
            license = githubRepo.license?.name,
            homepageUrl = githubRepo.homepage,
            hasIssues = githubRepo.has_issues,
            hasProjects = githubRepo.has_projects,
            hasWiki = githubRepo.has_wiki,
            archived = githubRepo.archived,
            disabled = githubRepo.disabled,
            lastPushAt = githubRepo.pushed_at?.let { LocalDateTime.parse(it.replace("Z", "")) }
        )
    }

    private fun updateRepositoryFromGitHub(repository: Repository, githubRepo: GitHubRepoResponse): Repository {
        repository.description = githubRepo.description
        repository.starCount = githubRepo.stargazers_count.toLong()
        repository.forkCount = githubRepo.forks_count.toLong()
        repository.watchCount = githubRepo.watchers_count.toLong()
        repository.sizeKb = githubRepo.size
        repository.primaryLanguage = githubRepo.language
        repository.lastPushAt = githubRepo.pushed_at?.let { LocalDateTime.parse(it.replace("Z", "")) }
        repository.updatedAt = LocalDateTime.now()
        return repository
    }

    private fun findOrCreateUserFromGitHub(githubUser: GitHubUserResponse?): User {
        if (githubUser == null) {
            // Return a default system user or handle this case appropriately
            return userRepository.findByUsername("system")
                ?: throw BusinessException("System user not found")
        }

        return userRepository.findByUsername(githubUser.login)
            ?: User(
                username = githubUser.login,
                email = "${githubUser.login}@github.local",
                displayName = githubUser.login,
                avatarUrl = githubUser.avatar_url
            ).also { userRepository.save(it) }
    }

    private fun handlePushEvent(payload: GitHubWebhookPayload): Mono<Void> {
        // Handle push events - sync commits, notify followers, etc.
        return Mono.empty()
    }

    private fun handlePullRequestEvent(payload: GitHubWebhookPayload): Mono<Void> {
        // Handle PR events - create notifications, update stats, etc.
        return Mono.empty()
    }

    private fun handleIssueEvent(payload: GitHubWebhookPayload): Mono<Void> {
        // Handle issue events - create notifications, update stats, etc.
        return Mono.empty()
    }

    private fun mapToRepositoryResponse(repository: Repository): RepositoryResponse {
        return RepositoryResponse(
            id = repository.id!!,
            name = repository.name,
            fullName = repository.fullName,
            description = repository.description,
            owner = UserSummaryResponse(
                id = repository.owner.id!!,
                username = repository.owner.username,
                displayName = repository.owner.displayName,
                avatarUrl = repository.owner.avatarUrl
            ),
            isPrivate = repository.isPrivate,
            defaultBranch = repository.defaultBranch,
            starCount = repository.starCount,
            forkCount = repository.forkCount,
            watchCount = repository.watchCount,
            primaryLanguage = repository.primaryLanguage,
            topics = repository.topics.toList(),
            createdAt = repository.createdAt,
            updatedAt = repository.updatedAt
        )
    }

    private fun mapToCommitResponse(commit: Commit): CommitResponse {
        return CommitResponse(
            id = commit.id!!,
            sha = commit.sha,
            message = commit.message,
            author = UserSummaryResponse(
                id = commit.author.id!!,
                username = commit.author.username,
                displayName = commit.author.displayName,
                avatarUrl = commit.author.avatarUrl
            ),
            branch = commit.branch,
            additions = commit.additions,
            deletions = commit.deletions,
            changedFiles = commit.changedFiles,
            verified = commit.verified,
            createdAt = commit.createdAt
        )
    }
}

// GitHub API Response DTOs
data class GitHubRepoResponse(
    val id: Long,
    val name: String,
    val full_name: String,
    val description: String?,
    val private: Boolean,
    val default_branch: String?,
    val clone_url: String,
    val ssh_url: String,
    val size: Long,
    val stargazers_count: Int,
    val forks_count: Int,
    val watchers_count: Int,
    val language: String?,
    val license: GitHubLicenseResponse?,
    val homepage: String?,
    val has_issues: Boolean,
    val has_projects: Boolean,
    val has_wiki: Boolean,
    val archived: Boolean,
    val disabled: Boolean,
    val pushed_at: String?
)

data class GitHubLicenseResponse(
    val name: String
)

data class GitHubCommitResponse(
    val sha: String,
    val commit: GitHubCommitDetailResponse,
    val author: GitHubUserResponse?,
    val stats: GitHubCommitStatsResponse?,
    val files: List<GitHubFileResponse>?
)

data class GitHubCommitDetailResponse(
    val message: String,
    val tree: GitHubTreeResponse
)

data class GitHubTreeResponse(
    val sha: String
)

data class GitHubUserResponse(
    val login: String,
    val avatar_url: String?
)

data class GitHubCommitStatsResponse(
    val additions: Int,
    val deletions: Int
)

data class GitHubFileResponse(
    val filename: String
)

data class GitHubWebhookRequest(
    val name: String,
    val active: Boolean,
    val events: List<String>,
    val config: GitHubWebhookConfig
)

data class GitHubWebhookConfig(
    val url: String,
    val content_type: String,
    val insecure_ssl: String
)

data class GitHubWebhookResponse(
    val id: Long,
    val config: GitHubWebhookConfig,
    val events: List<String>,
    val active: Boolean
)

data class GitHubWebhookPayload(
    val action: String?,
    val repository: GitHubRepoResponse?,
    val commits: List<GitHubCommitResponse>?
)

// Response DTOs
data class RepositoryResponse(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val owner: UserSummaryResponse,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val starCount: Long,
    val forkCount: Long,
    val watchCount: Long,
    val primaryLanguage: String?,
    val topics: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CommitResponse(
    val id: Long,
    val sha: String,
    val message: String,
    val author: UserSummaryResponse,
    val branch: String,
    val additions: Int,
    val deletions: Int,
    val changedFiles: Int,
    val verified: Boolean,
    val createdAt: LocalDateTime
)

data class RepositoryStatsResponse(
    val totalCommits: Long,
    val starCount: Long,
    val forkCount: Long,
    val watchCount: Long,
    val issueCount: Long,
    val pullRequestCount: Long,
    val primaryLanguage: String?,
    val sizeKb: Long,
    val lastPushAt: LocalDateTime?,
    val recentCommits: List<CommitResponse>
)

data class WebhookResponse(
    val id: Long,
    val url: String,
    val events: List<String>,
    val active: Boolean
)