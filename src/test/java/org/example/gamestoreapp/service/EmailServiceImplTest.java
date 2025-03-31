package org.example.gamestoreapp.service;

import jakarta.mail.internet.MimeMessage;
import org.example.gamestoreapp.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage message;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testSendMail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendEmail("test@example.com", "Test subject", "Test body");

        verify(mailSender, times(1)).send(message);
    }
}
