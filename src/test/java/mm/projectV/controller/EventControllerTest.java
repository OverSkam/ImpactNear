package mm.projectV.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mm.projectV.dto.EventRequest;
import mm.projectV.mapper.EventMapper;
import mm.projectV.model.CustomUserDetails;
import mm.projectV.model.User;
import mm.projectV.service.AuthService;
import mm.projectV.service.CustomUserDetailsService;
import mm.projectV.service.EventService;
import mm.projectV.service.UserService;
import mm.projectV.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(ObjectMapper.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // The primary service you are testing
    @MockitoBean
    private EventService eventService;

    // --- Mocks needed because Spring scans OTHER controllers ---

    // For AuthController
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    // For UserController (The latest error)
    @MockitoBean
    private UserService userService;

    // If you have a Facade or other common utils injected in controllers:
    @MockitoBean
    private EventMapper eventMapper;

    private CustomUserDetails mockUserDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Creating the principal object the controller expects
        mockUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @WithMockUser // Simulates a security context
    void getEvent_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/events/get/100")
                .with(user(mockUserDetails))) // Injecting our custom user
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event was fetched successfully"));

        verify(eventService).getOrganizedEvent(any(User.class), eq(100L));
    }

    @Test
    @WithMockUser
    void createEvent_ShouldReturnOk() throws Exception {
        EventRequest request = new EventRequest();
        request.setName("New Workshop");

        mockMvc.perform(post("/api/v1/events/create")
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Sending JSON body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event was created"));

        verify(eventService).createEvent(any(User.class), any(EventRequest.class));
    }

    @Test
    @WithMockUser
    void deleteEvent_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/events/delete/100")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event was deleted successfully"));

        verify(eventService).deleteEvent(any(User.class), eq(100L));
    }
}