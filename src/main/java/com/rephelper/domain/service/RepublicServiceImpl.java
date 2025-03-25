package com.rephelper.domain.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.RepublicServicePort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RepublicServiceImpl implements RepublicServicePort {

    private final RepublicRepositoryPort republicRepository;
    private final UserRepositoryPort userRepository;

    @Override
    public Republic createRepublic(Republic republic) {
        // Verificar se o dono existe
        if (republic.getOwner() == null || republic.getOwner().getId() == null) {
            throw new ValidationException("Owner is required");
        }

        User owner = userRepository.findById(republic.getOwner().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // Gerar código único
        String code = republicRepository.generateUniqueCode();

        // Criar nova república
        Republic newRepublic = Republic.builder()
                .id(republic.getId())
                .name(republic.getName())
                .code(code)
                .address(republic.getAddress())
                .owner(owner)
                .createdAt(republic.getCreatedAt())
                .updatedAt(republic.getUpdatedAt())
                .build();

        // Salvar república
        Republic savedRepublic = republicRepository.save(newRepublic);

        // Adicionar dono como membro e definir como admin
        owner = User.builder()
                .id(owner.getId())
                .name(owner.getName())
                .email(owner.getEmail())
                .phoneNumber(owner.getPhoneNumber())
                .profilePictureUrl(owner.getProfilePictureUrl())
                .firebaseUid(owner.getFirebaseUid())
                .provider(owner.getProvider())
                .currentRepublic(savedRepublic)
                .isAdmin(true)
                .entryDate(owner.getEntryDate())
                .departureDate(owner.getDepartureDate())
                .status(owner.getStatus())
                .createdAt(owner.getCreatedAt())
                .lastLogin(owner.getLastLogin())
                .build();

        userRepository.save(owner);

        return savedRepublic;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Republic> getAllRepublics() {
        return republicRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Republic getRepublicById(UUID id) {
        return republicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Republic getRepublicByCode(String code) {
        return republicRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Republic not found with code: " + code));
    }

    @Override
    public Republic updateRepublic(UUID id, String name, Address address) {
        Republic republic = getRepublicById(id);

        republic.updateDetails(name, address);

        return republicRepository.save(republic);
    }

    @Override
    public void deleteRepublic(UUID id) {
        Republic republic = getRepublicById(id);

        // Remover todos os membros da república
        List<User> members = getRepublicMembers(id);

        for (User member : members) {
            member.leaveRepublic();
            userRepository.save(member);
        }

        // Deletar república
        republicRepository.delete(republic);
    }

    @Override
    public User joinRepublicByCode(UUID userId, String code) {
        // Buscar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Buscar república pelo código
        Republic republic = getRepublicByCode(code);

        // Verificar se o usuário já está nessa república
        if (user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId() != null &&
                user.getCurrentRepublic().getId().equals(republic.getId())) {
            throw new ValidationException("User is already a member of this republic");
        }

        // Associar usuário à república
        user.joinRepublic(republic);

        return userRepository.save(user);
    }

    @Override
    public Republic transferOwnership(UUID republicId, UUID newOwnerId) {
        Republic republic = getRepublicById(republicId);

        // Verificar se o novo dono é membro da república
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("New owner not found with id: " + newOwnerId));

        if (newOwner.getCurrentRepublic() == null ||
                !newOwner.getCurrentRepublic().getId().equals(republicId)) {
            throw new ForbiddenException("New owner must be a member of the republic");
        }

        // Transferir propriedade
        republic.transferOwnership(newOwner);

        return republicRepository.save(republic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getRepublicMembers(UUID republicId) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return republicRepository.findMembers(republicId);
    }

    @Override
    public Republic addRepublicAdmin(UUID republicId, UUID userId) {
        Republic republic = getRepublicById(republicId);

        // Verificar se o usuário é membro da república
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getCurrentRepublic() == null ||
                !user.getCurrentRepublic().getId().equals(republicId)) {
            throw new ForbiddenException("User must be a member of the republic");
        }

        // Definir usuário como admin
        user.makeRepublicAdmin();
        userRepository.save(user);

        return republic;
    }

    @Override
    public Republic removeRepublicAdmin(UUID republicId, UUID userId) {
        Republic republic = getRepublicById(republicId);

        // Verificar se o usuário é membro da república
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Não permitir remover o dono como admin
        if (republic.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Cannot remove admin status from the republic owner");
        }

        // Remover status de admin
        user.removeRepublicAdmin();
        userRepository.save(user);

        return republic;
    }

    @Override
    public Republic regenerateCode(UUID republicId) {
        Republic republic = getRepublicById(republicId);
        
        // Gerar novo código único
        String newCode = republicRepository.generateUniqueCode();
        
        // Atualizar o código da república usando o método do modelo
        republic.updateCode(newCode);
                
        return republicRepository.save(republic);
    }
    
    @Override
    public Republic regenerateCodeWithCustomCode(UUID republicId, String customCode) {
        Republic republic = getRepublicById(republicId);
        
        // Validar o formato do código personalizado
        if (!republicRepository.isValidCodeFormat(customCode)) {
            throw new ValidationException("O código personalizado deve ter " + 
                republicRepository.getCodeLength() + 
                " caracteres e conter apenas letras maiúsculas e números");
        }
        
        // Verificar se o código já existe
        if (republicRepository.existsByCode(customCode)) {
            throw new ValidationException("O código personalizado já está em uso");
        }
        
        // Atualizar o código da república
        republic.updateCode(customCode);
                
        return republicRepository.save(republic);
    }
    
    @Override
    public int getCodeLength() {
        return republicRepository.getCodeLength();
    }
}