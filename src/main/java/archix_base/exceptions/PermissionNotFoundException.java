package archix_base.exceptions;

public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException(Long message) {
        super(String.valueOf(message));
    }
}
