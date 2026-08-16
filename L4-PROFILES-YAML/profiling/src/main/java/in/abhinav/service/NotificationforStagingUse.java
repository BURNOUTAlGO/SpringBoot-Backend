package in.abhinav.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


// ye notification keval staging phase mein chalegi

@Service
@Profile("staging") // ye annotation spring ko batati hai ki staging enviroment mein is class ka notification chalega
public class NotificationforStagingUse implements NotificationService {
    @Override
    public String send(){
        return "here is your Notification test in Staging Environment";
    }
}
