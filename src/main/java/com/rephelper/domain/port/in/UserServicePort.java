package com.rephelper.domain.port.in;

import java.util.List;
import java.util.UUID;

import com.rephelper.domain.model.User;

/**
 * Porta de entrada definindo os casos de uso relacionados a usuários.
 * Esta interface será implementada pelo serviço de domínio.
 */
public interface UserServicePort {
    /**
     * Cria um novo usuário
     */
    User createUser(User user);

    /**
     * Atualiza um usuário existente
     */
    User updateUser(UUID id, User userDetails);

    /**
     * Obtém um usuário pelo ID
     */
    User getUserById(UUID id);

    /**
     * Obtém um usuário pelo UID do Firebase
     */
    User getUserByFirebaseUid(String firebaseUid);

    /**
     * Obtém todos os usuários
     */
    List<User> getAllUsers();

    /**
     * Atualiza a data do último login do usuário
     */
    User updateLastLogin(UUID id);

    /**
     * Atualiza a república associada ao usuário
     */
    User updateUserRepublic(UUID userId, UUID republicId);

    /**
     * Define o status do residente
     */
    User setResidentStatus(UUID userId, boolean isActive);

    /**
     * Obtém os residentes de uma república
     */
    List<User> getResidentsByRepublicId(UUID republicId);

    /**
     * Remove um usuário
     */
    void deleteUser(UUID id);
}