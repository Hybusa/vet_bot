package com.example.shalitbot.repository;

import com.example.shalitbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(Long chatID);

    boolean existsByChatId(Long chatId);
}
