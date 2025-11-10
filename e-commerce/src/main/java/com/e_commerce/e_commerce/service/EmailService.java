package com.e_commerce.e_commerce.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class EmailService {
    @Value("${sendgrid.api.key}")
    String sendGridApiKey;

    public void sendEmail(String to, String subject, String content) throws Exception {
        Email from = new Email("lhtvinh2005@gmail.com");
        Email toEmail = new Email(to);
        Content emailContent = new Content("text/plain", content);
        Mail mail = new Mail(from, subject, toEmail, emailContent);

        // configure SendGrid client with ur API key
        SendGrid sg = new SendGrid(sendGridApiKey);

        // create request
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        // send email
        Response response = sg.api(request);
        log.info(String.valueOf(response.getStatusCode()));
        log.info(response.getBody());
        log.info(response.getHeaders().toString());
    }
}
