package com.hasanalmunawr.Spring_Payment_Gateway_Paypal.controller;


import com.hasanalmunawr.Spring_Payment_Gateway_Paypal.service.PaypalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaypalController {

    private final PaypalService paypalService;

    @GetMapping
    public String home() {
        return "index";
    }

    @PostMapping("/pay")
    public RedirectView createPayment() {
        try {
            String cancelUrl = "test";
            String successUrl = "test";

            Payment payment = paypalService.createPayment(
                    10.0,
                    "USD",
                    "paypal",
                    "sale",
                    "Payment Description",
                    cancelUrl,
                    successUrl
            );

            return payment.getLinks()
                    .stream()
                    .filter(
                            links -> "approval_url".equals(links.getRel()))
                    .findFirst()
                    .map(
                            links ->
                                    new RedirectView(links.getHref()))
                    .orElse(null);
        } catch (PayPalRESTException e) {
            log.error("Error Due to : ", e);
        }
        return new RedirectView("/payment/failed");
    }

    @GetMapping(path = "/success")
    public String paymentSuccess(
            @RequestParam("paymentID") String paymentId,
            @RequestParam("payerID") String payerId
    ) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                return "paymentSuccess";
            }
        } catch (PayPalRESTException e) {
            log.error("Error Due To: ", e);
        }
        return "paymentSuccess";
    }

    @GetMapping(path = "/failed")
    public String paymentFailed() {
        return "paymentFail";
    }
}