package com.dragonguard.open_api.gitrepomember

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

@Repository
class GitRepoMemberRepository(
    private val dataSource: DataSource
) {
    private val jdbcTemplate = JdbcTemplate(dataSource)

    @Transactional
    fun saveAll(gitRepoMembers: List<GitRepoMember>): List<Long> {
        val updatedGitRepoMembers = gitRepoMembers.map {
            val id = ensureMemberExists(it.member)
            it.member.id = id
            it
        }

        return updatedGitRepoMembers.map { gitRepoMember ->
            val upsertSql = """
            INSERT INTO git_repo_member 
            (git_repo_id, member_id, commits, additions, deletions, created_at, deleted) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
            commits = VALUES(commits),
            additions = VALUES(additions),
            deletions = VALUES(deletions)
            """.trimIndent()

            val keyHolder: KeyHolder = GeneratedKeyHolder()

            jdbcTemplate.update({ connection ->
                val ps: PreparedStatement =
                    connection.prepareStatement(upsertSql, PreparedStatement.RETURN_GENERATED_KEYS)
                ps.setLong(1, gitRepoMember.gitRepoId)
                ps.setLong(2, gitRepoMember.member.id!!)
                ps.setInt(3, gitRepoMember.commits)
                ps.setInt(4, gitRepoMember.additions)
                ps.setInt(5, gitRepoMember.deletions)
                ps.setTimestamp(6, Timestamp.from(Instant.now()))
                ps.setInt(7, 0)
                ps
            }, keyHolder)

            keyHolder.key?.toLong() ?: throw IllegalStateException("GitRepoMember 생성 실패")
        }
    }

    private fun ensureMemberExists(member: Member): Long {
        val existsSql = "SELECT id FROM member WHERE github_id = ?"

        val memberId: Long? = jdbcTemplate.query(
            existsSql,
            { rs, _ -> rs.getLong("id") },
            member.githubId
        ).firstOrNull()

        return if (memberId != null) {
            memberId
        } else {
            jdbcTemplate.update(
                "INSERT INTO member (github_id, profile_image, auth_step, tier, created_at, deleted) VALUES (?, ?, ?, ?, ?, ?)",
                member.githubId,
                member.profileImage,
                member.authStep,
                member.tier,
                Timestamp.from(Instant.now()),
                0
            )

            jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long::class.java)
                ?: throw IllegalStateException("Member id 조회 실패")
        }
    }
}
