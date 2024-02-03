package droid64.d64;

/**
 * Exception to throw when there is a problem when processing a disk image.
 *
 * @author Henrik
 */
public class CbmException extends Exception {

	private static final long serialVersionUID = 1L;

	public CbmException(String message) {
		super(message);
	}

	public CbmException(Throwable cause) {
		super(cause);
	}

	public CbmException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String toString() {
		return new StringBuilder().append("CbmException[").append(" .message=").append(getMessage()).append(']')
				.toString();
	}
}
