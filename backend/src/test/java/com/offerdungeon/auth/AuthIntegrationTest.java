package com.offerdungeon.auth;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetUsers() {
        jdbcTemplate.update("delete from sys_user_role");
        jdbcTemplate.update("delete from sys_user");
    }

    @Test
    void shouldRegisterNewUserSuccessfully() throws Exception {
        String requestId = "step6-register-request";

        mockMvc.perform(post("/api/auth/register")
                        .header("X-Request-Id", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "campus_user",
                                "email", "campus_user@example.com",
                                "password", "12345678",
                                "nickname", "校园用户"))))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("campus_user"))
                .andExpect(jsonPath("$.data.email").value("campus_user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("校园用户"))
                .andExpect(jsonPath("$.data.userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("ROLE_USER")));
    }

    @Test
    void shouldLoginWithUsername() throws Exception {
        registerDefaultUser();
        String requestId = "step6-login-username-request";

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Request-Id", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", "campus_user",
                                "password", "12345678"))))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("campus_user"))
                .andExpect(jsonPath("$.data.user.roleCodes", hasItem("ROLE_USER")));
    }

    @Test
    void shouldLoginWithEmail() throws Exception {
        registerDefaultUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", "campus_user@example.com",
                                "password", "12345678"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("campus_user@example.com"));
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        registerDefaultUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", "campus_user",
                                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid username/email or password."));
    }

    @Test
    void shouldReturnCurrentUserWhenTokenIsPresent() throws Exception {
        registerDefaultUser();
        String accessToken = loginAndExtractToken("campus_user");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("campus_user"))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("ROLE_USER")));
    }

    @Test
    void shouldRejectProtectedEndpointWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    @Test
    void shouldLogoutSuccessfullyWhenAuthenticated() throws Exception {
        registerDefaultUser();
        String accessToken = loginAndExtractToken("campus_user");

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    void shouldAllowAuthenticatedUserToAccessUserScopeEndpoint() throws Exception {
        registerDefaultUser();
        String accessToken = loginAndExtractToken("campus_user");

        mockMvc.perform(get("/api/user/access-scope")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope").value("USER"))
                .andExpect(jsonPath("$.data.username").value("campus_user"))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("ROLE_USER")));
    }

    @Test
    void shouldRejectAnonymousUserWhenAccessingUserScopeEndpoint() throws Exception {
        mockMvc.perform(get("/api/user/access-scope"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    @Test
    void shouldRejectNormalUserWhenAccessingAdminScopeEndpoint() throws Exception {
        registerDefaultUser();
        String accessToken = loginAndExtractToken("campus_user");

        mockMvc.perform(get("/api/admin/access-scope")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource."));
    }

    @Test
    void shouldAllowAdminUserToAccessAdminScopeEndpoint() throws Exception {
        insertAdminUser();
        String accessToken = loginAndExtractToken("admin");

        mockMvc.perform(get("/api/admin/access-scope")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope").value("ADMIN"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roleCodes", hasItem("ROLE_ADMIN")));
    }

    private void registerDefaultUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "campus_user",
                                "email", "campus_user@example.com",
                                "password", "12345678",
                                "nickname", "校园用户"))))
                .andExpect(status().isOk());
    }

    private void insertAdminUser() {
        jdbcTemplate.update(
                """
                insert into sys_user (
                    username,
                    email,
                    password_hash,
                    nickname,
                    user_status,
                    password_updated_at
                ) values (?, ?, ?, ?, 'ACTIVE', current_timestamp)
                """,
                "admin",
                "123@qq.com",
                passwordEncoder.encode("123456"),
                "系统管理员");

        jdbcTemplate.update(
                """
                insert into sys_user_role (user_id, role_id, assignment_status, created_by, updated_by, remark)
                values (
                    (select id from sys_user where username = ?),
                    (select id from sys_role where role_code = 'ROLE_ADMIN'),
                    'ACTIVE',
                    (select id from sys_user where username = ?),
                    (select id from sys_user where username = ?),
                    'Test admin assignment'
                )
                """,
                "admin",
                "admin",
                "admin");
    }

    private String loginAndExtractToken(String account) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", account,
                                "password", resolvePassword(account)))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return response.path("data").path("accessToken").asText();
    }

    private String resolvePassword(String account) {
        return "admin".equals(account) || "123@qq.com".equals(account) ? "123456" : "12345678";
    }
}
