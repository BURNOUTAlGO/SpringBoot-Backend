package in.abhinav.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

// ye notification keval developing phase mein chalegi
@Service
@Profile("dev")
public class NotificationforDevelopingUse implements NotificationService {
    @Override
    public String send(){
        return "here is your Notification test in Developing Environment";
    }
}
