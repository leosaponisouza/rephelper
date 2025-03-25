package com.rephelper.domain.service;

import com.rephelper.domain.model.Task;
import com.rephelper.domain.model.NotificationType;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.NotificationServicePort;
import com.rephelper.domain.port.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço agendado para processamento de recorrência de tarefas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskRecurrenceScheduler {
    
    private final TaskRepositoryPort taskRepository;
    private final NotificationServicePort notificationService;
    
    /**
     * Processa tarefas recorrentes vencidas que precisam de novas instâncias
     * Executa diariamente à meia-noite
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processOverdueRecurringTasks() {
        log.info("Iniciando processamento de tarefas recorrentes vencidas");
        
        List<Task> tasks = taskRepository.findOverdueRecurringTasks();
        int created = 0;
        
        log.info("Encontradas {} tarefas recorrentes vencidas para processar", tasks.size());
        
        for (Task task : tasks) {
            if (task.shouldContinueRecurrence()) {
                log.debug("Processando tarefa recorrente vencida: {} (ID: {})", task.getTitle(), task.getId());
                
                // Marca a tarefa atual como OVERDUE, se ainda não estiver
                if (task.getStatus() != Task.TaskStatus.OVERDUE) {
                    task.updateStatus();
                    taskRepository.save(task);
                }
                
                // Verifica se já existe uma tarefa filha
                if (!taskRepository.existsByParentTaskId(task.getId())) {
                    // Cria a próxima instância da tarefa recorrente
                    Task nextTask = task.createRecurringInstance();
                    if (nextTask != null) {
                        Task savedTask = taskRepository.save(nextTask);
                        log.debug("Nova instância de tarefa recorrente criada: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
                        
                        // Notifica os usuários atribuídos sobre a nova tarefa recorrente
                        notifyAssignedUsers(savedTask);
                        created++;
                    }
                } else {
                    log.debug("Tarefa {} já possui uma instância filha, pulando criação", task.getId());
                }
            }
        }
        
        log.info("Processamento de tarefas recorrentes vencidas concluído. {} novas instâncias criadas", created);
    }
    
    /**
     * Processa tarefas recorrentes concluídas que ainda não geraram a próxima instância
     * Executa diariamente às 1h da manhã
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processCompletedRecurringTasks() {
        log.info("Iniciando processamento de tarefas recorrentes concluídas");
        
        List<Task> tasks = taskRepository.findCompletedRecurringTasks();
        int created = 0;
        
        log.info("Encontradas {} tarefas recorrentes concluídas para processar", tasks.size());
        
        for (Task task : tasks) {
            if (task.shouldContinueRecurrence()) {
                // Verifica se já existe uma tarefa filha
                if (!taskRepository.existsByParentTaskId(task.getId())) {
                    log.debug("Processando tarefa recorrente concluída sem instância filha: {} (ID: {})", task.getTitle(), task.getId());
                    
                    // Cria a próxima instância da tarefa recorrente
                    Task nextTask = task.createRecurringInstance();
                    if (nextTask != null) {
                        Task savedTask = taskRepository.save(nextTask);
                        log.debug("Nova instância de tarefa recorrente criada: {} (ID: {})", savedTask.getTitle(), savedTask.getId());
                        
                        // Notifica os usuários atribuídos sobre a nova tarefa recorrente
                        notifyAssignedUsers(savedTask);
                        created++;
                    }
                } else {
                    log.debug("Tarefa {} já possui uma instância filha, pulando criação", task.getId());
                }
            }
        }
        
        log.info("Processamento de tarefas recorrentes concluídas finalizado. {} novas instâncias criadas", created);
    }
    
    /**
     * Notifica os usuários atribuídos sobre uma nova tarefa
     */
    private void notifyAssignedUsers(Task task) {
        for (User user : task.getAssignedUsers()) {
            notificationService.notifyTaskAssigned(user.getId(), task.getId(), task.getTitle());
        }
    }
} 