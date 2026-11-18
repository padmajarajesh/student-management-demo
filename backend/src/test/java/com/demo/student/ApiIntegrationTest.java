package com.demo.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API tests: HTTP request -> security -> controller -> service ->
 * JPA -> real PostgreSQL (schema created by the Flyway migrations).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(EmbeddedPostgresConfig.class)
class ApiIntegrationTest {

    private static final String XRW = "X-Requested-With";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    // ---------------------------------------------------------------- helpers

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    private Map<String, Object> registration(String email) {
        return Map.of(
                "fullName", "Test User",
                "email", email,
                "password", "Password123",
                "confirmPassword", "Password123",
                "phoneNumber", "+1 555 123 4567");
    }

    private Map<String, Object> student(String name, String email, String department) {
        return Map.of(
                "fullName", name,
                "email", email,
                "phoneNumber", "9876543210",
                "department", department,
                "gender", "Female",
                "dateOfBirth", "2003-05-14",
                "address", "12 Main Street");
    }

    private MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder req, Object body) throws Exception {
        return req.header(XRW, "XMLHttpRequest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body));
    }

    private Cookie[] cookiesFrom(MockHttpServletResponse response) {
        return Arrays.stream(response.getCookies())
                .filter(c -> c.getMaxAge() != 0)
                .toArray(Cookie[]::new);
    }

    private Cookie cookie(Cookie[] cookies, String name) {
        return Arrays.stream(cookies).filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }

    /** Registers a new user and returns their auth cookies. */
    private Cookie[] registerUser(String email) throws Exception {
        MockHttpServletResponse res = mvc.perform(withJson(post("/api/auth/register"), registration(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return cookiesFrom(res);
    }

    private String createStudent(Cookie[] auth, Map<String, Object> body) throws Exception {
        String content = mvc.perform(withJson(post("/api/students"), body).cookie(auth))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(content).at("/data/id").asText();
    }

    private JsonNode body(ResultActions actions) throws Exception {
        return json.readTree(actions.andReturn().getResponse().getContentAsString());
    }

    // ------------------------------------------------------------------ tests

    @Test
    void healthEndpointIsPublicAndReportsDatabase() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("UP"));
    }

    @Test
    void registrationValidatesInputAndNeverReturnsPassword() throws Exception {
        String email = uniqueEmail("reg");
        MockHttpServletResponse res = mvc.perform(withJson(post("/api/auth/register"), registration(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn().getResponse();
        assertThat(res.getContentAsString()).doesNotContain("Password123").doesNotContain("$2a$");

        List<String> setCookies = res.getHeaders("Set-Cookie");
        assertThat(setCookies).anyMatch(c -> c.startsWith("access_token=") && c.contains("HttpOnly"));
        assertThat(setCookies).anyMatch(c -> c.startsWith("refresh_token=") && c.contains("HttpOnly")
                && c.contains("Path=/api/auth"));

        // duplicate email (case-insensitive)
        mvc.perform(withJson(post("/api/auth/register"), registration(email.toUpperCase())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").exists());

        // invalid fields
        mvc.perform(withJson(post("/api/auth/register"), Map.of(
                        "fullName", "", "email", "not-an-email", "password", "short",
                        "confirmPassword", "short", "phoneNumber", "abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());

        // password confirmation mismatch
        Map<String, Object> mismatch = new java.util.HashMap<>(registration(uniqueEmail("mm")));
        mismatch.put("confirmPassword", "Different123");
        mvc.perform(withJson(post("/api/auth/register"), mismatch))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmPassword").value("Passwords do not match"));
    }

    @Test
    void loginMeRefreshAndLogoutFlow() throws Exception {
        String email = uniqueEmail("login");
        registerUser(email);

        mvc.perform(withJson(post("/api/auth/login"), Map.of("email", email, "password", "WrongPass1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        MockHttpServletResponse loginRes = mvc.perform(withJson(post("/api/auth/login"),
                        Map.of("email", email, "password", "Password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andReturn().getResponse();
        Cookie[] auth = cookiesFrom(loginRes);

        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").cookie(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        // refresh rotates the refresh token; the old one stops working
        Cookie oldRefresh = cookie(auth, "refresh_token");
        MockHttpServletResponse refreshRes = mvc.perform(post("/api/auth/refresh").header(XRW, "x").cookie(oldRefresh))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Cookie[] refreshed = cookiesFrom(refreshRes);
        assertThat(cookie(refreshed, "refresh_token").getValue()).isNotEqualTo(oldRefresh.getValue());
        mvc.perform(post("/api/auth/refresh").header(XRW, "x").cookie(oldRefresh))
                .andExpect(status().isUnauthorized());

        // logout revokes the refresh token and clears the cookies
        MockHttpServletResponse logoutRes = mvc.perform(post("/api/auth/logout").header(XRW, "x").cookie(refreshed))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(logoutRes.getHeaders("Set-Cookie")).allMatch(c -> c.contains("Max-Age=0"));
        mvc.perform(post("/api/auth/refresh").header(XRW, "x").cookie(cookie(refreshed, "refresh_token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/dashboard/stats")).andExpect(status().isUnauthorized());
        mvc.perform(withJson(post("/api/students"), student("A", "a@x.com", "IT")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/students").cookie(new Cookie("access_token", "forged.jwt.value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingRequestsRequireCsrfHeader() throws Exception {
        Cookie[] auth = registerUser(uniqueEmail("csrf"));
        mvc.perform(post("/api/students").cookie(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(student("A", "a@x.com", "IT"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("X-Requested-With")));
    }

    @Test
    void studentCrudSearchAndDashboard() throws Exception {
        Cookie[] auth = registerUser(uniqueEmail("crud"));

        String aliceId = createStudent(auth, student("Alice Johnson", "alice@uni.edu", "Computer Science"));
        createStudent(auth, student("Bob Smith", "bob@uni.edu", "Mathematics"));
        createStudent(auth, student("Carol White", "carol@uni.edu", "Computer Science"));

        // duplicate email for the same owner
        mvc.perform(withJson(post("/api/students"), student("Other", "ALICE@uni.edu", "Physics")).cookie(auth))
                .andExpect(status().isConflict());

        // validation
        mvc.perform(withJson(post("/api/students"), Map.of("fullName", "X", "email", "bad",
                        "gender", "Unknown", "dateOfBirth", "2999-01-01")).cookie(auth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gender").exists())
                .andExpect(jsonPath("$.errors.dateOfBirth").exists())
                .andExpect(jsonPath("$.errors.department").exists());

        mvc.perform(get("/api/students").cookie(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mvc.perform(get("/api/students/" + aliceId).cookie(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Alice Johnson"))
                .andExpect(jsonPath("$.data.dateOfBirth").value("2003-05-14"));

        mvc.perform(withJson(put("/api/students/" + aliceId),
                        student("Alice Cooper", "alice@uni.edu", "Physics")).cookie(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Alice Cooper"))
                .andExpect(jsonPath("$.data.department").value("Physics"));

        // updating to another existing student's email is rejected
        mvc.perform(withJson(put("/api/students/" + aliceId),
                        student("Alice Cooper", "bob@uni.edu", "Physics")).cookie(auth))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/students/search").param("keyword", "smith").cookie(auth))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].fullName").value("Bob Smith"));
        mvc.perform(get("/api/students/search").param("keyword", "CAROL@").cookie(auth))
                .andExpect(jsonPath("$.data.length()").value(1));

        JsonNode stats = body(mvc.perform(get("/api/dashboard/stats").cookie(auth)).andExpect(status().isOk()));
        assertThat(stats.at("/data/totalStudents").asLong()).isEqualTo(3);
        assertThat(stats.at("/data/studentsLast7Days").asLong()).isEqualTo(3);
        assertThat(stats.at("/data/totalDepartments").asLong()).isEqualTo(3);
        assertThat(stats.at("/data/recentStudents").size()).isEqualTo(3);

        mvc.perform(delete("/api/students/" + aliceId).header(XRW, "x").cookie(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Student deleted successfully"));
        mvc.perform(get("/api/students/" + aliceId).cookie(auth))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/students/not-a-uuid").cookie(auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usersCannotAccessEachOthersStudents() throws Exception {
        Cookie[] owner = registerUser(uniqueEmail("owner"));
        Cookie[] intruder = registerUser(uniqueEmail("intruder"));

        String id = createStudent(owner, student("Private Student", "private@uni.edu", "Biology"));

        mvc.perform(get("/api/students/" + id).cookie(intruder)).andExpect(status().isNotFound());
        mvc.perform(withJson(put("/api/students/" + id), student("Hacked", "h@x.com", "X")).cookie(intruder))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/students/" + id).header(XRW, "x").cookie(intruder))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/students").cookie(intruder))
                .andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(get("/api/students/search").param("keyword", "Private").cookie(intruder))
                .andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(get("/api/dashboard/stats").cookie(intruder))
                .andExpect(jsonPath("$.data.totalStudents").value(0));

        // the intruder may use the same student email in their own records
        createStudent(intruder, student("Their Student", "private@uni.edu", "Biology"));

        // the owner's record is untouched
        mvc.perform(get("/api/students/" + id).cookie(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Private Student"));
    }

    @Test
    void corsAllowsOnlyConfiguredOriginWithCredentials() throws Exception {
        mvc.perform(options("/api/students")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-requested-with"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mvc.perform(options("/api/students")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
