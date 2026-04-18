package com.offerdungeon.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "ai.interview.runMigrationTests", matches = "true")
class FlywayBaselineMigrationIT {

    private static final String HOST = envOrDefault("AI_INTERVIEW_DB_HOST", "localhost");
    private static final String PORT = envOrDefault("AI_INTERVIEW_DB_PORT", "5432");
    private static final String USERNAME = envOrDefault("AI_INTERVIEW_DB_USER", "postgres");
    private static final String PASSWORD = envOrDefault("AI_INTERVIEW_DB_PASSWORD", "postgres");
    private static final String ADMIN_DATABASE = "postgres";

    @Test
    void shouldApplyAuthPermissionMigrationsAndSupportMultiRoleAssignments() throws SQLException {
        String databaseName = "ai_interview_migration_it_" + System.currentTimeMillis();
        createDatabase(databaseName);

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(jdbcUrl(databaseName), USERNAME, PASSWORD)
                    .load();

            assertEquals(2, flyway.migrate().migrationsExecuted);
            assertEquals(0, flyway.migrate().migrationsExecuted);

            try (Connection connection =
                    DriverManager.getConnection(jdbcUrl(databaseName), USERNAME, PASSWORD)) {
                assertEquals(1, count(connection,
                        "select count(*) from flyway_schema_history where version = '1' and success"));
                assertEquals(1, count(connection,
                        "select count(*) from flyway_schema_history where version = '2' and success"));
                assertEquals(1, count(connection,
                        "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'sys_user'"));
                assertEquals(1, count(connection,
                        "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'sys_role'"));
                assertEquals(1, count(connection,
                        "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'sys_user_role'"));
                assertTrue(columnExists(connection, "sys_user", "password_updated_at"));
                assertTrue(columnExists(connection, "sys_user", "last_login_at"));
                assertTrue(columnExists(connection, "sys_user", "last_login_ip"));
                assertTrue(columnExists(connection, "sys_user", "account_locked_at"));
                assertTrue(columnExists(connection, "sys_user", "disabled_reason"));
                assertTrue(columnExists(connection, "sys_role", "role_type"));
                assertTrue(columnExists(connection, "sys_role", "is_builtin"));
                assertTrue(columnExists(connection, "sys_role", "sort_order"));
                assertTrue(columnExists(connection, "sys_role", "disabled_reason"));
                assertTrue(columnExists(connection, "sys_user_role", "assignment_status"));
                assertTrue(columnExists(connection, "sys_user_role", "expires_at"));
                assertTrue(columnExists(connection, "sys_user_role", "updated_at"));
                assertTrue(columnExists(connection, "sys_user_role", "updated_by"));
                assertTrue(columnExists(connection, "sys_user_role", "remark"));
                assertEquals(3, count(connection,
                        "select count(*) from sys_role where role_code in ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MENTOR')"));
                assertEquals(1, count(connection,
                        "select count(*) from sys_user_role ur "
                                + "join sys_user u on u.id = ur.user_id "
                                + "join sys_role r on r.id = ur.role_id "
                                + "where u.username = 'admin' and r.role_code = 'ROLE_ADMIN'"));
                assertEquals(1, count(connection,
                        "select count(*) from sys_role "
                                + "where role_code = 'ROLE_MENTOR' and role_status = 'DISABLED' "
                                + "and role_type = 'RESERVED' and is_builtin = true"));
                assertEquals(1, count(connection,
                        "select count(*) from sys_user "
                                + "where username = 'admin' and user_status = 'ACTIVE' "
                                + "and password_updated_at is not null"));
                assertTrue(hasMatchingSeedPassword(connection),
                        "Expected default admin password to match plain text 123456.");

                long campusUserId = insertUser(connection,
                        "campus_user",
                        "campus_user@example.com",
                        "校园用户");
                long opsAdminId = insertUser(connection,
                        "ops_admin",
                        "ops_admin@example.com",
                        "运营管理员");

                assignRole(connection, campusUserId, "ROLE_USER", "ACTIVE", "普通用户默认角色");
                assignRole(connection, campusUserId, "ROLE_MENTOR", "REVOKED", "预留导师角色扩展验证");
                assignRole(connection, opsAdminId, "ROLE_ADMIN", "ACTIVE", "管理员角色验证");

                assertEquals(2, count(connection,
                        "select count(*) from sys_user_role ur "
                                + "where ur.user_id = " + campusUserId));
                assertEquals(1, count(connection,
                        "select count(*) from sys_user_role ur "
                                + "join sys_role r on r.id = ur.role_id "
                                + "where ur.user_id = " + campusUserId + " and r.role_code = 'ROLE_USER'"));
                assertEquals(1, count(connection,
                        "select count(*) from sys_user_role ur "
                                + "join sys_role r on r.id = ur.role_id "
                                + "where ur.user_id = " + campusUserId + " and r.role_code = 'ROLE_MENTOR' "
                                + "and ur.assignment_status = 'REVOKED'"));
                assertEquals(1, count(connection,
                        "select count(*) from sys_user_role ur "
                                + "join sys_role r on r.id = ur.role_id "
                                + "where ur.user_id = " + opsAdminId + " and r.role_code = 'ROLE_ADMIN'"));

                assertInvalidStatusRejected(connection);
            }
        } finally {
            dropDatabase(databaseName);
        }
    }

    private static boolean hasMatchingSeedPassword(Connection connection) throws SQLException {
        String sql = "select crypt('123456', password_hash) = password_hash "
                + "from sys_user where username = 'admin' and email = '123@qq.com'";

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next(), "Expected seeded admin account to exist.");
            return resultSet.getBoolean(1);
        }
    }

    private static int count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName)
            throws SQLException {
        String sql = "select count(*) from information_schema.columns "
                + "where table_schema = 'public' and table_name = ? and column_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) == 1;
            }
        }
    }

    private static long insertUser(Connection connection, String username, String email, String nickname)
            throws SQLException {
        String sql = "insert into sys_user "
                + "(username, email, password_hash, nickname, user_status, created_by, updated_by) "
                + "values (?, ?, crypt('123456', gen_salt('bf', 10)), ?, 'ACTIVE', 1, 1) "
                + "returning id";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, nickname);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private static void assignRole(
            Connection connection,
            long userId,
            String roleCode,
            String assignmentStatus,
            String remark) throws SQLException {
        String sql = "insert into sys_user_role "
                + "(user_id, role_id, assignment_status, created_by, updated_by, remark) "
                + "values (?, (select id from sys_role where role_code = ?), ?, 1, 1, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, roleCode);
            statement.setString(3, assignmentStatus);
            statement.setString(4, remark);
            statement.executeUpdate();
        }
    }

    private static void assertInvalidStatusRejected(Connection connection) throws SQLException {
        String sql = "insert into sys_user (username, email, password_hash, nickname, user_status) "
                + "values ('invalid_status_user', 'invalid_status_user@example.com', "
                + "crypt('123456', gen_salt('bf', 10)), '非法状态用户', 'UNKNOWN')";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            fail("Expected sys_user.user_status check constraint to reject UNKNOWN.");
        } catch (SQLException ex) {
            assertTrue(ex.getMessage().contains("ck_sys_user_status"));
        }
    }

    private static void createDatabase(String databaseName) throws SQLException {
        try (Connection connection =
                        DriverManager.getConnection(jdbcUrl(ADMIN_DATABASE), USERNAME, PASSWORD);
                Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.execute("create database \"" + databaseName + "\"");
        }
    }

    private static void dropDatabase(String databaseName) throws SQLException {
        try (Connection connection =
                        DriverManager.getConnection(jdbcUrl(ADMIN_DATABASE), USERNAME, PASSWORD);
                Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.execute(
                    "select pg_terminate_backend(pid) from pg_stat_activity "
                            + "where datname = '"
                            + databaseName
                            + "' and pid <> pg_backend_pid()");
            statement.execute("drop database if exists \"" + databaseName + "\"");
        }
    }

    private static String jdbcUrl(String databaseName) {
        return "jdbc:postgresql://" + HOST + ":" + PORT + "/" + databaseName;
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
