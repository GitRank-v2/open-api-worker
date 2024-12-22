package com.dragonguard.open_api.gitrepomember

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import javax.sql.DataSource

@Repository
class GitRepoMemberRepository(
    private val dataSource: DataSource
) {
    private val jdbcTemplate = JdbcTemplate(dataSource)

    fun saveAll(gitRepoMembers: List<GitRepoMember>): List<Long> {
        gitRepoMembers.forEach { ensureMemberExists(it.member) }

        return gitRepoMembers.map { gitRepoMember ->
            val existsSql = "SELECT id FROM git_repo_member WHERE git_repo_id = ? AND member_id = ?"

            val existingId = runCatching {
                jdbcTemplate.queryForObject(
                    existsSql,
                    { rs, _ -> rs.getLong("id") },
                    arrayOf(gitRepoMember.gitRepoId, gitRepoMember.member.githubId)
                )
            }.getOrNull()

            existingId ?: run {
                val insertSql = """
                    INSERT INTO git_repo_member 
                    (git_repo_id, member_id, commits, additions, deletions) 
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

                val keyHolder = GeneratedKeyHolder()

                jdbcTemplate.update({ connection ->
                    connection.prepareStatement(insertSql, arrayOf("id")).apply {
                        setLong(1, gitRepoMember.gitRepoId)
                        setString(2, gitRepoMember.member.githubId)
                        setInt(3, gitRepoMember.commits)
                        setInt(4, gitRepoMember.additions)
                        setInt(5, gitRepoMember.deletions)
                    }
                }, keyHolder)

                keyHolder.key?.toLong() ?: throw IllegalStateException("GitRepoMember 생성 실패")
            }
        }
    }

    private fun ensureMemberExists(member: Member) {
        val existsSql = "SELECT COUNT(id) FROM member WHERE github_id = ?"
        val count = jdbcTemplate.queryForObject(existsSql, Int::class.java, arrayOf(member.githubId)) ?: 0

        if (count == 0) {
            jdbcTemplate.update(
                "INSERT INTO member (github_id, profile_image, auth_step, tier) VALUES (?, ?, ?, ?)",
                member.githubId,
                member.profileImage,
                member.authStep,
                member.tier
            )
        }
    }
}
