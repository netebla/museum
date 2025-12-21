package com.museum.service;

import com.museum.dto.TicketEmailModel;
import com.museum.model.Event;
import com.museum.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromAddress;
    private final String publicUrl;
    
    // Метрики времени отправки email (в миллисекундах)
    private final AtomicLong totalEmailSendTime = new AtomicLong(0);
    private final AtomicInteger emailSendCount = new AtomicInteger(0);

    public NotificationService(JavaMailSender mailSender,
                               TemplateEngine templateEngine,
                               @Value("${app.notifications.from:no-reply@kononovmuseum.ru}") String fromAddress,
                               @Value("${app.public-url:https://kononovmuseum.ru}") String publicUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
        this.publicUrl = publicUrl;
    }

    public void sendTicketPurchaseConfirmation(Ticket ticket) {
        String to = ticket.getBuyerEmail();
        if (to == null || to.isBlank()) {
            return;
        }
        Event event = ticket.getEvent();
        if (event == null) {
            log.warn("Cannot send email for ticket {}: event is null", ticket.getId());
            return;
        }

        String title = event.getTitle() != null ? event.getTitle() : "Мероприятие";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Заявка принята: " + title);

            // Формируем модель для шаблона
            TicketEmailModel model = buildEmailModel(ticket, event);

            // Рендерим HTML-шаблон
            Context context = new Context();
            context.setVariable("eventTitle", model.getEventTitle());
            context.setVariable("dateRange", model.getDateRange());
            context.setVariable("hall", model.getHall());
            context.setVariable("statusText", model.getStatusText());
            context.setVariable("ticketId", model.getTicketId());
            context.setVariable("eventUrl", model.getEventUrl());
            context.setVariable("imageUrl", model.getImageUrl());
            context.setVariable("year", model.getYear());

            String htmlContent = templateEngine.process("mail/ticket-confirmation", context);
            helper.setText(htmlContent, true);

            // TODO: Вариант B - inline image через helper.addInline("eventImg", Resource) и <img src="cid:eventImg">
            // Если imageUrl указывает на локальный файл, можно использовать:
            // Resource imageResource = new FileSystemResource(imagePath);
            // helper.addInline("eventImg", imageResource);
            // В шаблоне: <img src="cid:eventImg">

            // Измеряем время отправки
            long startTime = System.currentTimeMillis();
            mailSender.send(mimeMessage);
            long sendTime = System.currentTimeMillis() - startTime;
            
            // Обновляем метрики
            totalEmailSendTime.addAndGet(sendTime);
            emailSendCount.incrementAndGet();
            
            log.debug("Email sent in {} ms for ticket {}", sendTime, ticket.getId());
        } catch (MessagingException | MailException ex) {
            log.warn("Failed to send ticket confirmation e-mail for ticket {}: {}", ticket.getId(), ex.getMessage());
        }
    }
    
    /**
     * Получить среднее время отправки email в миллисекундах
     */
    public double getAverageEmailSendTime() {
        int count = emailSendCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) totalEmailSendTime.get() / count;
    }
    
    /**
     * Получить количество отправленных email
     */
    public int getEmailSendCount() {
        return emailSendCount.get();
    }

    private TicketEmailModel buildEmailModel(Ticket ticket, Event event) {
        String eventTitle = event.getTitle() != null ? event.getTitle() : "Мероприятие";
        
        // Форматируем даты
        String dateRange = formatDateRange(event.getStartDate(), event.getEndDate());
        
        String hall = event.getHall();
        
        String statusText = "Заявка принята";
        
        Long ticketId = ticket.getId();
        
        // Формируем URL мероприятия
        String eventUrl = publicUrl + "/event.html?id=" + event.getId();
        
        // Формируем абсолютный URL изображения
        String imageUrl = null;
        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            String imagePath = event.getImageUrl();
            // Если путь уже абсолютный, используем как есть, иначе добавляем publicUrl
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                imageUrl = imagePath;
            } else {
                // Убираем ведущий слэш, если есть, и добавляем publicUrl
                String cleanPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                imageUrl = publicUrl + "/" + cleanPath;
            }
        }
        
        int year = Year.now().getValue();
        
        return new TicketEmailModel(eventTitle, dateRange, hall, statusText, ticketId, eventUrl, imageUrl, year);
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "Дата будет уточнена";
        }
        if (startDate == null) {
            return endDate.format(DATE_FORMATTER);
        }
        if (endDate == null || endDate.equals(startDate)) {
            return startDate.format(DATE_FORMATTER);
        }
        return startDate.format(DATE_FORMATTER) + " — " + endDate.format(DATE_FORMATTER);
    }
}

