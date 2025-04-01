package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.NotificationDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.entity.Notification;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.NotificationRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserHelperService userHelperService;
    private final ModelMapper modelMapper;
    private final NotificationRepository notificationRepository;

    public UserServiceImpl(UserRepository userRepository,
                           UserHelperService userHelperService,
                           ModelMapper modelMapper, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.userHelperService = userHelperService;
        this.modelMapper = modelMapper;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Optional<UserDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    public EditProfileDTO getUserProfile() {
        User currentUser = userHelperService.getUser();

        EditProfileDTO editProfileDTO = new EditProfileDTO();
        editProfileDTO.setEmail(currentUser.getEmail());
        editProfileDTO.setFirstName(currentUser.getFirstName());
        editProfileDTO.setLastName(currentUser.getLastName());
        editProfileDTO.setAge(currentUser.getAge());

        return editProfileDTO;
    }

    @Override
    public void editProfile(EditProfileDTO editProfileDTO) {
        User currentUser = userHelperService.getUser();

        currentUser.setEmail(editProfileDTO.getEmail());
        currentUser.setFirstName(editProfileDTO.getFirstName());
        currentUser.setLastName(editProfileDTO.getLastName());
        currentUser.setAge(editProfileDTO.getAge());

        userRepository.save(currentUser);
    }

    @Override
    public List<NotificationDTO> getUserNotifications() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        User currentUser = userHelperService.getUser();

        return notificationRepository.findAllByUser(currentUser)
                .orElse(Collections.emptyList())
                .stream()
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setId(notification.getId());
                    dto.setMessage(notification.getMessage());
                    dto.setUnread(notification.unread());
                    dto.setCreatedAt(formatter.format(notification.getCreatedAt()));

                    return dto;
                })
                .toList();
    }

    @Override
    public long countUnreadNotifications() {
        User currentUser = userHelperService.getUser();

        return notificationRepository.countByUserAndUnreadIsTrue(currentUser);
    }

    @Override
    public void setNotificationsAsRead() {
        User currentUser = userHelperService.getUser();

        List<Notification> notifications = notificationRepository.findAllByUser(currentUser)
                .orElse(Collections.emptyList())
                .stream()
                .peek(notification -> notification.setUnread(false))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    public UserProfileViewModel getProfileView() {
        User currentUser = userHelperService.getUser();
        UserProfileViewModel userProfileViewModel = new UserProfileViewModel();

        userProfileViewModel.setUsername(currentUser.getUsername());
        userProfileViewModel.setRole(currentUser.getRole().toString());
        userProfileViewModel.setAge(currentUser.getAge());
        userProfileViewModel.setEmail(currentUser.getEmail());
        userProfileViewModel.setFirstName(currentUser.getFirstName());
        userProfileViewModel.setLastName(currentUser.getLastName());
        userProfileViewModel.setProfileImageUrl(currentUser.getProfileImageUrl());

        return userProfileViewModel;
    }
}
