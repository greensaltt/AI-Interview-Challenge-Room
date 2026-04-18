package com.offerdungeon.auth.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class AuthUserRepository {

    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert userInsert;

    public AuthUserRepository(JdbcClient jdbcClient, DataSource dataSource) {
        this.jdbcClient = jdbcClient;
        this.userInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("sys_user")
                .usingColumns(
                        "username",
                        "email",
                        "password_hash",
                        "nickname",
                        "user_status",
                        "password_updated_at")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<AuthUserRecord> findByAccount(String account) {
        return jdbcClient
                .sql(
                        """
                        select id, username, email, password_hash, nickname, user_status
                        from sys_user
                        where username = :account or lower(email) = lower(:account)
                        fetch first 1 row only
                        """)
                .param("account", account)
                .query(this::mapUser)
                .optional();
    }

    public Optional<AuthUserRecord> findById(Long userId) {
        return jdbcClient
                .sql(
                        """
                        select id, username, email, password_hash, nickname, user_status
                        from sys_user
                        where id = :userId
                        fetch first 1 row only
                        """)
                .param("userId", userId)
                .query(this::mapUser)
                .optional();
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcClient
                .sql("select count(*) from sys_user where username = :username")
                .param("username", username)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcClient
                .sql("select count(*) from sys_user where lower(email) = lower(:email)")
                .param("email", email)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public long createUser(String username, String email, String passwordHash, String nickname) {
        Number generatedId = userInsert.executeAndReturnKey(Map.of(
                "username", username,
                "email", email,
                "password_hash", passwordHash,
                "nickname", nickname,
                "user_status", "ACTIVE",
                "password_updated_at", Timestamp.from(Instant.now())));

        if (generatedId == null) {
            throw new IllegalStateException("Failed to create user.");
        }
        return generatedId.longValue();
    }

    public void assignRole(long userId, String roleCode, String remark) {
        int updated = jdbcClient
                .sql(
                        """
                        insert into sys_user_role (
                            user_id,
                            role_id,
                            assignment_status,
                            created_by,
                            updated_by,
                            remark
                        ) values (
                            :userId,
                            (select id from sys_role where role_code = :roleCode),
                            'ACTIVE',
                            :userId,
                            :userId,
                            :remark
                        )
                        """)
                .param("userId", userId)
                .param("roleCode", roleCode)
                .param("remark", remark)
                .update();

        if (updated != 1) {
            throw new IllegalStateException("Failed to assign default role to newly registered user.");
        }
    }

    public List<String> findActiveRoleCodesByUserId(Long userId) {
        return jdbcClient
                .sql(
                        """
                        select r.role_code
                        from sys_user_role ur
                        join sys_role r on r.id = ur.role_id
                        where ur.user_id = :userId
                          and ur.assignment_status = 'ACTIVE'
                          and r.role_status = 'ACTIVE'
                          and (ur.expires_at is null or ur.expires_at > current_timestamp)
                        order by r.sort_order asc, r.role_code asc
                        """)
                .param("userId", userId)
                .query(String.class)
                .list();
    }

    public void updateLastLogin(Long userId, Instant loginAt, String loginIp) {
        jdbcClient
                .sql(
                        """
                        update sys_user
                        set last_login_at = :loginAt,
                            last_login_ip = :loginIp,
                            updated_at = current_timestamp
                        where id = :userId
                        """)
                .param("loginAt", Timestamp.from(loginAt))
                .param("loginIp", loginIp)
                .param("userId", userId)
                .update();
    }

    private AuthUserRecord mapUser(ResultSet resultSet, int rowNum) throws SQLException {
        return new AuthUserRecord(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("nickname"),
                resultSet.getString("user_status"));
    }
}
