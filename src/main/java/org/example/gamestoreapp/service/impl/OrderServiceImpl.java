package org.example.gamestoreapp.service.impl;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.exception.UserNotFoundException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderItemDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.NotificationService;
import org.example.gamestoreapp.service.OrderService;
import org.example.gamestoreapp.update.OrderStatusUpdater;
import org.example.gamestoreapp.util.HMACUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final RestTemplate restTemplate;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final OrderStatusUpdater orderStatusUpdater;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${app.api.key}")
    private String apiKey;

    @Value("${app.api.secret}")
    private String secret;

    public OrderServiceImpl(RestTemplate restTemplate, EmailService emailService, NotificationService notificationService,
                            GameRepository gameRepository, UserRepository userRepository, OrderStatusUpdater orderStatusUpdater) {
        this.restTemplate = restTemplate;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.orderStatusUpdater = orderStatusUpdater;
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() throws NoSuchAlgorithmException, InvalidKeyException {

        String endpoint = "/api/orders";
        String url = orderServiceUrl + endpoint;
        String method = "GET";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponseDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderResponseDTO[].class);

        OrderResponseDTO[] responseBody = response.getBody();

        if (responseBody == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(responseBody);
    }

    @Override
    public OrderResponseDTO getOrderById(long id) throws NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "/api/orders/" + id;
        String url = orderServiceUrl + endpoint;
        String method = "GET";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderResponseDTO.class);

        return response.getBody();
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUser(long userId) throws NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "/api/orders/customer/" + userId;
        String url = orderServiceUrl + endpoint;
        String method = "GET";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponseDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderResponseDTO[].class);

        OrderResponseDTO[] responseBody = response.getBody();

        if (responseBody == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(responseBody);
    }

    @Override
    public OrderResponseDTO sendCreateOrderRequest(CreateOrderRequestDTO createOrderRequest) throws NoSuchAlgorithmException, InvalidKeyException {

        String endpoint = "/api/orders/create";
        String url = orderServiceUrl + endpoint;
        String method = "POST";

        // Create headers and set Content-Type to application/json
        HttpHeaders headers = setHeaders(method, endpoint);

        // Wrap the request and headers in an HttpEntity
        HttpEntity<CreateOrderRequestDTO> entity = new HttpEntity<>(createOrderRequest, headers);

        // Make POST request to OrderService createOrder endpoint
        ResponseEntity<OrderResponseDTO> response = restTemplate.postForEntity(url, entity, OrderResponseDTO.class);

        // Check response status and return the OrderResponseDTO
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create order in OrderService");
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void processPendingOrders() throws NoSuchAlgorithmException, InvalidKeyException, MessagingException {
        String endpoint = "/api/orders/pending";
        String url = orderServiceUrl + endpoint;
        String method = "GET";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponseDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderResponseDTO[].class);

        OrderResponseDTO[] orders = Optional.ofNullable(response.getBody()).orElse(new OrderResponseDTO[0]);

        for (OrderResponseDTO order : orders) {
            OrderStatus newStatus = orderStatusUpdater.updateOrderStatus();
            order.setStatus(newStatus);

            if (newStatus == OrderStatus.APPROVED) {
                User customer = userRepository.findById(order.getCustomer().getId())
                        .orElseThrow(() -> new UserNotFoundException("Customer not found"));

                Set<Long> gameIds = order.getBoughtGames().stream()
                        .map(OrderItemDTO::getOrderItemId).collect(Collectors.toSet());
                List<Game> games = gameRepository.findByIdIn(gameIds);

                completeOrder(order, customer, games);
            } else if (newStatus == OrderStatus.REJECTED) {
                User customer = userRepository.findById(order.getCustomer().getId())
                        .orElseThrow(() -> new UserNotFoundException("Customer not found"));

                endpoint = "/api/orders/update";
                method = "POST";

                headers = setHeaders(method, endpoint);

                HttpEntity<OrderResponseDTO> request = new HttpEntity<>(order, headers);

                restTemplate.postForEntity(url, request, Void.class);

                String message = "Your recent payment attempt for order #" + order.getId() +
                        " on " + order.getOrderDate() + " was unsuccessful. " +
                        "This could be due to various reasons, such as insufficient funds, incorrect details, or a bank authorization issue.";

                notificationService.sendNotification(message, customer);

                String subject = "Order Cancellation";
                String body = createCancellationEmail(order, customer);
                emailService.sendEmail(customer.getEmail(), subject, body);
            }
        }
    }

    @Override
    public void completeOrder(OrderResponseDTO order, User customer, List<Game> boughtGames) throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "/api/orders/update";
        String url = orderServiceUrl + endpoint;
        String method = "POST";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<OrderResponseDTO> request = new HttpEntity<>(order, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        if (response.getStatusCode() == HttpStatus.OK) {

            customer.getOwnedGames().addAll(boughtGames);

            // Update user with owned games
            userRepository.save(customer);

            // Send confirmation email
            String subject = "Order Confirmation";
            String body = createConfirmationEmail(order, customer);
            emailService.sendEmail(customer.getEmail(), subject, body);

            //Send notification
            String message = "Thank you for your recent purchase! Your order #" + order.getId() + " has been successfully processed. " +
                    "You can find purchased games in your library.";

            notificationService.sendNotification(message, customer);
        }
    }

    private HttpHeaders setHeaders(String method, String endpoint) throws NoSuchAlgorithmException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();

        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        // Construct the payload
        String payload = method + endpoint + timestamp;

        // Generate HMAC signature
        String signature = HMACUtil.generateHMAC(payload, secret);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        headers.set("X-Signature", signature);
        headers.set("X-Timestamp", timestamp);

        return headers;
    }

    private String createConfirmationEmail(OrderResponseDTO order, User customer) {
        StringBuilder sb = new StringBuilder();

        for (OrderItemDTO game : order.getBoughtGames()) {
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

    private String createCancellationEmail(OrderResponseDTO order, User customer) {
        return "Dear " + customer.getFirstName() + " " + customer.getLastName() + ",\n" +
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
    }
}
