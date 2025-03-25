package com.rephelper.domain.service;

import com.rephelper.domain.model.Notification;
import com.rephelper.domain.model.NotificationType;
import com.rephelper.domain.model.Task;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.NotificationServicePort;
import com.rephelper.domain.port.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por agendar notificações automáticas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    
    private final TaskRepositoryPort taskRepository;
    private final NotificationServicePort notificationService;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Verifica e notifica sobre tarefas com prazo próximo
     * Executa todos os dias às 8h da manhã
     */
    @Scheduled(cron = "0 0 8 * * *")  
    public void sendTaskDueSoonNotifications() {
        log.info("Iniciando verificação de tarefas com prazo próximo");
        
        // Buscar tarefas pendentes ou em progresso com vencimento nas próximas 24h
        List<Task> dueSoonTasks = taskRepository.findTasksDueWithinNextDay();
        
        log.info("Encontradas {} tarefas com prazo em até 24h", dueSoonTasks.size());
        
        for (Task task : dueSoonTasks) {
            // Notificar usuários atribuídos à tarefa
            for (User user : task.getAssignedUsers()) {
                String formattedDate = task.getDueDate().format(DATE_FORMAT);
                String message = "A tarefa '" + task.getTitle() + "' tem prazo de entrega em " + formattedDate + " (menos de 24h)";
                
                notificationService.notifyTaskDueSoon(
                    user.getId(),
                    task.getId(),
                    task.getTitle(),
                    message
                );
                
                log.debug("Notificação enviada para usuário {} sobre tarefa {}", user.getId(), task.getId());
            }
        }
        
        log.info("Verificação de tarefas com prazo próximo concluída");
    }
    
    /**
     * Verifica e notifica sobre tarefas com prazo em 3 dias
     * Executa todos os dias às 8h da manhã
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendTaskDueInThreeDaysNotifications() {
        log.info("Iniciando verificação de tarefas com prazo em 3 dias");
        
        // Buscar tarefas pendentes ou em progresso com vencimento em 3 dias
        List<Task> dueInThreeDaysTasks = taskRepository.findTasksDueInThreeDays();
        
        log.info("Encontradas {} tarefas com prazo em 3 dias", dueInThreeDaysTasks.size());
        
        for (Task task : dueInThreeDaysTasks) {
            // Notificar usuários atribuídos à tarefa
            for (User user : task.getAssignedUsers()) {
                String formattedDate = task.getDueDate().format(DATE_FORMAT);
                String message = "A tarefa '" + task.getTitle() + "' tem prazo de entrega em " + formattedDate + " (3 dias)";
                
                notificationService.notifyTaskDueSoon(
                    user.getId(),
                    task.getId(),
                    task.getTitle(),
                    message
                );
                
                log.debug("Notificação enviada para usuário {} sobre tarefa {}", user.getId(), task.getId());
            }
        }
        
        log.info("Verificação de tarefas com prazo em 3 dias concluída");
    }
    
    /**
     * Verifica e notifica sobre tarefas atrasadas há mais de 1 dia
     * Executa todos os dias às 9h da manhã
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendOverdueTaskNotifications() {
        log.info("Iniciando verificação de tarefas atrasadas");
        
        // Buscar tarefas pendentes ou em progresso com prazo já expirado há mais de 1 dia
        List<Task> overdueTasks = taskRepository.findTasksOverdueMoreThanOneDay();
        
        log.info("Encontradas {} tarefas atrasadas há mais de 1 dia", overdueTasks.size());
        
        for (Task task : overdueTasks) {
            // Notificar usuários atribuídos à tarefa
            for (User user : task.getAssignedUsers()) {
                String formattedDate = task.getDueDate().format(DATE_FORMAT);
                String message = "A tarefa '" + task.getTitle() + "' está atrasada! O prazo era " + formattedDate;
                
                notificationService.notifyTaskOverdue(
                    user.getId(),
                    task.getId(),
                    task.getTitle(),
                    message
                );
                
                log.debug("Notificação enviada para usuário {} sobre tarefa atrasada {}", user.getId(), task.getId());
            }
        }
        
        log.info("Verificação de tarefas atrasadas concluída");
    }
} 