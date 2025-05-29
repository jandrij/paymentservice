package com.example.paymentservice.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Integration test - run manually with profile")
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPayment_whenInvalidAmount_shouldReturnBadRequest() throws Exception {
        String json = """
            {
              "type": "TYPE1",
              "amount": -10,
              "currency": "EUR",
              "debtorIban": "DE1234567890",
              "creditorIban": "DE0987654321",
              "details": "some details"
            }
            """;

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());  // assuming you send errors in this field
    }

    @Test
    void createPayment_whenMissingMandatoryFields_shouldReturnBadRequest() throws Exception {
        String json = "{}"; // empty JSON

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createPayment_whenValid_shouldReturnCreated() throws Exception {
        String json = """
            {
              "type": "TYPE1",
              "amount": 100,
              "currency": "EUR",
              "debtorIban": "DE1234567890",
              "creditorIban": "DE0987654321",
              "details": "valid details"
            }
            """;

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());
    }
}