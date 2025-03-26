package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.config.TestConfig;
import org.example.gamestoreapp.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutController checkoutController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ViewResolver viewResolver = new TestConfig().viewResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController)
                .setViewResolvers(viewResolver).build();
    }

    @Test
    void testCheckout() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"));
    }

    @Test
    void testPayment() throws Exception {
        String paymentMethod = "CREDIT_CARD";

        mockMvc.perform(post("/payment")
                        .param("paymentMethod", paymentMethod))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/library"));

        verify(checkoutService, times(1)).payment(paymentMethod);
    }
}