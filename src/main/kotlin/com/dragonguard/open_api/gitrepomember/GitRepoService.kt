package com.dragonguard.open_api.gitrepomember

import com.dragonguard.open_api.gitrepomember.dto.GitRepoInfoRequest
import com.dragonguard.open_api.gitrepomember.dto.GitRepoResponse
import org.springframework.stereotype.Service

@Service
class GitRepoService(
    private val gitRepoMemberClient: GitRepoMemberClient,
    private val gitRepoMemberRepository: GitRepoMemberRepository,
) {
    fun getRepoInfo(request: GitRepoInfoRequest): GitRepoResponse {
        val response = gitRepoMemberClient.requestToGithub(request)

        val gitRepoMembers = response.map {
            GitRepoMember(
                gitRepoId = request.gitRepoId!!,
                commits = it.total!!,
                additions = it.weeks?.sumOf { week -> week.a!! } ?: 0,
                deletions = it.weeks?.sumOf { week -> week.d!! } ?: 0,
                member = Member(
                    githubId = it.author?.login!!,
                    profileImage = it.author.avatarUrl!!,
                )
            )
        }

        return GitRepoResponse(
            saveAll(gitRepoMembers)
        )
    }

    private fun saveAll(gitRepoMembers: List<GitRepoMember>): List<Long> =
        gitRepoMemberRepository.saveAll(gitRepoMembers)
}
