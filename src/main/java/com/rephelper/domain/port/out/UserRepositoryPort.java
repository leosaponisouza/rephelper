package com.rephelper.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rephelper.domain.model.User;

/**
 * Porta de saída para operações de repositório relacionadas a usuários.
 * Define o contrato que a camada de infraestrutura deve implementar.
 */
public interface UserRepositoryPort {
    /**
     * Salva ou atualiza um usuário
     */
    User save(User user);

    /**
     * Busca um usuário pelo ID
     */
    Optional<User> findById(UUID id);

    /**
     * Busca um usuário pelo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca um usuário pelo UID do Firebase
     */
    Optional<User> findByFirebaseUid(String firebaseUid);

    /**
     * Verifica se existe um usuário com o email fornecido
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se existe um usuário com o UID do Firebase fornecido
     */
    boolean existsByFirebaseUid(String firebaseUid);

    /**
     * Busca todos os usuários
     */
    List<User> findAll();

    /**
     * Busca usuários pelo papel
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Busca usuários pela república atual
     */
    List<User> findByCurrentRepublicId(UUID republicId);

    /**
     * Busca usuários ativos em uma república
     */
    List<User> findByCurrentRepublicIdAndIsActiveResident(UUID republicId, Boolean isActiveResident);

    /**
     * Remove um usuário
     */
    void delete(User user);
}