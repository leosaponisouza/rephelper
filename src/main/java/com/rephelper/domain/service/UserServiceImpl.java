package com.rephelper.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserServicePort {

    private final UserRepositoryPort userRepository;
    private final RepublicRepositoryPort republicRepository;

    @Override
    public User createUser(User user) {
        // Validar se email e firebaseUid são únicos
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already in use: " + user.getEmail());
        }

        if (userRepository.existsByFirebaseUid(user.getFirebaseUid())) {
            throw new ValidationException("Firebase UID already exists: " + user.getFirebaseUid());
        }

        // Definir valores padrão
        if (user.getStatus() == null) {
            user = User.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .profilePictureUrl(user.getProfilePictureUrl())
                    .firebaseUid(user.getFirebaseUid())
                    .provider(user.getProvider())
                    .currentRepublic(user.getCurrentRepublic())
                    .isAdmin(user.getIsAdmin())
                    .entryDate(user.getEntryDate())
                    .departureDate(user.getDepartureDate())
                    .status("active")
                    .build();
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Firebase UID: " + firebaseUid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(UUID id, User userDetails) {
        User existingUser = getUserById(id);

        // Verificar se o email está sendo alterado e já existe
        if (userDetails.getEmail() != null && !existingUser.getEmail().equals(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new ValidationException("Email already in use: " + userDetails.getEmail());
        }

        // Atualizar campos
        existingUser.updateProfile(
                userDetails.getName(),
                userDetails.getNickname(),
                userDetails.getEmail(),
                userDetails.getPhoneNumber(),
                userDetails.getProfilePictureUrl());

        return userRepository.save(existingUser);
    }

    @Override
    public User updateLastLogin(UUID id) {
        User user = getUserById(id);
        user.updateLastLogin();
        return userRepository.save(user);
    }

    @Override
    public User updateUserRepublic(UUID userId, UUID republicId) {
        User user = getUserById(userId);

        if (republicId != null) {
            Republic republic = republicRepository.findById(republicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + republicId));

            user.joinRepublic(republic);
        } else {
            user.leaveRepublic();
        }

        return userRepository.save(user);
    }

    @Override
    public User setResidentStatus(UUID userId, boolean isActive) {
        User user = getUserById(userId);

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Atualizar status de residente
        if (isActive) {
            // Se estamos ativando, limpar data de saída
            user = User.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .profilePictureUrl(user.getProfilePictureUrl())
                    .firebaseUid(user.getFirebaseUid())
                    .provider(user.getProvider())
                    .currentRepublic(user.getCurrentRepublic())
                    .isAdmin(user.getIsAdmin())
                    .entryDate(user.getEntryDate())
                    .departureDate(null) // Limpar data de saída
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .lastLogin(user.getLastLogin())
                    .build();
        } else {
            // Se estamos desativando, definir data de saída
            user = User.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .profilePictureUrl(user.getProfilePictureUrl())
                    .firebaseUid(user.getFirebaseUid())
                    .provider(user.getProvider())
                    .currentRepublic(user.getCurrentRepublic())
                    .isAdmin(user.getIsAdmin())
                    .entryDate(user.getEntryDate())
                    .departureDate(LocalDateTime.now()) // Definir data de saída atual
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .lastLogin(user.getLastLogin())
                    .build();
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getResidentsByRepublicId(UUID republicId) {
        return userRepository.findByCurrentRepublicId(republicId);
    }


    @Override
    public void deleteUser(UUID id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}