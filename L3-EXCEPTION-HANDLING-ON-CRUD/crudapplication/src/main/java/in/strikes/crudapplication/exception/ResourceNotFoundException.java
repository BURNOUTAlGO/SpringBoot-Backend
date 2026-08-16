package in.strikes.crudapplication.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){
        // ye globalExceptionhandler ke pass forward kardega message
        super(message);
    }
}
