package com.example.paymentservice.integration.controller;

import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.CountryResolverService;
import com.example.paymentservice.service.CountryResolverServiceImpl;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository repo;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public CountryResolverService countryResolverService() {
            return Mockito.mock(CountryResolverServiceImpl.class);
        }
    }

    @AfterEach
    public void cleanup() {
        repo.deleteAll();
    }

    @Test
    void testCreatePayment_InvalidAmount_ReturnsUnprocessableEntity() throws Exception {
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
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testCreatePayment_Success() throws Exception {
        String json = """
            {
              "type": "TYPE1",
              "amount": 100.00,
              "currency": "EUR",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "details": "Payment details"
            }
            """;

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetAllActivePayments_Success() throws Exception {
        insertInitialValues();
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void testGetAllActivePaymentsWithQuery_Success() throws Exception {
        insertInitialValues();
        mockMvc.perform(get("/payments")
                    .param("amountMin", "30")
                    .param("amountMax", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testCancelPayment_Success() throws Exception {
        String json = """
            {
              "type": "TYPE1",
              "amount": 100.00,
              "currency": "EUR",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "details": "Payment details"
            }
            """;
        MvcResult result = mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Integer id = JsonPath.read(responseBody, "$.id");

        mockMvc.perform(post("/payments/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cancellationFee").value(0.00));
    }

    @Test
    void testGetAllActivePaymentsAfterCancel_Success() throws Exception {
        HashMap<String, Integer> paymentIds = insertInitialValues();
        mockMvc.perform(post("/payments/" + paymentIds.get("second") + "/cancel"));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetPayment_Success() throws Exception {
        String json = """
            {
              "type": "TYPE1",
              "amount": 100.00,
              "currency": "EUR",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "details": "Payment details"
            }
            """;
        Integer id = createPaymentAndReturnId(json);

        mockMvc.perform(get("/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cancellationFee").doesNotExist());
    }

    @Test
    void testGetCancelledPayment_Success() throws Exception {
        String json = """
            {
              "type": "TYPE1",
              "amount": 100.00,
              "currency": "EUR",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "details": "Payment details"
            }
            """;
        Integer id = createPaymentAndReturnId(json);
        mockMvc.perform(post("/payments/" + id + "/cancel"));

        mockMvc.perform(get("/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cancellationFee").value(0.00));
    }

// ---------------------------------------------------------------------------------------------------------------------
// Utility functions
// ---------------------------------------------------------------------------------------------------------------------

    Integer createPaymentAndReturnId(String json) throws Exception {
        MvcResult result = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        return JsonPath.read(responseBody, "$.id");
    }

    HashMap<String,Integer> insertInitialValues() throws Exception {
        String firstJson = """
            {
              "type": "TYPE1",
              "amount": 10.00,
              "currency": "EUR",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "details": "Payment details"
            }
            """;

        String SecondJson = """
            {
              "type": "TYPE2",
              "amount": 50.00,
              "currency": "USD",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "details": "Payment details"
            }
            """;

        String ThirdJson = """
            {
              "type": "TYPE3",
              "amount": 100.00,
              "currency": "EUR",
              "debtorIban": "LT1234567890",
              "creditorIban": "LT0987654321",
              "creditorBankBic": "NORZNOZZ77"
            }
            """;

        HashMap<String,Integer> paymentIds = new HashMap<>();
        paymentIds.put("first", createPaymentAndReturnId(firstJson));
        paymentIds.put("second", createPaymentAndReturnId(SecondJson));
        paymentIds.put("third", createPaymentAndReturnId(ThirdJson));
        return paymentIds;
    }
}