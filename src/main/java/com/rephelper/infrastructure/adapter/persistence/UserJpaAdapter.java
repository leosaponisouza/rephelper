package com.rephelper.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rephelper.domain.model.User;
import com.rephelper.domain.port.out.UserRepositoryPort;
import com.rephelper.infrastructure.entity.UserJpaEntity;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do adaptador para o repositório de usuários usando JPA.
 */
@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity userEntity = userMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = userJpaRepository.save(userEntity);
        return userMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomainEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomainEntity);
    }

    @Override
    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userJpaRepository.findByFirebaseUid(firebaseUid)
                .map(userMapper::toDomainEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByFirebaseUid(String firebaseUid) {
        return userJpaRepository.existsByFirebaseUid(firebaseUid);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(User.UserRole role) {
        UserJpaEntity.UserRole jpaRole = userMapper.mapToJpaUserRole(role);
        return userJpaRepository.findByRole(jpaRole).stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByCurrentRepublicId(UUID republicId) {
        return userJpaRepository.findByCurrentRepublicId(republicId).stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByCurrentRepublicIdAndIsActiveResident(UUID republicId, Boolean isActiveResident) {
        return userJpaRepository.findByCurrentRepublicIdAndIsActiveResident(republicId, isActiveResident).stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(User user) {
        userJpaRepository.deleteById(user.getId());
    }
}