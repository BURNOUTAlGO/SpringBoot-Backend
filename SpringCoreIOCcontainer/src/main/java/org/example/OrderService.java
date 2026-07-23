package org.example;

import org.example.Payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrderService {
    //Constructor dependency Injection

    private PaymentService paymentService;
    @Autowired
    //bean name is in camel case - "upiPayment"
    public OrderService(@Qualifier("upiPayment") PaymentService paymentService){
        this.paymentService = paymentService;

    }

    public void placeOrder(){
        paymentService.pay();
        System.out.println("Order Placed.....");
    }
}
