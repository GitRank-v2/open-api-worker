package com.dragonguard.open_api.gitrepomember.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Author(
    val login: String?,
    @JsonProperty("avatar_url")
    val avatarUrl: String?
)
