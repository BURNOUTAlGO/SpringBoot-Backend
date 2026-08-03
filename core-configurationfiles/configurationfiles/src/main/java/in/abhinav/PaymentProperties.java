package in.abhinav;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


//Note: ye file apllication.properties se value legi aur Paymentgateway class mein dependency inject karegi
@Component
// application.properties file mein jao aur ("payment-propertie") prefix ki values leke aoo
@ConfigurationProperties("payment-propertie")
public class PaymentProperties {

    private String type;
    private int retrycount;
    private int timeout;
    private boolean enabled;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRetrycount() {
        return retrycount;
    }

    public void setRetrycount(int retrycount) {
        this.retrycount = retrycount;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }




}
