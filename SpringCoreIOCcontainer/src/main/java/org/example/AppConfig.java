package org.example;

//Rules Defining Class which later being passed as reference in application context


import com.bhaiyedependencyhai.CartService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class AppConfig {

    //rules- yahan pe hum khudh object banayenge kiska - jo external dependency humne install ki hai .
    // aur jo hum chahte hai ki ioc container us dependency ke object manage kare.
    @Bean
    public User createUser(){
        return new User("Abhinav",20);
    }
    @Bean
    public CartService createCartService(){
         return new CartService();
    }


}
