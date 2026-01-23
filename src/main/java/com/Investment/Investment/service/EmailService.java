package com.Investment.Investment.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendReservationEmail(String to, String name) throws IOException{
        Email fromEmail = new Email("register@egxmoneymadesimplebyccg.com");
        Email toEmail = new Email(to);
        String subject = "Registration Confirmation - Money Made Simple";
        String body = """
                Thank you for your interest in Money Made Simple, organized by  the Egyptian Exchange (EGX) and Cairo Capital Group.

                We have received your registration. Due to high demand, attendance is subject to a waiting list. Please keep an eye on your email and WhatsApp for confirmation and further details.

                Best regards,
                EGX & Cairo Capital Group Team

                شكرًا لاهتمامكم بفعالية “Money Made Simple”، التي تنظمها البورصة المصرية (EGX) ومجموعة كايرو كابيتال.

                نود إعلامكم بأنه تم استلام تسجيلكم. ونظرًا للإقبال الكبير، فإن الحضور يخضع لقائمة انتظار. يُرجى متابعة بريدكم الإلكتروني وتطبيق واتساب للحصول على تأكيد الحضور وكافة التفاصيل لاحقًا.

                مع خالص التحية،
                فريق البورصة المصرية (EGX) ومجموعة كايرو كابيتال
                """;


        Content content = new Content("text/plain", body);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        // SendGrid client uses the API key from the environment variable
        String apiKey = System.getenv("SENDGRID_API_KEY");
        System.out.println("SENDGRID_API_KEY exists? " + (apiKey != null));

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY is NOT set in environment variables");
        }

        SendGrid sg = new SendGrid(apiKey);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        System.out.println("SendGrid status code: " + response.getStatusCode());
        System.out.println("SendGrid response body: " + response.getBody());
        System.out.println("SendGrid response headers: " + response.getHeaders());

        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid failed with status " + response.getStatusCode());
        }
    }
}

