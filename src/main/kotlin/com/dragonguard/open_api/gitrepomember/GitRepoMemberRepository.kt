package com.dragonguard.open_api.gitrepomember

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

@Repository
class GitRepoMemberRepository(
    private val dataSource: DataSource
) {
    private val jdbcTemplate = JdbcTemplate(dataSource)

    fun saveAll(gitRepoMembers: List<GitRepoMember>): List<Long> {
        val updatedGitRepoMembers = gitRepoMembers.map {
            val id = ensureMemberExists(it.member)
            it.member.id = id
            it
        }

        return updatedGitRepoMembers.map { gitRepoMember ->
            val upsertSql = """
            INSERT INTO git_repo_member 
            (git_repo_id, member_id, commits, additions, deletions, created_at) 
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (git_repo_id, member_id) 
            DO UPDATE SET 
                commits = EXCLUDED.commits,
                additions = EXCLUDED.additions,
                deletions = EXCLUDED.deletions,
        """.trimIndent()

            val keyHolder = GeneratedKeyHolder()

            jdbcTemplate.update({ connection ->
                connection.prepareStatement(upsertSql, arrayOf("id")).apply {
                    setLong(1, gitRepoMember.gitRepoId)
                    setLong(2, gitRepoMember.member.id!!)
                    setInt(3, gitRepoMember.commits)
                    setInt(4, gitRepoMember.additions)
                    setInt(5, gitRepoMember.deletions)
                    setTimestamp(6, Timestamp.from(Instant.now()))
                }
            }, keyHolder)

            keyHolder.key?.toLong() ?: throw IllegalStateException("GitRepoMember 생성 실패")
        }
    }

    private fun ensureMemberExists(member: Member): Long {
        val existsSql = "SELECT id FROM member WHERE github_id = ?"
        val memberId: Long? = jdbcTemplate.queryForObject(existsSql, Long::class.java, member.githubId)

        return if (memberId != null) {
            memberId
        } else {
            jdbcTemplate.update(
                "INSERT INTO member (github_id, profile_image, auth_step, tier, created_at) VALUES (?, ?, ?, ?, ?)",
                member.githubId,
                member.profileImage,
                member.authStep,
                member.tier,
                Timestamp.from(Instant.now())
            )

            jdbcTemplate.queryForObject(existsSql, Long::class.java, member.githubId)
                ?: throw IllegalStateException("Member id 조회 실패")
        }
    }
}
