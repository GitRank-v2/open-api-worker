package com.dragonguard.open_api.gitrepomember.dto

data class GitRepoInfoRequest(
    val gitRepoId: Long?,
    val memberId: Long?,
    val githubToken: String?,
    val name: String?,
)
