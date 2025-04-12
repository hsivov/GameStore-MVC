package org.example.gamestoreapp.event.listener;

import org.example.gamestoreapp.event.OrderApprovedEvent;
import org.example.gamestoreapp.event.OrderRejectedEvent;
import org.example.gamestoreapp.event.PasswordResetRequestEvent;
import org.example.gamestoreapp.event.UserRegisteredEvent;
import org.example.gamestoreapp.model.dto.OrderItemDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailConfirmationListener {

    private final TokenService tokenService;
    private final EmailService emailService;

    @Value("${app.domain.name}")
    private String domain;

    public EmailConfirmationListener(TokenService tokenService, EmailService emailService) {
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        ConfirmationToken token = new ConfirmationToken(user);

        tokenService.saveConfirmationToken(token);

        // Send confirmation email
        String link = domain + "/auth/confirm?token=" + token.getToken();

        String subject = "Confirm your email";
        String htmlContent = "<h3>Thank you for registering!</h3>"
                + "<p>Please click the link below to confirm your email:</p>"
                + "<a href='" + link + "'>Confirm Email</a>"
                + "<p>If the button above doesn’t work, copy and paste the following link into your browser:</p>"
                + "<p>" + link + "</p>"
                + "<p>If you didn't request this, please ignore this email.</p>";

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    @EventListener
    @Async
    public void handlePasswordResetRequested(PasswordResetRequestEvent event) {
        User user = event.getUser();
        ConfirmationToken token = new ConfirmationToken(user);
        tokenService.saveConfirmationToken(token);

        String link = domain + "/auth/confirm/reset-password?token=" + token.getToken();

        String subject = "Reset your password";
        String htmlContent = "<p>Hello <strong>" + user.getFirstName() + "</strong>,</p>" +
                "<p>We received a request to reset the password for your <strong>" + user.getUsername() + "</strong> account. " +
                "If you made this request, please click the button below to reset your password:</p>" +
                "<a href=\"" + link + "\">Reset My Password</a>" +
                "<p>If the button above doesn’t work, copy and paste the following link into your browser:</p>" +
                "<p>" + link + "</p>" +
                "<p>This link is valid for <strong>15 minutes</strong>.</p>" +
                "<p><strong>If you did not request a password reset</strong>, no action is required. " +
                "Your account is still secure, and your password has not been changed. " +
                "If you suspect any suspicious activity, please contact our support team immediately.</p>" +
                "<p>Thank you,</p>" +
                "<p>The <strong>Game Store</strong> Support Team</p>" +
                "<div>" +
                "<p>This email is automatically generated. Please do not answer. If you need further assistance, " +
                "please contact us at <a href=\"mailto:support@yourwebsite.com\">support@yourwebsite.com</a>.</p>" +
                "</div>";

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    @EventListener
    @Async
    public void handleOrderApproved(OrderApprovedEvent event) {
        User customer = event.getUser();
        OrderResponseDTO order = event.getOrder();

        StringBuilder sb = new StringBuilder();

        for (OrderItemDTO game : order.getBoughtGames()) {
            sb.append("<li>").append(game.getTitle()).append("</li>");
        }

        String subject = "Order Confirmation";
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Order Confirmation</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0;\">\n" +
                "    <div style=\"max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd;\">\n" +
                "        <h2 style=\"color: #2d3748;\">Thank You for Your Purchase!</h2>\n" +
                "        \n" +
                "        <p>Hi <strong>" + customer.getFirstName() + "</strong>,</p>\n" +
                "        <p>Thank you for your recent purchase with <strong>Game Store</strong>! We’re excited to let you know that your order (#<strong>" + order.getId() + "</strong>) has been successfully processed.</p>\n" +
                "        \n" +
                "        <h3 style=\"color: #4a5568;\">Order Details:</h3>\n" +
                "        <ul>\n" +
                "            <li><strong>Item(s) Purchased: </strong></li>\n" +
                "       " + sb + "\n" +
                "            <li><strong>Order Total: </strong>" + order.getTotalPrice() + " лв.</li>\n" +
                "        </ul>\n" +
                "\n" +
                "        <h3 style=\"color: #4a5568;\">What’s Next?</h3>\n" +
                "        <p><strong>Need Help?</strong> If you have any questions or need further assistance, feel free to reach out to us at <a href=\"mailto:[Customer Support Email]\" style=\"color: #3182ce;\">[Customer Support Email]</a> or call us at [Phone Number].</p>\n" +
                "\n" +
                "        <p>We truly appreciate your business and hope you love your new [product]! Keep an eye out for future offers and product updates from us.</p>\n" +
                "\n" +
                "        <p>Thanks again for choosing <strong>Game Store</strong>!</p>\n" +
                "\n" +
                "        <p>Best regards,</p>\n" +
                "        <p><strong>The Game Store Team</strong></p>\n" +
                "        <p>[Company Contact Information]</p>\n" +
                "        <p><a href=\"[Company Social Media Link]\" style=\"color: #3182ce;\">Follow us on social media</a></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";

        emailService.sendEmail(customer.getEmail(), subject, htmlContent);
    }

    @EventListener
    @Async
    public void handleOrderRejected(OrderRejectedEvent event) {
        User customer = event.getUser();
        OrderResponseDTO order = event.getOrder();

        String subject = "Order Cancellation";
        String body = "Dear " + customer.getFirstName() + " " + customer.getLastName() + ",\n" +
                "\n" +
                "We regret to inform you that your recent payment attempt for Order #" + order.getId() + " on " + order.getOrderDate() +
                " was unsuccessful. This could be due to various reasons, such as insufficient funds, incorrect card details, or a bank authorization issue.\n" +
                "\n" +
                "To complete your purchase, please try one of the following:\n" +
                "✅ Verify your payment details and try again.\n" +
                "✅ Use an alternative payment method.\n" +
                "✅ Contact your bank to check for any restrictions.\n" +
                "\n" +
                "If you need assistance, our support team is here to help! Reach us at [Support Email/Phone].\n" +
                "\n" +
                "We appreciate your business and look forward to resolving this promptly.\n" +
                "\n" +
                "Best regards,\n" +
                "Game Store\n" +
                "[Company Website] | [Support Contact]";

        emailService.sendEmail(customer.getEmail(), subject, body);
    }
}
