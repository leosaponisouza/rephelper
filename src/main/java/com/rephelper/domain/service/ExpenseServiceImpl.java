package com.rephelper.domain.service;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.*;
import com.rephelper.domain.port.in.ExpenseServicePort;
import com.rephelper.domain.port.in.NotificationServicePort;
import com.rephelper.domain.port.in.RepublicFinancesServicePort;
import com.rephelper.domain.port.out.ExpenseRepositoryPort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseServicePort {

    private final ExpenseRepositoryPort expenseRepository;
    private final UserRepositoryPort userRepository;
    private final RepublicRepositoryPort republicRepository;
    private final RepublicFinancesServicePort republicFinancesService;
    private final NotificationServicePort notificationService;

    @Override
    public Expense createExpense(Expense expense, UUID creatorUserId) {
        // Validar usuário
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorUserId));

        // Verificar se o usuário pertence à república
        if (creator.getCurrentRepublic() == null ||
                !creator.getCurrentRepublic().getId().equals(expense.getRepublic().getId())) {
            throw new ForbiddenException("You can only create expenses for your own republic");
        }

        // Verificar se a república existe
        Expense finalExpense = expense;
        Republic republic = republicRepository.findById(expense.getRepublic().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + finalExpense.getRepublic().getId()));

        // Configurar criador e status
        expense = Expense.builder()
                .republic(republic)
                .creator(creator)
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .receiptUrl(expense.getReceiptUrl())
                .status(Expense.ExpenseStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // Notify republic admins about the new expense
        List<User> republicMembers = userRepository.findByCurrentRepublicId(republic.getId());
        for (User member : republicMembers) {
            if (member.isRepublicAdmin() && !member.getId().equals(creatorUserId)) {
                notificationService.notifyExpenseCreated(
                        member.getId(),
                        savedExpense.getId(),
                        savedExpense.getDescription(),
                        creatorUserId
                );
            }
        }

        return savedExpense;
    }

    @Override
    public Expense updateExpense(Long id, String description, BigDecimal amount,
                                 LocalDate expenseDate, String category,
                                 String receiptUrl, UUID modifierId) {
        // Obter despesa
        Expense expense = getExpenseById(id);

        // Validar usuário
        User modifier = userRepository.findById(modifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierId));

        // Verificar se o usuário é o criador ou administrador
        boolean isCreator = expense.getCreator().getId().equals(modifierId);
        boolean isRepublicAdmin = modifier.getCurrentRepublic() != null &&
                modifier.getCurrentRepublic().getId().equals(expense.getRepublic().getId()) &&
                modifier.isRepublicAdmin();

        if (!isCreator && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to update this expense");
        }

        // Somente despesas pendentes podem ser atualizadas
        if (expense.getStatus() != Expense.ExpenseStatus.PENDING) {
            throw new ValidationException("Only pending expenses can be updated");
        }

        // Atualizar despesa
        expense.updateDetails(description, amount, expenseDate, category, receiptUrl);

        return expenseRepository.save(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByRepublicId(UUID republicId) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return expenseRepository.findByRepublicId(republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByRepublicIdAndStatus(UUID republicId, Expense.ExpenseStatus status) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return expenseRepository.findByRepublicIdAndStatus(republicId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByRepublicIdAndDateRange(UUID republicId, LocalDate startDate, LocalDate endDate) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        // Validar intervalo de datas
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        return expenseRepository.findByRepublicIdAndDateRange(republicId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByRepublicIdAndCategory(UUID republicId, String category) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return expenseRepository.findByRepublicIdAndCategory(republicId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCreatorId(UUID creatorId) {
        // Verificar se o usuário existe
        if (!userRepository.findById(creatorId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + creatorId);
        }

        return expenseRepository.findByCreatorId(creatorId);
    }

    @Override
    public Expense approveExpense(Long id, UUID approverId) {
        // Obter despesa
        Expense expense = getExpenseById(id);

        // Validar usuário
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + approverId));

        // Verificar se o usuário é administrador da república
        boolean isRepublicAdmin = approver.getCurrentRepublic() != null &&
                approver.getCurrentRepublic().getId().equals(expense.getRepublic().getId()) &&
                approver.isRepublicAdmin();

        if (!isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to approve expenses");
        }

        // Aprovar despesa
        expense.approve();

        Expense approvedExpense = expenseRepository.save(expense);

        // Notify the expense creator about the approval
        if (expense.getCreator() != null) {
            notificationService.notifyExpenseApproved(
                    expense.getCreator().getId(),
                    id,
                    expense.getDescription()
            );
        }

        return approvedExpense;
    }


    @Override
    public Expense rejectExpense(Long id, String reason, UUID rejecterId) {
        // Obter despesa
        Expense expense = getExpenseById(id);

        // Validar usuário
        User rejecter = userRepository.findById(rejecterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + rejecterId));

        // Verificar se o usuário é administrador da república
        boolean isRepublicAdmin = rejecter.getCurrentRepublic() != null &&
                rejecter.getCurrentRepublic().getId().equals(expense.getRepublic().getId()) &&
                rejecter.isRepublicAdmin();

        if (!isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to reject expenses");
        }

        // Rejeitar despesa
        expense.reject(reason);

        Expense rejectedExpense = expenseRepository.save(expense);

        // Notify the expense creator about the rejection
        if (expense.getCreator() != null) {
            notificationService.notifyExpenseRejected(
                    expense.getCreator().getId(),
                    id,
                    expense.getDescription(),
                    reason
            );
        }

        return rejectedExpense;
    }

    @Override
    public Expense reimburseExpense(Long id, UUID reimburserId) {
        // Obter despesa
        Expense expense = getExpenseById(id);

        // Validar usuário
        User reimburser = userRepository.findById(reimburserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reimburserId));

        // Verificar se o usuário é administrador da república
        boolean isRepublicAdmin = reimburser.getCurrentRepublic() != null &&
                reimburser.getCurrentRepublic().getId().equals(expense.getRepublic().getId()) &&
                reimburser.isRepublicAdmin();

        if (!isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to mark expenses as reimbursed");
        }

        // Verificar se a república tem saldo suficiente
        if (!republicFinancesService.hasEnoughBalance(expense.getRepublic().getId(), expense.getAmount())) {
            throw new ValidationException("Republic does not have enough balance to reimburse this expense");
        }

        // Marcar como reembolsada
        expense.reimburse();

        // Atualizar finanças da república
        republicFinancesService.updateBalance(expense.getRepublic().getId(), expense.getAmount().negate());

        Expense reimbursedExpense = expenseRepository.save(expense);

        // Notify the expense creator about the reimbursement
        if (expense.getCreator() != null) {
            notificationService.createNotification(
                    expense.getCreator().getId(),
                    "Expense Reimbursed",
                    "Your expense '" + expense.getDescription() + "' has been reimbursed",
                    Notification.NotificationType.EXPENSE_REIMBURSED,
                    "expense",
                    expense.getId().toString()
            );
        }

        // Notify all republic members about the significant expense
        boolean isSignificantExpense = expense.getAmount().compareTo(new BigDecimal("100")) > 0;
        if (isSignificantExpense) {
            List<User> republicMembers = userRepository.findByCurrentRepublicId(expense.getRepublic().getId());
            for (User member : republicMembers) {
                if (!member.getId().equals(reimburser.getId()) &&
                        (expense.getCreator() == null || !member.getId().equals(expense.getCreator().getId()))) {
                    notificationService.createNotification(
                            member.getId(),
                            "Significant Expense Reimbursed",
                            "An expense of " + expense.getAmount() + " for '" + expense.getDescription() + "' has been reimbursed",
                            Notification.NotificationType.EXPENSE_REIMBURSED,
                            "expense",
                            expense.getId().toString()
                    );
                }
            }
        }

        return reimbursedExpense;
    }

    @Override
    public Expense resetExpenseToPending(Long id, UUID modifierId) {
        // Obter despesa
        Expense expense = getExpenseById(id);

        // Validar usuário
        User modifier = userRepository.findById(modifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierId));

        // Verificar se o usuário é o criador ou administrador
        boolean isCreator = expense.getCreator().getId().equals(modifierId);
        boolean isRepublicAdmin = modifier.getCurrentRepublic() != null &&
                modifier.getCurrentRepublic().getId().equals(expense.getRepublic().getId()) &&
                modifier.isRepublicAdmin();

        if (!isCreator && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to reset this expense");
        }

        // Redefinir para pendente
        expense.resetToPending();

        return expenseRepository.save(expense);
    }

    @Override
    public void deleteExpense(Long id, UUID deleterId) {
        // Obter despesa
        Expense expense = getExpenseById(id);

        // Validar usuário
        User deleter = userRepository.findById(deleterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + deleterId));

        // Verificar se o usuário é o criador ou administrador
        boolean isCreator = expense.getCreator().getId().equals(deleterId);
        boolean isRepublicAdmin = deleter.getCurrentRepublic() != null &&
                deleter.getCurrentRepublic().getId().equals(expense.getRepublic().getId()) &&
                deleter.isRepublicAdmin();

        if (!isCreator && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to delete this expense");
        }

        // Somente despesas pendentes ou rejeitadas podem ser excluídas
        if (expense.getStatus() != Expense.ExpenseStatus.PENDING &&
                expense.getStatus() != Expense.ExpenseStatus.REJECTED) {
            throw new ValidationException("Only pending or rejected expenses can be deleted");
        }

        // Excluir despesa
        expenseRepository.delete(expense);
    }
}