package in.abhinav.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


// ye notification service keval production mein chalegi
@Service
@Profile("prod")
public class NotificationforProductionUse implements NotificationService {

    @Override
    public String send(){
        return "here is your Notification test in Production Environment ";
    }
}
