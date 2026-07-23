package org.example;

import org.example.Notifiactions.EmailService;
import org.example.Notifiactions.NotificationService;

public class OrderService {
    NotificationService notification;

    public OrderService(NotificationService notification){
        this.notification=notification;

    }


    public void placeOrder(){
        System.out.println("Order placed...");
        notification.sendNotification();

    }
}
