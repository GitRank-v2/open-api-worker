package com.dragonguard.open_api.gitrepomember

import com.dragonguard.open_api.gitrepomember.dto.GitRepoClientRequest
import com.dragonguard.open_api.gitrepomember.dto.GitRepoMemberClientResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
    private val restClient: RestClient,
) {
    private val virtualThreadExecutorService: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    private val objectMapper = objectMapper()
    private val logger = LoggerFactory.getLogger(GitRepoMemberClient::class.java)

    companion object {
        private const val PATH_FORMAT = "repos/%s/stats/contributors"
    }

    @Retryable(
        value = [OpenApiException::class],
        maxAttempts = 30,
        backoff = Backoff(delay = 300L, maxDelay = 2000L, multiplier = 1.3, random = true)
    )
    fun requestToGithub(request: GitRepoClientRequest): List<GitRepoMemberClientResponse> {
        return CompletableFuture.supplyAsync({
            val uri = String.format(PATH_FORMAT, request.name)
            logger.info("Request to GitHub: {}", uri)

            val responseEntity = restClient
                .get()
                .uri(uri)
                .header("Authorization", "Bearer ${request.githubToken}")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String::class.java)

            val statusCode = responseEntity.statusCode

            when (statusCode) {
                HttpStatus.OK -> {
                    val responseBody = responseEntity.body ?: throw OpenApiException.gitRepoMemberClient()
                    try {
                        objectMapper.readValue(
                            responseBody,
                            object : TypeReference<List<GitRepoMemberClientResponse>>() {})
                    } catch (e: Exception) {
                        logger.info("GitHub OpenAPI Response Error: {}", e.message)
                        emptyList()
                    }
                }

                HttpStatus.ACCEPTED -> {
                    throw OpenApiException.gitRepoMemberClient()
                }

                else -> {
                    throw OpenApiException.gitRepoMemberClient()
                }
            }
        }, virtualThreadExecutorService).get()
    }

    private fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(ParameterNamesModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }
}
