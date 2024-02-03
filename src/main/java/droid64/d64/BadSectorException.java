package droid64.d64;

/**
 * Exception to throw when there is a problem with a bad sector.
 */
public class BadSectorException extends CbmException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param message
	 * @param track
	 * @param sector
	 */
	public BadSectorException(String message, int track, int sector) {
		super(message + " [" + track + '/' + sector + ']');
	}

	/**
	 * Constructor
	 * @param message
	 * @param block
	 */
	public BadSectorException(String message, TrackSector block) {
		this(message, block.track, block.sector);
	}
}
