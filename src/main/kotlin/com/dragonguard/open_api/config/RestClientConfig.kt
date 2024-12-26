package com.dragonguard.open_api.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.Executors

@Configuration
class RestClientConfig {
    companion object {
        private const val GITHUB_API_MIME_TYPE = "application/vnd.github+json"
        private const val REQUEST_TIMEOUT_DURATION = 20L
    }

    @Bean
    fun restClient(
        @Value("\${github.url}") url: String,
        @Value("\${github.version-key}") versionKey: String,
        @Value("\${github.version-value}") versionValue: String,
        @Value("\${github.user-agent}") userAgent: String,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, GITHUB_API_MIME_TYPE)
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
            .defaultHeader(versionKey, versionValue)
            .requestFactory(getClientHttpRequestFactory())
            .messageConverters { converters ->
                converters.add(MappingJackson2HttpMessageConverter(objectMapper()))
            }.build()

    private fun getClientHttpRequestFactory(): ClientHttpRequestFactory {
        val requestFactory =
            JdkClientHttpRequestFactory(
                HttpClient
                    .newBuilder()
                    .executor(Executors.newVirtualThreadPerTaskExecutor())
                    .build(),
            )
        requestFactory.setReadTimeout(Duration.ofSeconds(REQUEST_TIMEOUT_DURATION))
        return requestFactory
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
