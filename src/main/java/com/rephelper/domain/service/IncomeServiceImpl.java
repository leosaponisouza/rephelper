package com.rephelper.domain.service;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Income;
import com.rephelper.domain.model.Notification;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.IncomeServicePort;
import com.rephelper.domain.port.in.NotificationServicePort;
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
    private final NotificationServicePort notificationService;

    @Override
    public Income createIncome(Income income, UUID contributorId) {
        // Validar usuário
        User contributor = userRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + contributorId));

        // Verificar se o usuário pertence à república
        if (contributor.getCurrentRepublic() == null ||
                !contributor.getCurrentRepublic().getId().equals(income.getRepublic().getId())) {
            throw new ForbiddenException("You can only create incomes for your own republic");
        }

        // Verificar se a república existe
        Income finalIncome = income;
        Republic republic = republicRepository.findById(income.getRepublic().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + finalIncome.getRepublic().getId()));

        // Validar se o valor é positivo
        if (income.getAmount() == null || income.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Income amount must be positive");
        }

        // Configurar contribuidor e timestamp
        income = Income.builder()
                .republic(republic)
                .contributor(contributor)
                .description(income.getDescription())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate() != null ? income.getIncomeDate() : LocalDateTime.now())
                .source(income.getSource())
                .createdAt(LocalDateTime.now())
                .build();

        // Salvar receita
        Income savedIncome = incomeRepository.save(income);

        // Atualizar finanças da república
        republicFinancesService.updateBalance(republic.getId(), income.getAmount());

        // Criar notificação para o contribuidor
        notificationService.createNotification(
                contributorId,
                "Receita Registrada",
                "Você registrou com sucesso uma receita de R$ " + income.getAmount() + " da fonte: " + income.getSource(),
                Notification.NotificationType.INCOME_CREATED,
                "income",
                savedIncome.getId().toString()
        );

        // Notificar administradores da república sobre nova receita (especialmente para valores significativos)
        boolean isSignificantAmount = income.getAmount().compareTo(new BigDecimal("100")) > 0;
        if (isSignificantAmount) {
            List<User> republicMembers = userRepository.findByCurrentRepublicId(republic.getId());
            for (User member : republicMembers) {
                if (member.isRepublicAdmin() && !member.getId().equals(contributorId)) {
                    notificationService.createNotification(
                            member.getId(),
                            "Nova Receita Registrada",
                            contributor.getNickname() != null ? contributor.getNickname() : contributor.getName()
                                    + " registrou uma nova receita de R$ " + income.getAmount() + " da fonte: " + income.getSource(),
                            Notification.NotificationType.INCOME_CREATED,
                            "income",
                            savedIncome.getId().toString()
                    );
                }
            }
        }

        return savedIncome;
    }
    @Override
    public Income updateIncome(Long id, String description, BigDecimal amount,
                               LocalDateTime incomeDate, String source, UUID modifierId) {
        // Obter receita
        Income income = getIncomeById(id);

        // Validar usuário
        User modifier = userRepository.findById(modifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierId));

        // Verificar se o usuário é o contribuidor ou administrador
        boolean isContributor = income.getContributor() != null && income.getContributor().getId().equals(modifierId);
        boolean isRepublicAdmin = modifier.getCurrentRepublic() != null &&
                modifier.getCurrentRepublic().getId().equals(income.getRepublic().getId()) &&
                modifier.isRepublicAdmin();

        if (!isContributor && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to update this income");
        }

        // Calcular delta para atualização do saldo
        BigDecimal originalAmount = income.getAmount();
        BigDecimal newAmount = amount != null ? amount : originalAmount;
        BigDecimal delta = newAmount.subtract(originalAmount);

        // Atualizar detalhes da receita
        income.updateDetails(description, amount, incomeDate, source);

        // Salvar mudanças
        Income updatedIncome = incomeRepository.save(income);

        // Se o valor mudou, atualizar finanças da república
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            republicFinancesService.updateBalance(income.getRepublic().getId(), delta);

            // Notificar o contribuidor sobre a atualização da receita (se não for ele quem está atualizando)
            if (income.getContributor() != null && !income.getContributor().getId().equals(modifierId)) {
                notificationService.createNotification(
                        income.getContributor().getId(),
                        "Receita Atualizada",
                        "Seu registro de receita para '" + income.getDescription() + "' foi atualizado",
                        Notification.NotificationType.INCOME_CREATED, // Reutilizando este tipo
                        "income",
                        income.getId().toString()
                );
            }

            // Notificar administradores sobre alterações significativas de valor
            if (delta.abs().compareTo(new BigDecimal("50")) > 0) {
                List<User> republicMembers = userRepository.findByCurrentRepublicId(income.getRepublic().getId());
                for (User member : republicMembers) {
                    if (member.isRepublicAdmin() && !member.getId().equals(modifierId) &&
                            (income.getContributor() == null || !member.getId().equals(income.getContributor().getId()))) {
                        notificationService.createNotification(
                                member.getId(),
                                "Valor de Receita Modificado",
                                "O valor da receita '" + income.getDescription() + "' foi modificado de R$ " +
                                        originalAmount + " para R$ " + newAmount,
                                Notification.NotificationType.INCOME_CREATED, // Reutilizando este tipo
                                "income",
                                income.getId().toString()
                        );
                    }
                }
            }
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
        if (republicRepository.findById(republicId).isEmpty()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return incomeRepository.findByRepublicId(republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByRepublicIdAndDateRange(UUID republicId, LocalDateTime startDate, LocalDateTime endDate) {
        // Verify republic exists
        if (republicRepository.findById(republicId).isEmpty()) {
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
        if (republicRepository.findById(republicId).isEmpty()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return incomeRepository.findByRepublicIdAndSource(republicId, source);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByContributorId(UUID contributorId) {
        // Verify user exists
        if (userRepository.findById(contributorId).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + contributorId);
        }

        return incomeRepository.findByContributorId(contributorId);
    }

    @Override
    public void deleteIncome(Long id, UUID deleterId) {
        // Obter receita
        Income income = getIncomeById(id);

        // Validar usuário
        User deleter = userRepository.findById(deleterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + deleterId));

        // Verificar se o usuário é o contribuidor ou administrador
        boolean isContributor = income.getContributor() != null && income.getContributor().getId().equals(deleterId);
        boolean isRepublicAdmin = deleter.getCurrentRepublic() != null &&
                deleter.getCurrentRepublic().getId().equals(income.getRepublic().getId()) &&
                deleter.isRepublicAdmin();

        if (!isContributor && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to delete this income");
        }

        // Atualizar finanças da república (subtrair o valor da receita)
        republicFinancesService.updateBalance(income.getRepublic().getId(), income.getAmount().negate());

        // Notificar administradores e contribuidor sobre a exclusão da receita
        String notificationTitle = "Receita Excluída";
        String notificationMessage = "Uma receita de R$ " + income.getAmount() + " referente a '" + income.getDescription() +
                "' foi excluída por " + (deleter.getNickname() != null ? deleter.getNickname() : deleter.getName());

        // Notificar o contribuidor se ele não excluiu a receita ele mesmo
        if (income.getContributor() != null && !income.getContributor().getId().equals(deleterId)) {
            notificationService.createNotification(
                    income.getContributor().getId(),
                    notificationTitle,
                    notificationMessage,
                    Notification.NotificationType.INCOME_CREATED, // Reutilizando este tipo
                    "income",
                    income.getId().toString()
            );
        }

        // Notificar administradores para valores significativos
        if (income.getAmount().compareTo(new BigDecimal("100")) > 0) {
            List<User> republicMembers = userRepository.findByCurrentRepublicId(income.getRepublic().getId());
            for (User member : republicMembers) {
                if (member.isRepublicAdmin() && !member.getId().equals(deleterId) &&
                        (income.getContributor() == null || !member.getId().equals(income.getContributor().getId()))) {
                    notificationService.createNotification(
                            member.getId(),
                            notificationTitle,
                            notificationMessage,
                            Notification.NotificationType.INCOME_CREATED, // Reutilizando este tipo
                            "income",
                            income.getId().toString()
                    );
                }
            }
        }

        // Excluir receita
        incomeRepository.delete(income);
    }

}