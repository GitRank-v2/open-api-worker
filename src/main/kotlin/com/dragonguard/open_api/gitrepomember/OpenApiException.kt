package com.dragonguard.open_api.gitrepomember

class OpenApiException(
    override val message: String
) : IllegalStateException(message) {
    companion object {
        fun gitRepoMemberClient() = OpenApiException("GitRepoMemberClient")
    }
}
