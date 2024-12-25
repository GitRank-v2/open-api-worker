package com.dragonguard.open_api.gitrepomember

import com.dragonguard.open_api.gitrepomember.dto.GitRepoInfoRequest
import com.dragonguard.open_api.gitrepomember.dto.GitRepoMemberClientResponse
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class GitRepoMemberClient(
    private val restClient: RestClient
) {
    private val virtualThreadExecutorService: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    private val logger = LoggerFactory.getLogger(GitRepoMemberClient::class.java)

    companion object {
        private const val PATH_FORMAT = "repos/%s/stats/contributors"
    }

    @Retryable(
        value = [Exception::class],
        maxAttempts = 30,
        backoff = Backoff(delay = 300L, maxDelay = 2000L, multiplier = 1.3, random = true)
    )
    fun requestToGithub(request: GitRepoInfoRequest): List<GitRepoMemberClientResponse> {
        return CompletableFuture.supplyAsync({
            val uri = String.format(PATH_FORMAT, request.name)
            logger.info("Request to GitHub: $uri")

            val response = restClient
                .get()
                .uri(uri)
                .header("Authorization", "Bearer ${request.githubToken}")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(object : ParameterizedTypeReference<List<GitRepoMemberClientResponse>>() {})

            validateResponse(response) ?: throw OpenApiException.gitRepoMemberClient()
        }, virtualThreadExecutorService).get()
    }

    private fun validateResponse(response: List<GitRepoMemberClientResponse>?): List<GitRepoMemberClientResponse>? {
        if (isResponseEmpty(response)) {
            return null
        }
        return response
    }

    private fun isResponseEmpty(response: List<GitRepoMemberClientResponse>?): Boolean =
        response.isNullOrEmpty() || response.any {
            it.total == null || it.weeks.isNullOrEmpty() ||
                    it.author?.login == null || it.author.avatarUrl == null
        }
}
