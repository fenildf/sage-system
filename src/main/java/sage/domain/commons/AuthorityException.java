package sage.domain.commons;

public class AuthorityException extends DomainException {
  public AuthorityException() {
  }

  public AuthorityException(String message) {
    super(message);
  }

  public AuthorityException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthorityException(Throwable cause) {
    super(cause);
  }
}
