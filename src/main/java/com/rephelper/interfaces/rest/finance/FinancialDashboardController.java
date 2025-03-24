package com.rephelper.interfaces.rest.finance;

import com.rephelper.application.dto.response.ExpenseResponse;
import com.rephelper.application.dto.response.IncomeResponse;
import com.rephelper.application.dto.response.RepublicFinancesResponse;
import com.rephelper.application.mapper.ExpenseDtoMapper;
import com.rephelper.application.mapper.IncomeDtoMapper;
import com.rephelper.application.mapper.RepublicFinancesDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Expense;
import com.rephelper.domain.model.Income;
import com.rephelper.domain.model.RepublicFinances;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.ExpenseServicePort;
import com.rephelper.domain.port.in.IncomeServicePort;
import com.rephelper.domain.port.in.RepublicFinancesServicePort;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/financial-dashboard")
@RequiredArgsConstructor
@Tag(name = "Financial Dashboard", description = "Financial dashboard endpoints providing summary and analytics")
public class FinancialDashboardController {

    private final ExpenseServicePort expenseService;
    private final IncomeServicePort incomeService;
    private final RepublicFinancesServicePort republicFinancesService;
    private final UserServicePort userService;

    private final ExpenseDtoMapper expenseDtoMapper;
    private final IncomeDtoMapper incomeDtoMapper;
    private final RepublicFinancesDtoMapper republicFinancesDtoMapper;

    @GetMapping("/summary/{republicId}")
    @Operation(summary = "Get financial summary", description = "Retrieves a summary of the financial situation for a republic")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(
            @PathVariable UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view financial data for your own republic");
        }

        // Get current finances
        RepublicFinances finances = republicFinancesService.getOrCreateRepublicFinances(republicId);

        // Get current month's expenses
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        List<Expense> currentMonthExpenses = expenseService.getExpensesByRepublicIdAndDateRange(
                republicId, startOfMonth, now);

        // Get pending expenses
        List<Expense> pendingExpenses = expenseService.getExpensesByRepublicIdAndStatus(
                republicId, Expense.ExpenseStatus.PENDING);

        // Get approved expenses awaiting reimbursement
        List<Expense> approvedExpenses = expenseService.getExpensesByRepublicIdAndStatus(
                republicId, Expense.ExpenseStatus.APPROVED);

        // Get current month's incomes
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime nowDateTime = now.atTime(23, 59, 59);

        List<Income> currentMonthIncomes = incomeService.getIncomesByRepublicIdAndDateRange(
                republicId, startOfMonthDateTime, nowDateTime);

        // Calculate total expenses and incomes for the month
        BigDecimal totalExpensesMonth = currentMonthExpenses.stream()
                .filter(e -> e.getStatus() == Expense.ExpenseStatus.REIMBURSED)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncomesMonth = currentMonthIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate pending and approved amounts
        BigDecimal pendingAmount = pendingExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal approvedAmount = approvedExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("currentBalance", finances.getCurrentBalance());
        response.put("lastUpdated", finances.getLastUpdated());
        response.put("totalExpensesCurrentMonth", totalExpensesMonth);
        response.put("totalIncomesCurrentMonth", totalIncomesMonth);
        response.put("pendingExpensesCount", pendingExpenses.size());
        response.put("pendingExpensesAmount", pendingAmount);
        response.put("approvedExpensesCount", approvedExpenses.size());
        response.put("approvedExpensesAmount", approvedAmount);

        // Get expenses by category
        Map<String, BigDecimal> expensesByCategory = currentMonthExpenses.stream()
                .filter(e -> e.getStatus() == Expense.ExpenseStatus.REIMBURSED)
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory() : "Other",
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        response.put("expensesByCategory", expensesByCategory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses/monthly/{republicId}")
    @Operation(summary = "Get monthly expenses", description = "Retrieves expenses grouped by month for charts")
    public ResponseEntity<Map<String, Object>> getMonthlyExpenses(
            @PathVariable UUID republicId,
            @RequestParam(required = false, defaultValue = "6") Integer numberOfMonths,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view financial data for your own republic");
        }

        // Calculate date range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(numberOfMonths, ChronoUnit.MONTHS);

        // Get expenses
        List<Expense> expenses = expenseService.getExpensesByRepublicIdAndDateRange(
                republicId, startDate, endDate);

        // Get incomes
        List<Income> incomes = incomeService.getIncomesByRepublicIdAndDateRange(
                republicId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        // Group expenses by month
        Map<YearMonth, BigDecimal> expensesByMonth = expenses.stream()
                .filter(e -> e.getStatus() == Expense.ExpenseStatus.REIMBURSED)
                .collect(Collectors.groupingBy(
                        e -> YearMonth.from(e.getExpenseDate()),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        // Group incomes by month
        Map<YearMonth, BigDecimal> incomesByMonth = incomes.stream()
                .collect(Collectors.groupingBy(
                        i -> YearMonth.from(i.getIncomeDate()),
                        Collectors.reducing(BigDecimal.ZERO, Income::getAmount, BigDecimal::add)
                ));

        // Fill in missing months
        List<YearMonth> allMonths = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!current.isAfter(end)) {
            allMonths.add(current);
            current = current.plusMonths(1);
        }

        List<Map<String, Object>> monthlyData = new ArrayList<>();

        for (YearMonth month : allMonths) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.toString());
            monthData.put("expenses", expensesByMonth.getOrDefault(month, BigDecimal.ZERO));
            monthData.put("incomes", incomesByMonth.getOrDefault(month, BigDecimal.ZERO));
            monthlyData.add(monthData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("monthlyData", monthlyData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses/by-user/{republicId}")
    @Operation(summary = "Get expenses by user", description = "Retrieves expenses grouped by user for charts")
    public ResponseEntity<Map<String, Object>> getExpensesByUser(
            @PathVariable UUID republicId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view financial data for your own republic");
        }

        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Get expenses
        List<Expense> expenses = expenseService.getExpensesByRepublicIdAndDateRange(
                republicId, startDate, endDate);

        // Group expenses by user
        Map<UUID, List<Expense>> expensesByUser = expenses.stream()
                .filter(e -> e.getCreator() != null && e.getStatus() == Expense.ExpenseStatus.REIMBURSED)
                .collect(Collectors.groupingBy(e -> e.getCreator().getId()));

        List<Map<String, Object>> userData = new ArrayList<>();

        for (Map.Entry<UUID, List<Expense>> entry : expensesByUser.entrySet()) {
            UUID userId = entry.getKey();
            List<Expense> userExpenses = entry.getValue();

            BigDecimal totalAmount = userExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Get the user's name from the first expense in the list
            String userName = userExpenses.get(0).getCreator().getName();

            Map<String, Object> userDataItem = new HashMap<>();
            userDataItem.put("userId", userId);
            userDataItem.put("userName", userName);
            userDataItem.put("totalAmount", totalAmount);
            userDataItem.put("expenseCount", userExpenses.size());

            userData.add(userDataItem);
        }

        // Sort by total amount descending
        userData.sort((a, b) -> ((BigDecimal) b.get("totalAmount")).compareTo((BigDecimal) a.get("totalAmount")));

        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("userData", userData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-actions/{republicId}")
    @Operation(summary = "Get pending financial actions", description = "Retrieves pending expenses and other actionable items")
    public ResponseEntity<Map<String, Object>> getPendingActions(
            @PathVariable UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view financial data for your own republic");
        }

        // Get pending expenses
        List<Expense> pendingExpenses = expenseService.getExpensesByRepublicIdAndStatus(
                republicId, Expense.ExpenseStatus.PENDING);

        // Convert to DTO
        List<ExpenseResponse> pendingExpensesDto = expenseDtoMapper.toExpenseResponseList(pendingExpenses);

        // Calculate totals
        BigDecimal pendingTotal = pendingExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("pendingExpenses", pendingExpensesDto);
        response.put("pendingExpensesCount", pendingExpenses.size());
        response.put("pendingExpensesTotal", pendingTotal);


        return ResponseEntity.ok(response);
    }
}