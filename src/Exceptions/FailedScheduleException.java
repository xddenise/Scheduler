package Exceptions;

public class FailedScheduleException extends Exception {

    public FailedScheduleException(String errorMessage){
        super(errorMessage);
    }
}
