package com.rephelper.domain.port.in;

import java.util.List;
import java.util.UUID;

import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;

/**
 * Porta de entrada definindo os casos de uso relacionados a repúblicas.
 * Esta interface será implementada pelo serviço de domínio.
 */
public interface RepublicServicePort {
    /**
     * Cria uma nova república
     */
    Republic createRepublic(Republic republic);

    /**
     * Obtém todas as repúblicas
     */
    List<Republic> getAllRepublics();

    /**
     * Obtém uma república pelo ID
     */
    Republic getRepublicById(UUID id);

    /**
     * Obtém uma república pelo código de convite
     */
    Republic getRepublicByCode(String code);

    /**
     * Atualiza uma república existente
     */
    Republic updateRepublic(UUID id, String name, Address address);

    /**
     * Remove uma república
     */
    void deleteRepublic(UUID id);

    /**
     * Permite um usuário entrar em uma república usando o código de convite
     */
    User joinRepublicByCode(UUID userId, String code);

    /**
     * Transfere a propriedade da república para outro usuário
     */
    Republic transferOwnership(UUID republicId, UUID newOwnerId);

    /**
     * Obtém os membros de uma república
     */
    List<User> getRepublicMembers(UUID republicId);

      /**
     * Adiciona um usuário como administrador da república
     */
    Republic addRepublicAdmin(UUID republicId, UUID userId);

    /**
     * Remove o status de administrador de um usuário na república
     */
    Republic removeRepublicAdmin(UUID republicId, UUID userId);
}