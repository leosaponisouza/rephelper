package com.rephelper.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;

/**
 * Porta de saída para operações de repositório relacionadas a repúblicas.
 * Define o contrato que a camada de infraestrutura deve implementar.
 */
public interface RepublicRepositoryPort {
    /**
     * Salva ou atualiza uma república
     */
    Republic save(Republic republic);

    /**
     * Busca uma república pelo ID
     */
    Optional<Republic> findById(UUID id);

    /**
     * Busca uma república pelo código de convite
     */
    Optional<Republic> findByCode(String code);

    /**
     * Verifica se existe uma república com o código fornecido
     */
    boolean existsByCode(String code);

    /**
     * Busca todas as repúblicas
     */
    List<Republic> findAll();

    /**
     * Busca repúblicas pelo dono
     */
    List<Republic> findByOwnerId(UUID ownerId);

    /**
     * Remove uma república
     */
    void delete(Republic republic);

    /**
     * Busca membros de uma república
     */
    List<User> findMembers(UUID republicId);

    /**
     * Gera um código único para uma república
     */
    String generateUniqueCode();

    /**
     * Valida se o formato do código personalizado é válido
     */
    boolean isValidCodeFormat(String code);

    /**
     * Obtém o tamanho do código de convite
     */
    int getCodeLength();
}