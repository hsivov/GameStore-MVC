package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestController userRestController;

    @Test
    void testGetUserById_WhenUserExists() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userRestController).build();
        Long userId = 1L;

        UserDTO mockUser = new UserDTO();
        mockUser.setId(userId);

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserById_WhenUserDoesNotExists() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userRestController).build();
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
    }
}