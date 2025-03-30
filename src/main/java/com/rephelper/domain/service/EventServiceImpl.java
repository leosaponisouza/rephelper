package com.rephelper.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rephelper.domain.model.*;
import com.rephelper.domain.port.in.NotificationServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.port.in.EventServicePort;
import com.rephelper.domain.port.out.EventRepositoryPort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

import static com.rephelper.domain.model.Notification.NotificationType.EVENT_INVITATION;
import static com.rephelper.domain.model.Notification.NotificationType.EVENT_CREATED;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventServicePort {

    private final EventRepositoryPort eventRepository;
    private final UserRepositoryPort userRepository;
    private final RepublicRepositoryPort republicRepository;
    private final NotificationServicePort notificationService;

    @Override
    public Event createEvent(Event event, UUID creatorUserId) {
        // Validar usuário criador
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorUserId));

        // Verificar se o usuário pertence à república
        if (creator.getCurrentRepublic() == null ||
                !creator.getCurrentRepublic().getId().equals(event.getRepublic().getId())) {
            throw new ForbiddenException("You can only create events for your own republic");
        }

        // Verificar se a república existe
        if (!republicRepository.findById(event.getRepublic().getId()).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + event.getRepublic().getId());
        }

        // Validar datas
        validateEventDates(event.getStartDate(), event.getEndDate());

        // Definir o criador do evento
        event = Event.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .republic(event.getRepublic())
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .build();

        // Salvar o evento para obter um ID
        Event savedEvent = eventRepository.save(event);
        
        // Adicionar o criador como participante confirmado automaticamente
        eventRepository.inviteUserToEvent(savedEvent.getId(), creatorUserId, Event.InvitationStatus.CONFIRMED);
        
        // Notificar outros membros da república sobre o novo evento
        notifyRepublicMembersAboutNewEvent(savedEvent, creator);
        
        // Retornar o evento atualizado com o convite do criador
        return eventRepository.findById(savedEvent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found after creation"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEventsByRepublicId(UUID republicId) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return eventRepository.findByRepublicId(republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getUpcomingEventsByRepublicId(UUID republicId) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return eventRepository.findUpcomingByRepublicId(republicId);
    }

    @Override
    public Event updateEvent(Long id, String title, String description,
                             LocalDateTime startDate, LocalDateTime endDate,
                             String location, UUID modifierUserId) {

        Event event = getEventById(id);

        // Validar usuário
        User user = userRepository.findById(modifierUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierUserId));

        // Verificar se o usuário é o criador do evento ou um administrador da república
        boolean isCreator = event.getCreator().getId().equals(modifierUserId);
        boolean isRepublicAdmin = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(event.getRepublic().getId()) &&
                user.isRepublicAdmin();

        if (!isCreator && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to update this event");
        }

        // Validar datas se estiverem sendo atualizadas
        if (startDate != null || endDate != null) {
            LocalDateTime newStartDate = startDate != null ? startDate : event.getStartDate();
            LocalDateTime newEndDate = endDate != null ? endDate : event.getEndDate();
            validateEventDates(newStartDate, newEndDate);
        }

        // Atualizar evento
        event.updateDetails(title, description, startDate, endDate, location);

        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id, UUID userId) {
        Event event = getEventById(id);

        // Validar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verificar se o usuário é o criador do evento ou um administrador da república
        boolean isCreator = event.getCreator().getId().equals(userId);
        boolean isRepublicAdmin = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(event.getRepublic().getId()) &&
                user.isRepublicAdmin();

        if (!isCreator && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to delete this event");
        }

        // Deletar evento
        eventRepository.delete(event);
    }

    @Override
    public Event inviteUsers(Long eventId, List<UUID> userIds, UUID inviterUserId) {
        Event event = getEventById(eventId);

        // Validar usuário que está convidando
        User inviter = userRepository.findById(inviterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + inviterUserId));

        // Verificar se o usuário é o criador do evento ou um administrador da república
        boolean isCreator = event.getCreator().getId().equals(inviterUserId);
        boolean isRepublicAdmin = inviter.getCurrentRepublic() != null &&
                inviter.getCurrentRepublic().getId().equals(event.getRepublic().getId()) &&
                inviter.isRepublicAdmin();

        if (!isCreator && !isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to invite users to this event");
        }

        // Para cada usuário a ser convidado
        for (UUID userId : userIds) {
            // Verificar se o usuário já foi convidado
            if (event.isUserInvited(userId)) {
                continue;
            }

            // Buscar usuário
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            // Verificar se o usuário pertence à mesma república do evento
            if (user.getCurrentRepublic() == null ||
                    !user.getCurrentRepublic().getId().equals(event.getRepublic().getId())) {
                throw new ValidationException("User with id " + userId + " is not a member of the event's republic");
            }

            // Use the new repository method that handles the entity relationships
            eventRepository.inviteUserToEvent(eventId, userId, Event.InvitationStatus.INVITED);
            
            // Enviar notificação de convite
            notificationService.createNotification(
                userId,
                "Convite para evento: " + event.getTitle(),
                inviter.getName() + " convidou você para o evento: " + event.getTitle(),
                EVENT_INVITATION,
                "event",
                eventId.toString()
            );
        }

        // Salvar evento com novos convites
        return eventRepository.save(event);
    }

    @Override
    public Event updateInvitationStatus(Long eventId, UUID userId, Event.InvitationStatus status) {
        Event event = getEventById(eventId);

        // Verificar se o evento ocorreu no passado
        if (event.hasFinished()) {
            throw new ValidationException("Cannot update invitation status for a past event");
        }

        // Verificar se o usuário foi convidado
        if (!event.isUserInvited(userId)) {
            throw new ValidationException("User is not invited to this event");
        }

        // Encontrar o convite
        EventInvitation invitation = event.getInvitations().stream()
                .filter(i -> i.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        // Atualizar status
        switch (status) {
            case CONFIRMED:
                invitation.confirm();
                break;
            case DECLINED:
                invitation.decline();
                break;
            default:
                throw new ValidationException("Invalid invitation status: " + status);
        }

        // Salvar evento com status de convite atualizado
        eventRepository.save(event);
        
        // Se o usuário confirmou presença, notificar o criador do evento
        if (status == Event.InvitationStatus.CONFIRMED) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    
            notificationService.createNotification(
                event.getCreator().getId(),
                "Confirmação de presença: " + event.getTitle(),
                user.getName() + " confirmou presença no evento: " + event.getTitle(),
                EVENT_INVITATION,
                "event",
                eventId.toString()
            );
        }

        return event;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByInvitedUser(UUID userId) {
        // Validar usuário
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return eventRepository.findByInvitedUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByConfirmedUser(UUID userId) {
        // Validar usuário
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return eventRepository.findByConfirmedUserId(userId);
    }

    /**
     * Valida as datas de início e fim do evento
     */
    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new ValidationException("Start date cannot be null");
        }

        if (endDate == null) {
            throw new ValidationException("End date cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start date cannot be in the past");
        }
    }

    /**
     * Notifica os membros da república sobre um novo evento
     */
    private void notifyRepublicMembersAboutNewEvent(Event event, User creator) {
        if (event.getRepublic() != null && event.getRepublic().getMembers() != null) {
            event.getRepublic().getMembers().stream()
                    .filter(member -> !member.getId().equals(creator.getId())) // Excluir o criador
                    .forEach(member -> {
                        notificationService.createNotification(
                            member.getId(),
                            "Novo evento na república: " + event.getTitle(),
                            creator.getName() + " criou um novo evento: " + event.getTitle(),
                            Notification.NotificationType.EVENT_CREATED,
                            "event",
                            event.getId().toString()
                        );
                    });
        }
    }
}