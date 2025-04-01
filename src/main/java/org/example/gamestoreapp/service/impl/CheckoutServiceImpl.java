package org.example.gamestoreapp.service.impl;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Notification;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.NotificationRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.CheckoutService;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.OrderService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OrderService orderService;
    private final NotificationRepository notificationRepository;

    public CheckoutServiceImpl(UserHelperService userHelperService,
                               ShoppingCartRepository shoppingCartRepository,
                               UserRepository userRepository,
                               EmailService emailService,
                               OrderService orderService, NotificationRepository notificationRepository) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.orderService = orderService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void payment(String paymentMethod) throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(currentUser);

        if (cart.isPresent()) {
            ShoppingCart shoppingCart = cart.get();

            Map<String, BigDecimal> orderItems = shoppingCart.getGames().stream()
                    .collect(Collectors.toMap(Game::getTitle, Game::getPrice));

            // Prepare request to OrderService
            CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO();
            createOrderRequest.setCustomerId(currentUser.getId());
            createOrderRequest.setOrderItems(orderItems);
            createOrderRequest.setTotalPrice(shoppingCart.getTotalPrice());
            createOrderRequest.setOrderDate(LocalDateTime.now());
            createOrderRequest.setPaymentMethod(paymentMethod);

            // Send request to OrderService
            OrderResponseDTO orderResponse = orderService.sendCreateOrderRequest(createOrderRequest);

            switch (orderResponse.getStatus()) {
                case APPROVED -> {
                    List<Game> games = shoppingCart.getGames();
                    currentUser.getOwnedGames().addAll(games);

                    // Send confirmation email
                    String subject = "Order Confirmation";
                    String body = createConfirmationEmail(orderResponse, currentUser, games);
                    emailService.sendEmail(currentUser.getEmail(), subject, body);

                    //Send notification
                    String message = "Thank you for your recent purchase! Your order #" + orderResponse.getId() + " has been successfully processed. " +
                            "You can find purchased games in your library.";

                    sendNotification(message, currentUser);

                    // Remove shopping cart from the App
                    shoppingCartRepository.delete(shoppingCart);

                    // Update user with owned games
                    userRepository.save(currentUser);
                }

                case PENDING -> {
                    String message = "Your order #" + orderResponse.getId() + " is awaiting processing. Your games will be available instantly after payment confirmation.";

                    shoppingCartRepository.delete(shoppingCart);

                    sendNotification(message, currentUser);
                }
            }
        }
    }

    private String createConfirmationEmail(OrderResponseDTO order, User customer, List<Game> games) {
        StringBuilder sb = new StringBuilder();

        for (Game game : games) {
            sb.append("<li>").append(game.getTitle()).append("</li>");
        }

        return "<!DOCTYPE html>\n" +
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
    }

    private void sendNotification(String message, User receiver) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(receiver);

        notificationRepository.save(notification);
    }
}
