package com.mehmetkerem.service.impl;

import com.mehmetkerem.service.INotificationService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@Profile("!test")
public class ResendNotificationService implements INotificationService {

    private final Resend resend;
    private final String fromEmail;

    public ResendNotificationService(@Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendOrderConfirmation(String toEmail, String orderCode) {
        String subject = "Sipariş Onayı - " + orderCode;
        String content = "<h1>Siparişiniz Alındı!</h1><p>Sipariş kodunuz: <strong>" + orderCode + "</strong></p>";
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendStockAlert(String productName) {
        log.info("Stock alert for: {}", productName);
    }

    @Override
    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        String subject = "Şifre Sıfırlama İsteği";
        String content = "<h1>Şifre Sıfırlama</h1><p>Şifrenizi sıfırlama için aşağıdaki linke tıklayın:</p>" +
                "<a href=\"" + resetUrl + "\">Şifremi Sıfırla</a>";
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Hoş Geldiniz!";
        String content = "<h1>Hoş Geldin " + name + "!</h1><p>Can Antika dünyasına katıldığınız için teşekkürler.</p>";
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendOrderTrackingEmail(String toEmail, String orderCode, String trackingNumber, String carrier) {
        String subject = "Siparişiniz Yola Çıktı! - " + orderCode;
        String content = "<h1>Kargonuz Yolda!</h1><p>Siparişiniz <strong>" + carrier + "</strong> ile gönderildi.</p>" +
                "<p>Takip Numarası: <strong>" + trackingNumber + "</strong></p>";
        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        CreateEmailOptions createEmailOptions = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(createEmailOptions);
            log.info("Email sent successfully: {}", data.getId());
        } catch (ResendException e) {
            log.error("Failed to send email via Resend: {}", e.getMessage());
        }
    }
}
