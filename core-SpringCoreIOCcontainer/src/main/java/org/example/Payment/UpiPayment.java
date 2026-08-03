package org.example.Payment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
// spring confuse ho jayega konsa reference pass karoon cardPayment ka ya fir Upi Payment ka
// use using this annotation if you want to  prioritise this component
//@Primary

//otherwise use
@Qualifier

public class UpiPayment implements PaymentService{

    @Override
    public void pay(){
        System.out.println("Payment via UPI has been Done.....");
    }
}
