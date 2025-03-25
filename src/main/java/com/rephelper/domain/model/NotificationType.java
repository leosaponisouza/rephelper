package com.rephelper.domain.model;

public enum NotificationType {
    TASK_ASSIGNED("Tarefa Atribuída"),
    TASK_COMPLETED("Tarefa Concluída"),
    TASK_DUE_SOON("Prazo de Tarefa Próximo"),
    EXPENSE_ADDED("Nova Despesa"),
    EXPENSE_PAID("Despesa Paga"),
    EVENT_CREATED("Novo Evento"),
    EVENT_INVITATION("Convite para Evento"),
    REPUBLIC_INVITATION("Convite para República"),
    REPUBLIC_JOINED("Novo Membro"),
    REPUBLIC_LEFT("Membro Saiu"),
    SYSTEM_NOTIFICATION("Notificação do Sistema");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 