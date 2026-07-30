package in.abhinav;

import org.springframework.stereotype.Component;

@Component
public class PaymentGateway {

    //create an object of class paymentproperties

    private PaymentProperties paymentProperties;

// YE EK RUNNER FUNCTION HAI JO SAARI VALUES PRINT KAR DEGA
    public void print(){
        System.out.println(getRetryCount());
        System.out.println(getTimeout());
        System.out.println(isEnabled());
        System.out.println(getType());
    }

    public PaymentGateway(PaymentProperties paymentProperties){
        this.paymentProperties=paymentProperties;
    }

    //only GETTERS

    public String getType() {
        return paymentProperties.getType();
    }

    public int  getRetryCount(){
        return paymentProperties.getRetrycount();
    }
    public boolean isEnabled() {
        return paymentProperties.isEnabled();
    }
    public int getTimeout(){
        return paymentProperties.getTimeout();
    }


}
