package org.example.gamestoreapp.exception;

public class UsedTokenException extends RuntimeException{
    public UsedTokenException(String message) {
        super(message);
    }
}
