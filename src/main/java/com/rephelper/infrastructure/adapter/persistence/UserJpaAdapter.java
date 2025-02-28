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
        // Adicione log para verificar o firebaseUid sendo buscado
        System.out.println("Searching for user with Firebase UID: " + firebaseUid);

        // Verifique se o repositório está realmente encontrando o usuário
        Optional<UserJpaEntity> userEntityOptional = userJpaRepository.findByFirebaseUid(firebaseUid);

        // Log para verificar o resultado da busca
        if (!userEntityOptional.isPresent()) {
            System.out.println("No user found with Firebase UID: " + firebaseUid);
            // Você pode querer adicionar mais logs ou verificações
            // Por exemplo, verificar se o usuário existe no banco de dados
            long userCount = userJpaRepository.count();
            System.out.println("Total users in database: " + userCount);
        }

        // Mapeie apenas se o optional contém um valor
        return userEntityOptional
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
    public List<User> findByCurrentRepublicId(UUID republicId) {
        return userJpaRepository.findByCurrentRepublicUuid(republicId).stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toList());
    }


    @Override
    public void delete(User user) {
        userJpaRepository.deleteById(user.getId());
    }
}