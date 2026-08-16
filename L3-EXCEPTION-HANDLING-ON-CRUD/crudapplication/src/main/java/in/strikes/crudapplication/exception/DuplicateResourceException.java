package in.strikes.crudapplication.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message){
        // ye globalExceptionhandler ke pass forward kardega message
        super(message);
    }
}
