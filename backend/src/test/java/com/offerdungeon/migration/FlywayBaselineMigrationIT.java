package com.offerdungeon.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void shouldApplyBaselineMigrationAndSeedAdminAccount() throws SQLException {
        String databaseName = "ai_interview_migration_it_" + System.currentTimeMillis();
        createDatabase(databaseName);

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(jdbcUrl(databaseName), USERNAME, PASSWORD)
                    .load();

            assertEquals(1, flyway.migrate().migrationsExecuted);
            assertEquals(0, flyway.migrate().migrationsExecuted);

            try (Connection connection =
                    DriverManager.getConnection(jdbcUrl(databaseName), USERNAME, PASSWORD)) {
                assertEquals(1, count(connection,
                        "select count(*) from flyway_schema_history where version = '1' and success"));
                assertEquals(1, count(connection,
                        "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'sys_user'"));
                assertEquals(1, count(connection,
                        "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'sys_role'"));
                assertEquals(1, count(connection,
                        "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'sys_user_role'"));
                assertEquals(2, count(connection,
                        "select count(*) from sys_role where role_code in ('ROLE_ADMIN', 'ROLE_USER')"));
                assertEquals(1, count(connection,
                        "select count(*) from sys_user_role ur "
                                + "join sys_user u on u.id = ur.user_id "
                                + "join sys_role r on r.id = ur.role_id "
                                + "where u.username = 'admin' and r.role_code = 'ROLE_ADMIN'"));
                assertTrue(hasMatchingSeedPassword(connection),
                        "Expected default admin password to match plain text 123456.");
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
