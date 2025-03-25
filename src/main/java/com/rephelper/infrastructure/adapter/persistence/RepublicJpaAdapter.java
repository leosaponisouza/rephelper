package com.rephelper.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do adaptador para o repositório de repúblicas usando JPA.
 */
@Component
@RequiredArgsConstructor
public class RepublicJpaAdapter implements RepublicRepositoryPort {

    private final RepublicJpaRepository republicJpaRepository;
    private final RepublicMapper republicMapper;
    private final UserMapper userMapper;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    @Override
    public Republic save(Republic republic) {
        RepublicJpaEntity republicEntity = republicMapper.toJpaEntity(republic);
        RepublicJpaEntity savedEntity = republicJpaRepository.save(republicEntity);
        return republicMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Republic> findById(UUID id) {
        return republicJpaRepository.findById(id)
                .map(republicMapper::toDomainEntity);
    }

    @Override
    public Optional<Republic> findByCode(String code) {
        return republicJpaRepository.findByCode(code)
                .map(republicMapper::toDomainEntity);
    }

    @Override
    public boolean existsByCode(String code) {
        return republicJpaRepository.existsByCode(code);
    }

    @Override
    public List<Republic> findAll() {
        return republicJpaRepository.findAll().stream()
                .map(republicMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Republic> findByOwnerId(UUID ownerId) {
        return republicJpaRepository.findByOwnerUuid(ownerId).stream()
                .map(republicMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Republic republic) {
        republicJpaRepository.deleteById(republic.getId());
    }

    @Override
    public List<User> findMembers(UUID republicId) {
        List<UserJpaEntity> memberEntities = republicJpaRepository.findMembers(republicId);
        return memberEntities.stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toList());
    }


    @Override
    public String generateUniqueCode() {
        Random random = new Random();
        String code;
        boolean isUnique = false;

        // Continua gerando códigos até encontrar um único
        do {
            StringBuilder codeBuilder = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                codeBuilder.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = codeBuilder.toString();

            isUnique = !republicJpaRepository.existsByCode(code);
        } while (!isUnique);

        return code;
    }

    @Override
    public boolean isValidCodeFormat(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }
        
        for (char c : code.toCharArray()) {
            if (CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int getCodeLength() {
        return CODE_LENGTH;
    }
}