package org.example;

import org.example.Notifiactions.EmailService;
import org.example.Notifiactions.NotificationService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

//loose coupling
public class Main {
    public static void main(String[] args) {
        NotificationService notification = new EmailService(); // you have to only change the service

        OrderService order = new OrderService(notification);
        order.placeOrder();


    }
}