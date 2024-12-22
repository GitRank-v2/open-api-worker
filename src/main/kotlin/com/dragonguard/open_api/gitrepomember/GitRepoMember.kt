package com.dragonguard.open_api.gitrepomember

data class GitRepoMember(
    val gitRepoId: Long,
    val commits: Int,
    val additions: Int,
    val deletions: Int,
    val member: Member,
)
