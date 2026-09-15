package com.sonrisa.homework;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// End-to-end proof, through real HTTP requests (not mocks), that the security posture actually
// holds together: public registration, authentication required everywhere else, role-based
// gating for admin-only endpoints, ownership-based gating for Alert/Sender, and a disabled
// account rejected at login rather than just at matching time. The service-layer unit tests
// cover the individual rules in isolation; this covers the wiring between them.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthorizationIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin12345";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndGetId(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }

    @Test
    void registrationIsPublic() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"newperson@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void everythingElseRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/alerts/user/00000000-0000-0000-0000-000000000000")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnlyEndpointsRejectARegularUserAndAcceptAnAdmin() throws Exception {
        registerAndGetId("regular@example.com", "password123");

        mockMvc.perform(get("/api/users").with(httpBasic("regular@example.com", "password123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/data-sources").with(httpBasic("regular@example.com", "password123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users").with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void aUserCanOnlyReadTheirOwnAlertsAndSendersNotAnotherUsersById() throws Exception {
        String aliceId = registerAndGetId("alice-auth-test@example.com", "password123");
        registerAndGetId("bob-auth-test@example.com", "password123");

        mockMvc.perform(get("/api/senders/user/" + aliceId).with(httpBasic("alice-auth-test@example.com", "password123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/senders/user/" + aliceId).with(httpBasic("bob-auth-test@example.com", "password123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/alerts/user/" + aliceId).with(httpBasic("bob-auth-test@example.com", "password123")))
                .andExpect(status().isForbidden());

        // an admin isn't restricted by the ownership check
        mockMvc.perform(get("/api/senders/user/" + aliceId).with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void aUserCannotCreateASenderClaimingSomeoneElsesUserId() throws Exception {
        String aliceId = registerAndGetId("alice-claim-test@example.com", "password123");
        registerAndGetId("bob-claim-test@example.com", "password123");

        mockMvc.perform(post("/api/senders")
                        .with(httpBasic("bob-claim-test@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"EMAIL\",\"userId\":\"%s\",\"config\":\"evil@example.com\"}".formatted(aliceId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        registerAndGetId("wrongpass-test@example.com", "password123");

        mockMvc.perform(get("/api/users/me").with(httpBasic("wrongpass-test@example.com", "not-the-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aDisabledUserIsRejectedAtAuthenticationNotJustAtMatchingTime() throws Exception {
        String userId = registerAndGetId("disable-test@example.com", "password123");

        mockMvc.perform(get("/api/users/me").with(httpBasic("disable-test@example.com", "password123")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/users/" + userId + "/deactivate").with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me").with(httpBasic("disable-test@example.com", "password123")))
                .andExpect(status().isUnauthorized());
    }
}
