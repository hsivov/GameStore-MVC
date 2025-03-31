package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
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

    private User mockUser;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("username");
        mockUser.setPassword("password");
        mockUser.setEmail("email");
        mockUser.setFirstName("firstName");
        mockUser.setLastName("lastName");
        mockUser.setRole(UserRole.USER);
        mockUser.setEnabled(true);
        mockUser.setAge(25);

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(modelMapper.map(mockUser, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userServiceImpl.getUserById(1L).orElse(null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userDTO.getId());
        assertThat(result.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(result.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(result.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(result.getRole()).isEqualTo(userDTO.getRole());
        assertThat(result.isEnabled()).isEqualTo(userDTO.isEnabled());

        verify(userRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(mockUser, UserDTO.class);
    }

    @Test
    void testGetUserById_NotExist_ShouldReturnNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserDTO result = userServiceImpl.getUserById(99L).orElse(null);

        assertThat(result).isNull();

        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void testGetUserProfile_ShouldReturnEditProfileDTO() {
        when(userHelperService.getUser()).thenReturn(mockUser);

        EditProfileDTO result = userServiceImpl.getUserProfile();

        assertNotNull(result);
        assertEquals(mockUser.getEmail(), result.getEmail());
        assertEquals(mockUser.getFirstName(), result.getFirstName());
        assertEquals(mockUser.getLastName(), result.getLastName());
        assertEquals(mockUser.getAge(), result.getAge());

        verify(userHelperService, times(1)).getUser();
    }

    @Test
    void testEditProfile() {
        when(userHelperService.getUser()).thenReturn(mockUser);

        EditProfileDTO editProfileDTO = new EditProfileDTO();
        editProfileDTO.setEmail("new@example.com");
        editProfileDTO.setFirstName("NewFirst");
        editProfileDTO.setLastName("NewLast");
        editProfileDTO.setAge(30);

        userServiceImpl.editProfile(editProfileDTO);

        assertEquals("new@example.com", mockUser.getEmail());
        assertEquals("NewFirst", mockUser.getFirstName());
        assertEquals("NewLast", mockUser.getLastName());
        assertEquals(30, mockUser.getAge());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetProfileView_ShouldReturnUserProfileViewModel() {
        when(userHelperService.getUser()).thenReturn(mockUser);

        UserProfileViewModel result = userServiceImpl.getProfileView();

        assertEquals(mockUser.getUsername(), result.getUsername());
        assertEquals(mockUser.getRole().toString(), result.getRole());
        assertEquals(mockUser.getAge(), result.getAge());
        assertEquals(mockUser.getEmail(), result.getEmail());
        assertEquals(mockUser.getFirstName(), result.getFirstName());
        assertEquals(mockUser.getLastName(), result.getLastName());
        assertEquals(mockUser.getProfileImageUrl(), result.getProfileImageUrl());

        verify(userHelperService, times(1)).getUser();
    }
}
