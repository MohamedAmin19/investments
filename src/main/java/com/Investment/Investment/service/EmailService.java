package com.Investment.Investment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "register@egxmoneymadesimplebyccg.com";
    private static final String EMAIL_SUBJECT = "Registration Confirmation - Money Made Simple";

    private static final String EMAIL_BODY = """
        Thank you for your interest in Money Made Simple, organized by  the Egyptian Exchange (EGX) and Cairo Capital Group.

        We have received your registration. Due to high demand, attendance is subject to a waiting list. Please keep an eye on your email and WhatsApp for confirmation and further details.

        Best regards,
        EGX & Cairo Capital Group

        شكرًا لاهتمامكم بفعالية "Money Made Simple"، التي تنظمها البورصة المصرية (EGX) ومجموعة كايرو كابيتال.

        نود إعلامكم بأنه تم استلام تسجيلكم. ونظرًا للإقبال الكبير، فإن الحضور يخضع لقائمة انتظار. يُرجى متابعة بريدكم الإلكتروني وتطبيق واتساب للحصول على تأكيد الحضور وكافة التفاصيل لاحقًا.

        مع خالص التحية،
        فريق البورصة المصرية (EGX) ومجموعة كايرو كابيتا
        """;

    public void sendRegistrationEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(toEmail);
            message.setSubject(EMAIL_SUBJECT);
            message.setText(EMAIL_BODY);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't throw - we don't want email failures to break investment creation
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

