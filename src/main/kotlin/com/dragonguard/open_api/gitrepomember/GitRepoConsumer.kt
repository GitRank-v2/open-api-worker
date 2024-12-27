package com.dragonguard.open_api.gitrepomember

import com.dragonguard.open_api.gitrepomember.dto.GitRepoClientRequest
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class GitRepoConsumer(
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val gitRepoService: GitRepoService,
    private val objectMapper: ObjectMapper,
) : MessageListener {

    private val logger = LoggerFactory.getLogger(GitRepoConsumer::class.java)

    @PostConstruct
    fun init() {
        val topic = ChannelTopic("openapi:git-repo-member")
        redisMessageListenerContainer.addMessageListener(this, topic)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            var payload = message.body.toString(StandardCharsets.UTF_8)

            val jsonStartIndex = payload.indexOf("{")
            if (jsonStartIndex != -1) {
                payload = payload.substring(jsonStartIndex)
            }
            val request = objectMapper.readValue(payload, GitRepoClientRequest::class.java)

            gitRepoService.getRepoInfo(request)
        } catch (e: Exception) {
            logger.error("GitHub OpenAPI 호출 오류 발생: {}", e.message)
        }
    }
}
