package droid64.d64;

public class D88Constants {

	/** Number of tracks (77) of image */
	public static final int TRACK_COUNT	= 77;

	/** Number of sectors per track (26*2) */
	public static final int TRACK_SECTORS	= 52;

	/** Directory track sector sequence */
	protected static final int[] DIR_SECTORS = {
			10, 13, 16, 19, 22, 25,  1,  4,
			 7, 11, 14, 17, 21, 24,  2,  5,
			 8, 12, 15,	18, 23, 26,  3,  6,
			 9, 44, 47, 50, 53, 56, 33, 36,
			39, 42, 45, 48, 51, 54, 57, 34,
			37, 40, 43, 46, 49, 52, 55,	58,
			35, 38, 41 };

	private D88Constants() {
		// Unused
	}

}
