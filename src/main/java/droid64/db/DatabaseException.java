package droid64.db;

/**
 * Exception to be thrown when there is a failure during a database access.
 * @author Henrik
 */
public class DatabaseException extends Exception {

	private static final long serialVersionUID = 1L;

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable t) {
		super(t);
	}

	public DatabaseException(String message, Throwable t) {
		super(message, t);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("DatabaseException[")
				.append(" .message=").append(getMessage()).append(']').toString();
	}
}
