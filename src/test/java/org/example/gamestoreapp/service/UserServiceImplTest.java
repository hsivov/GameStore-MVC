package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.impl.UserServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserHelperService userHelperService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setRole(UserRole.USER);
        user.setEnabled(true);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("firstName");
        userDTO.setLastName("lastName");
        userDTO.setEmail("email");
        userDTO.setRole(UserRole.USER);
        userDTO.setEnabled(true);
    }

    @Test
    void testGetUserById_ShouldReturnUserDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userServiceImpl.getUserById(1L).orElse(null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userDTO.getId());
        assertThat(result.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(result.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(result.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(result.getRole()).isEqualTo(userDTO.getRole());
        assertThat(result.isEnabled()).isEqualTo(userDTO.isEnabled());

        verify(userRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(user, UserDTO.class);
    }
}
