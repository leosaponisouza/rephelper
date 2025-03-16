package com.rephelper.domain.service;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Income;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.IncomeServicePort;
import com.rephelper.domain.port.in.RepublicFinancesServicePort;
import com.rephelper.domain.port.out.IncomeRepositoryPort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeServiceImpl implements IncomeServicePort {

    private final IncomeRepositoryPort incomeRepository;
    private final UserRepositoryPort userRepository;
    private final RepublicRepositoryPort republicRepository;
    private final RepublicFinancesServicePort republicFinancesService;

    @Override
    public Income createIncome(Income income, UUID contributorId) {
        // Validate user
        User contributor = userRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + contributorId));

        // Verify user belongs to the republic
        if (contributor.getCurrentRepublic() == null ||
                !contributor.getCurrentRepublic().getId().equals(income.getRepublic().getId())) {
            throw new ForbiddenException("You can only create incomes for your own republic");
        }

        // Verify republic exists
        Income finalIncome = income;
        Republic republic = republicRepository.findById(income.getRepublic().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + finalIncome.getRepublic().getId()));

        // Validate amount is positive
        if (income.getAmount() == null || income.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Income amount must be positive");
        }

        // Set contributor and timestamp
        income = Income.builder()
                .republic(republic)
                .contributor(contributor)
                .description(income.getDescription())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate() != null ? income.getIncomeDate() : LocalDateTime.now())
                .source(income.getSource())
                .createdAt(LocalDateTime.now())
                .build();

        // Save income
        Income savedIncome = incomeRepository.save(income);

        // Update republic finances
        republicFinancesService.updateBalance(republic.getId(), income.getAmount());

        return savedIncome;
    }

    @Override
    public Income updateIncome(Long id, String description, BigDecimal amount,
                               LocalDateTime incomeDate, String source, UUID modifierId) {
        // Get income
        Income income = getIncomeById(id);

        // Validate user
        User modifier = userRepository.findById(modifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierId));

        // Check if user is contributor or admin
        boolean isContributor = income.getContributor() != null && income.getContributor().getId().equals(modifierId);
        boolean isRepublicAdmin = modifier.getCurrentRepublic() != null &&
                modifier.getCurrentRepublic().getId().equals(income.getRepublic().getId()) &&
                modifier.isRepublicAdmin();

        if (!isContributor && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to update this income");
        }

        // Calculate delta for balance update
        BigDecimal originalAmount = income.getAmount();
        BigDecimal newAmount = amount != null ? amount : originalAmount;
        BigDecimal delta = newAmount.subtract(originalAmount);

        // Update income details
        income.updateDetails(description, amount, incomeDate, source);

        // Save changes
        Income updatedIncome = incomeRepository.save(income);

        // If amount changed, update republic finances
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            republicFinancesService.updateBalance(income.getRepublic().getId(), delta);
        }

        return updatedIncome;
    }

    @Override
    @Transactional(readOnly = true)
    public Income getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByRepublicId(UUID republicId) {
        // Verify republic exists
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return incomeRepository.findByRepublicId(republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByRepublicIdAndDateRange(UUID republicId, LocalDateTime startDate, LocalDateTime endDate) {
        // Verify republic exists
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        return incomeRepository.findByRepublicIdAndDateRange(republicId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByRepublicIdAndSource(UUID republicId, String source) {
        // Verify republic exists
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return incomeRepository.findByRepublicIdAndSource(republicId, source);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByContributorId(UUID contributorId) {
        // Verify user exists
        if (!userRepository.findById(contributorId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + contributorId);
        }

        return incomeRepository.findByContributorId(contributorId);
    }

    @Override
    public void deleteIncome(Long id, UUID deleterId) {
        // Get income
        Income income = getIncomeById(id);

        // Validate user
        User deleter = userRepository.findById(deleterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + deleterId));

        // Check if user is contributor or admin
        boolean isContributor = income.getContributor() != null && income.getContributor().getId().equals(deleterId);
        boolean isRepublicAdmin = deleter.getCurrentRepublic() != null &&
                deleter.getCurrentRepublic().getId().equals(income.getRepublic().getId()) &&
                deleter.isRepublicAdmin();

        if (!isContributor && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to delete this income");
        }

        // Update republic finances (subtract the income amount)
        republicFinancesService.updateBalance(income.getRepublic().getId(), income.getAmount().negate());

        // Delete income
        incomeRepository.delete(income);
    }
}