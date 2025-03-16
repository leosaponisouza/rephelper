package com.rephelper.domain.service;

import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.RepublicFinances;
import com.rephelper.domain.port.in.RepublicFinancesServicePort;
import com.rephelper.domain.port.out.RepublicFinancesRepositoryPort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RepublicFinancesServiceImpl implements RepublicFinancesServicePort {

    private final RepublicFinancesRepositoryPort republicFinancesRepository;
    private final RepublicRepositoryPort republicRepository;

    @Override
    public RepublicFinances getOrCreateRepublicFinances(UUID republicId) {
        // Check if finances exist for this republic
        return republicFinancesRepository.findByRepublicId(republicId)
                .orElseGet(() -> {
                    // If not, create new finances
                    Republic republic = republicRepository.findById(republicId)
                            .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + republicId));

                    RepublicFinances newFinances = RepublicFinances.builder()
                            .republic(republic)
                            .currentBalance(BigDecimal.ZERO)
                            .lastUpdated(LocalDateTime.now())
                            .build();

                    return republicFinancesRepository.save(newFinances);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public RepublicFinances getRepublicFinances(UUID republicId) {
        return republicFinancesRepository.findByRepublicId(republicId)
                .orElseThrow(() -> new ResourceNotFoundException("Finances not found for republic with id: " + republicId));
    }

    @Override
    public RepublicFinances updateBalance(UUID republicId, BigDecimal amount) {
        // Get or create finances
        RepublicFinances finances = getOrCreateRepublicFinances(republicId);

        // Update balance
        finances.updateBalance(amount);

        // Save updated finances
        return republicFinancesRepository.save(finances);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughBalance(UUID republicId, BigDecimal amount) {
        try {
            // Try to get finances
            RepublicFinances finances = getRepublicFinances(republicId);
            return finances.hasEnoughBalance(amount);
        } catch (ResourceNotFoundException e) {
            // If finances don't exist, check if amount is positive (contribution)
            return amount != null && amount.compareTo(BigDecimal.ZERO) <= 0;
        }
    }
}