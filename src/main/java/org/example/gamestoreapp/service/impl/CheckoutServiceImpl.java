package org.example.gamestoreapp.service.impl;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.CheckoutService;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.example.gamestoreapp.util.HMACUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartHelperService cartHelperService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${app.api.key}")
    private String apiKey;

    @Value("${app.api.secret}")
    private String secret;
    public static final String HEADER_API_KEY = "X-Api-Key";
    public static final String HEADER_SIGNATURE = "X-Signature";
    public static final String HEADER_TIMESTAMP = "X-Timestamp";

    public CheckoutServiceImpl(UserHelperService userHelperService, ShoppingCartRepository shoppingCartRepository, CartHelperService cartHelperService, UserRepository userRepository, EmailService emailService, RestTemplate restTemplate) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartHelperService = cartHelperService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public void payment(String paymentMethod) throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(currentUser);

        if (cart.isPresent()) {
            ShoppingCart shoppingCart = cart.get();
            Set<Game> games = shoppingCart.getGames();
            currentUser.getOwnedGames().addAll(games);

            // Prepare request to OrderService
            CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO();
            createOrderRequest.setCustomerId(currentUser.getId());
            createOrderRequest.setGameIds(games.stream().map(Game::getId).toList());
            createOrderRequest.setTotalPrice(cartHelperService.getTotalPrice());
            createOrderRequest.setOrderDate(LocalDateTime.now());
            createOrderRequest.setPaymentMethod(paymentMethod);

            // Send request to OrderService
            OrderResponseDTO orderResponse = sendRequest(createOrderRequest);

            // Send confirmation email
            String subject = "Order Confirmation";
            String body = createConfirmationEmail(orderResponse, currentUser, games);
            emailService.sendEmail(currentUser.getEmail(), subject, body);

            // Remove shopping cart from the App
            shoppingCartRepository.delete(shoppingCart);

            // Update user with owned games
            userRepository.save(currentUser);
        }
    }

    private OrderResponseDTO sendRequest(CreateOrderRequestDTO createOrderRequest) throws NoSuchAlgorithmException, InvalidKeyException {

        String endpoint = "/api/orders/create";
        String url = orderServiceUrl + endpoint;
        String method = "POST";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        // Construct the payload
        String payload = method + endpoint + timestamp;

        // Generate HMAC signature
        String signature = HMACUtil.generateHMAC(payload, secret);

        // Create headers and set Content-Type to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HEADER_API_KEY, apiKey);
        headers.set(HEADER_SIGNATURE, signature);
        headers.set(HEADER_TIMESTAMP, timestamp);

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

    private String createConfirmationEmail(OrderResponseDTO order, User customer, Set<Game> games) {
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
}
