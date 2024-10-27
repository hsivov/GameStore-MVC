package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface CheckoutService {
    void payment(String paymentMethod) throws MessagingException, NoSuchAlgorithmException, InvalidKeyException;
}
