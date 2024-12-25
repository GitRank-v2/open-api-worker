package com.dragonguard.open_api.gitrepomember

data class Member(
    val githubId: String,
    val profileImage: String,
) {
    var id: Long? = null
    val authStep: String = "NONE"
    val tier: String = "SPROUT"
}
