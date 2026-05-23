package com.trip_ai.domain.port.out;

import com.trip_ai.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByPhone(String phone);
    User save(User user);
}
