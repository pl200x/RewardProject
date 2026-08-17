package com.example.Music_management.controller;

import com.example.Music_management.entity.User;
import com.example.Music_management.controller.cmd.RegisterCmd;
import com.example.Music_management.security.JwtUtil;
import com.example.Music_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private StubUserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = new StubUserService();
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "jwtUtil", new StubJwtUtil());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginAcceptsCredentialsInPostBody() throws Exception {
        User user = new User();
        user.setId(7);
        user.setName("alice");
        user.setInterest("jazz");
        user.setAvatar(3);
        userService.loginResult = user;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userName": "alice",
                                  "password": "correct horse battery staple"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed-token"))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.baseVO.success").value(true));

        assertEquals("alice", userService.receivedUserName);
        assertEquals("correct horse battery staple", userService.receivedPassword);
    }

    @Test
    void loginNoLongerAcceptsCredentialsInQueryString() throws Exception {
        mockMvc.perform(get("/user/login")
                        .param("userName", "alice")
                        .param("password", "visible-in-url"))
                .andExpect(status().isMethodNotAllowed());
    }

    private static class StubJwtUtil extends JwtUtil {
        @Override
        public String generate(int userId, String userName) {
            return "signed-token";
        }
    }

    private static class StubUserService implements UserService {
        private User loginResult;
        private String receivedUserName;
        private String receivedPassword;

        @Override
        public User login(String userName, String password) {
            receivedUserName = userName;
            receivedPassword = password;
            return loginResult;
        }

        @Override
        public void register(RegisterCmd registerCmd) {
        }

        @Override
        public void logout(int userId) {
        }

        @Override
        public void changeInterest(int userId, String interest) {
        }

        @Override
        public void changeAvatar(int userId, int avatar) {
        }

        @Override
        public Set<User> findAllUserByIds(Set<Integer> userIds) {
            return Set.of();
        }

        @Override
        public User queryById(int id) {
            return null;
        }
    }
}
