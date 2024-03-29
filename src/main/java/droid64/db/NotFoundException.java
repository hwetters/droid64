package droid64.db;

/**
 * Exception to be thrown when specific data could not be found in database.
 * @author Henrik
 */
public class NotFoundException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable t) {
		super(t);
	}

	public NotFoundException(String message, Throwable t) {
		super(message, t);
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("NotFoundException{")
		.append(" .message=").append(getMessage())
		.append(']')
		.toString();
	}
}
