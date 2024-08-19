package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.gamestoreapp.validation.annotation.PasswordMatches;
import org.example.gamestoreapp.validation.annotation.UniqueEmail;
import org.example.gamestoreapp.validation.annotation.UniqueUsername;

@PasswordMatches
public class UserRegisterBindingModel {
    @Size(min = 3, max = 30, message = "Username length must be between 3 and 30 characters!")
    @UniqueUsername
    private String username;
    @Email
    @NotBlank(message = "Email cannot be empty!")
    @UniqueEmail
    private String email;
    @NotBlank(message = "First name cannot be empty!")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty!")
    private String lastName;
    @Positive(message = "Age must be positive number!")
    private int age;
    @Size(min = 4, max = 20, message = "Password length must be between 4 and 20 characters!")
    private String password;
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
