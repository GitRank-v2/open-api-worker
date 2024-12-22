package com.dragonguard.open_api.gitrepomember.dto

data class GitRepoMemberClientResponse(
    val total: Int?,
    val weeks: List<Week>?,
    val author: Author?
)
