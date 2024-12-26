package com.dragonguard.open_api.gitrepomember.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitRepoInfoRequest(
    @JsonProperty("git_repo_id")
    val gitRepoId: Long?,
    @JsonProperty("member_id")
    val memberId: Long?,
    @JsonProperty("github_token")
    val githubToken: String?,
    val name: String?,
)
