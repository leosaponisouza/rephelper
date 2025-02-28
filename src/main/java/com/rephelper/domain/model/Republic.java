package com.rephelper.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio que representa uma república.
 * Contém regras de negócio e comportamentos relacionados a repúblicas.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Republic {
    private UUID id;
    private String name;
    private String code; // Código único para convites
    private Address address;
    private User owner;
    @Builder.Default
    private List<User> members = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Verifica se um usuário é membro desta república
     */
    public boolean isMember(User user) {
        if (user == null) return false;

        // Se o usuário tem a referência direta para esta república
        if (user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId() != null &&
                user.getCurrentRepublic().getId().equals(this.id)) {
            return true;
        }

        // Ou se está na lista de membros
        return members.stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }

    /**
     * Verifica se um usuário é o dono da república
     */
    public boolean isOwner(User user) {
        return user != null && owner != null &&
                owner.getId() != null &&
                owner.getId().equals(user.getId());
    }

    /**
     * Verifica se um usuário é administrador desta república
     */
    public boolean isAdmin(User user) {
        return user != null &&
                isMember(user) &&
                (Boolean.TRUE.equals(user.getIsAdmin()) || isOwner(user));
    }

    /**
     * Adiciona um membro à república
     */
    public void addMember(User user) {
        if (user != null && !isMember(user)) {
            members.add(user);
        }
    }

    /**
     * Remove um membro da república
     */
    public void removeMember(User user) {
        if (user != null) {
            members.removeIf(member -> member.getId().equals(user.getId()));
        }
    }

    /**
     * Atualiza informações básicas da república
     */
    public void updateDetails(String name, Address address) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (address != null) {
            this.address = address;
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Transfere a propriedade da república para outro usuário
     */
    public void transferOwnership(User newOwner) {
        if (newOwner != null && isMember(newOwner)) {
            // Garante que o novo dono seja administrador
            newOwner.makeRepublicAdmin();
            this.owner = newOwner;
            this.updatedAt = LocalDateTime.now();
        }
    }
}