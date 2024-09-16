package exception;

public class EmptyCollectionException extends Exception {
    public  EmptyCollectionException(){
    }

    public  EmptyCollectionException(String message){
        super(message);
    }
}
