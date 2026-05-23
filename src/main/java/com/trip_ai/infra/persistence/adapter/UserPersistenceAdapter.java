package com.trip_ai.infra.persistence.adapter;

import com.trip_ai.domain.model.User;
import com.trip_ai.domain.port.out.UserRepositoryPort;
import com.trip_ai.infra.persistence.entity.UserEntity;
import com.trip_ai.infra.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepo;

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepo.findByPhone(phone).map(this::toDomain);
    }

    @Override
    public User save(User domain) {
        UserEntity entity = domain.getId() != null
            ? jpaRepo.findById(domain.getId()).orElseGet(UserEntity::new)
            : new UserEntity();
        entity.setPhone(domain.getPhone());
        entity.setName(domain.getName());
        if (domain.getStatus() != null) entity.setStatus(domain.getStatus());
        return toDomain(jpaRepo.save(entity));
    }

    User toDomain(UserEntity e) {
        return User.builder()
            .id(e.getId())
            .phone(e.getPhone())
            .name(e.getName())
            .status(e.getStatus())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}
