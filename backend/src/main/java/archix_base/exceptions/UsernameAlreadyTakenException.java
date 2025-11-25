package archix_base.exceptions;

public class UsernameAlreadyTakenException extends RuntimeException {

    public UsernameAlreadyTakenException() {
        super("Username already taken");
    }

}
