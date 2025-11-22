package com.museum.service;

import com.museum.model.Event;
import com.museum.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${app.notifications.from:no-reply@museum.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendTicketPurchaseConfirmation(Ticket ticket) {
        String to = ticket.getBuyerEmail();
        if (to == null || to.isBlank()) {
            return;
        }
        Event event = ticket.getEvent();
        String title = event != null && event.getTitle() != null ? event.getTitle() : "Мероприятие";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject("Заявка на мероприятие \"" + title + "\"");

        StringBuilder body = new StringBuilder();
        body.append("Вы оформили заявку на мероприятие \"").append(title).append("\".\n\n");
        if (event != null) {
            if (event.getStartDate() != null || event.getEndDate() != null) {
                body.append("Дата: ");
                if (event.getStartDate() != null) {
                    body.append(event.getStartDate());
                }
                if (event.getEndDate() != null && !event.getEndDate().equals(event.getStartDate())) {
                    body.append(" — ").append(event.getEndDate());
                }
                body.append("\n");
            }
            if (event.getHall() != null) {
                body.append("Зал: ").append(event.getHall()).append("\n");
            }
        }
        body.append("\nСтатус: заявка принята.\n");
        body.append("Номер заявки: ").append(ticket.getId()).append("\n\n");
        body.append("Спасибо за интерес к музею!");

        msg.setText(body.toString());

        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            log.warn("Failed to send ticket confirmation e-mail for ticket {}: {}", ticket.getId(), ex.getMessage());
        }
    }
}

