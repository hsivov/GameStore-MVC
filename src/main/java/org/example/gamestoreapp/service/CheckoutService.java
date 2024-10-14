package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;

public interface CheckoutService {
    void payment() throws MessagingException;
}
