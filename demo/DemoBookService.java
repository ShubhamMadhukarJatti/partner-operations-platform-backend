package com.sharkdom.service.demo;

import com.sharkdom.entity.demo.DemoBook;
import com.sharkdom.model.email.EmailReqModel;
import com.sharkdom.model.email.EmailReqModelWithMultipartAttachments;
import com.sharkdom.repository.demo.DemoBookRepository;
import com.sharkdom.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class DemoBookService {

    private final DemoBookRepository demoBookRepository;
    private final EmailService emailService;


    public DemoBookService(DemoBookRepository demoBookRepository, EmailService emailService) {
        this.demoBookRepository = demoBookRepository;
        this.emailService = emailService;
    }

    @Transactional
    public DemoBook create(DemoBook demoBook) {

        log.info("create DemoBook.");

        if (Objects.equals(demoBook.getPurpose(), "KPI-PLAYBOOK")) {
            sendEmail(demoBook);
        }

        if (Objects.equals(demoBook.getPurpose(), "DEMO_BOOK")) {
            sendEmailToBookingPerson(demoBook);
        }

        try {

            emailService.sendDemoBookDetailsEmail(
                    "demo_book_details_admin",
                    demoBook,
                    "akshunya.v@getsharkdom.com"
            );

        } catch (Exception e) {
            log.error("Failed to send admin demo details email", e);
        }

        return demoBookRepository.save(demoBook);
    }

    @Transactional
    public List<DemoBook> findAllFromTo(String from, String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        return demoBookRepository.findAllFromTo(fromDate, toDate);

    }

    public void sendEmail(DemoBook demoBook) {
        log.info("sendEmail");
        EmailReqModel emailReqModel = new EmailReqModel();
        emailReqModel.setRecipients(Collections.singletonList(demoBook.getBusinessEmail()));
        emailReqModel.setSubject("Mail with s3 attachment.");
        emailReqModel.setS3AttachmentNames(Collections.singletonList("Sharkdom_Sales_playbook.pdf"));
        emailReqModel.setBodyText("Please find the below attachment");
        emailReqModel.setBodyHtml("kpi-playbook");
        new EmailReqModelWithMultipartAttachments(emailReqModel);
    }

    public void sendEmailToBookingPerson(DemoBook demoBook) {
        log.info("sendEmailToBookingPerson");
        emailService.sendByEmailBookDemo("book_demo",
                demoBook.getBusinessEmail(),
                "meetingLink",
                "start-date",
                "start-time",
                "end-date",
                "end-time");

    }


}

