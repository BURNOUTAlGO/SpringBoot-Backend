package in.abhinav;


import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


//Note : ye class kya karegi -
// application start hote hi print function run hoga aur saari values print ho jayegi
// aur mainclass mein hume kisi ka object banane ki jaroorat nhi hai na hi context variable bananeki
@Component
public class DemoRunner implements ApplicationRunner {

    private PaymentGateway paymentGateway;

    public DemoRunner(PaymentGateway paymentGateway){
        this.paymentGateway =paymentGateway;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        paymentGateway.print();
    }
}
