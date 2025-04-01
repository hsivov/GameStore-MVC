package org.example.gamestoreapp.repository;

import org.example.gamestoreapp.model.entity.Notification;
import org.example.gamestoreapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<List<Notification>> findAllByUser(User user);

    long countByUserAndUnreadIsTrue(User user);
}
