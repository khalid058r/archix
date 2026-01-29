package archix_base.common.exception;













public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException(Long message) {
        super(String.valueOf(message));
    }
}








