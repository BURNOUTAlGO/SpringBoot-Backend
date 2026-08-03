package org.example;


//  First install the Spring Context Dependency in pom.xml

import com.bhaiyedependencyhai.CartService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //Starting IOC container
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        //fetching the Bean
        OrderService order = context.getBean(OrderService.class);
        order.placeOrder();
         
        User user = context.getBean(User.class);
        System.out.println(user.getName());

        CartService cart = context.getBean(CartService.class);
        cart.addToCart();
    }
}