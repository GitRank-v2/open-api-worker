package com.dragonguard.open_api.gitrepomember.dto

data class GitRepoRequest(
    val gitRepoId: Long?,
    val memberId: Long?,
    val githubToken: String?,
    val name: String?,
) {
    fun toGitRepoClientRequest(): GitRepoClientRequest {
        return GitRepoClientRequest(
            gitRepoId = gitRepoId,
            memberId = memberId,
            githubToken = githubToken,
            name = name,
        )
    }
}
