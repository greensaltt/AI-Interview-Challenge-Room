package com.offerdungeon.common.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class InfrastructureProbeService {

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider;

    public InfrastructureProbeService(
            ObjectProvider<DataSource> dataSourceProvider,
            ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider) {
        this.dataSourceProvider = dataSourceProvider;
        this.redisConnectionFactoryProvider = redisConnectionFactoryProvider;
    }

    public Map<String, Object> probe() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("checkedAt", Instant.now().toString());
        response.put("postgres", checkPostgres());
        response.put("pgvector", checkPgvector());
        response.put("redis", checkRedis());
        return response;
    }

    private Map<String, Object> checkPostgres() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return status("SKIPPED", "PostgreSQL datasource is not configured for this profile.");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("select version()");
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return status("UP", resultSet.getString(1));
        } catch (Exception exception) {
            return status("DOWN", compactMessage(exception));
        }
    }

    private Map<String, Object> checkPgvector() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return status("SKIPPED", "pgvector check skipped because datasource is unavailable.");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                "select extversion from pg_extension where extname = 'vector'");
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return status("DOWN", "Extension `vector` is not enabled in the current database.");
            }

            return status("UP", "vector " + resultSet.getString(1));
        } catch (Exception exception) {
            return status("DOWN", compactMessage(exception));
        }
    }

    private Map<String, Object> checkRedis() {
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactoryProvider.getIfAvailable();
        if (redisConnectionFactory == null) {
            return status("SKIPPED", "Redis connection factory is not configured for this profile.");
        }

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            return status("UP", connection.ping());
        } catch (Exception exception) {
            return status("DOWN", compactMessage(exception));
        }
    }

    private Map<String, Object> status(String status, String detail) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("status", status);
        item.put("detail", detail);
        return item;
    }

    private String compactMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.lines().findFirst().orElse(exception.getClass().getSimpleName());
    }
}
