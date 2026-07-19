package com.sharkdom.partnerattribution.service.impl;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.emailOutreach.dto.SendMailRequest;
import com.sharkdom.emailOutreach.service.MailgunServices;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.partnerattribution.dto.NotificationRequestDTO;
import com.sharkdom.partnerattribution.service.NotificationService;

import com.sharkdom.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerAttributionNotificationServiceImpl implements NotificationService {

    private final TaskScheduler taskScheduler;
    private final MailgunServices mailgunServices;

    @Override
    public void processNotification(NotificationRequestDTO request) {


        if (request.getReceiverEmail() == null || request.getReceiverEmail().isBlank()) {
            throw new SharkdomException(ErrorMessages.SH106);
        }

        if (request.isScheduled() && request.getScheduleTime() == null) {
            throw new SharkdomException(ErrorMessages.SH106);
        }

        Runnable task = () -> {

            try {

                log.info("Processing notification | receiver={}",
                        request.getReceiverEmail());

                Map<String, String> placeholders = new HashMap<>();

                if (request.getData() != null) {
                    request.getData().forEach((k, v) ->
                            placeholders.put(k, v != null ? v.toString() : ""));
                }

                SendMailRequest sendMailRequest=new SendMailRequest();
                sendMailRequest.setSubject(request.getSubject());
                sendMailRequest.setFrom("ceo@sharkdom.com");
                sendMailRequest.setTo(request.getReceiverEmail());
                mailgunServices.sendEmail(sendMailRequest);

                log.info("Notification email sent successfully to {}", request.getReceiverEmail());

            } catch (Exception e) {

                log.error("Failed to send notification email  | error={}",
                        e.getMessage(),
                        e);

                throw new SharkdomException(ErrorMessages.SH134, e.getMessage());
            }
        };

        if (request.isScheduled()) {


            taskScheduler.schedule(
                    task,
                    Timestamp.valueOf(request.getScheduleTime())
            );

        } else {

            task.run();
        }
    }
}