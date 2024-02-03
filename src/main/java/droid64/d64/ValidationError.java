package droid64.d64;

import java.io.Serializable;

public class ValidationError implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Error {
		UNKNOWN("Unknown"),
		/** 1. Too many errors encountered. Giving up. */
		ERROR_TOO_MANY("Too many errors"),
		/** 2. Referred directory sector outside of image. */
		ERROR_DIR_SECTOR_OUTSIDE_IMAGE("Directory sector outside of image"),
		/** 3. Directory sector was already seen (cyclic reference detected). */
		ERROR_DIR_SECTOR_ALREADY_SEEN("Cyclic directory sector references"),
		/** 4. Directory sector already seen and marked as used. */
		ERROR_DIR_SECTOR_ALREADY_USED("Directory sector already used"),
		/** 5. Directory sector already seen and marked as free. */
		ERROR_DIR_SECTOR_ALREADY_FREE("Directory sector already free"),
		/** 6. Partitions are only supported on D81 images. */
		ERROR_PARTITIONS_UNSUPPORTED("Partitions only supported on D81 images"),
		/** 7. Referred file sector outside of image. */
		ERROR_FILE_SECTOR_OUTSIDE_IMAGE("File sector outside of image"),
		/** 8. File sector already seen (cyclic reference detected). */
		ERROR_FILE_SECTOR_ALREADY_SEEN("Cyclic file sector references"),
		/** 9. File sector already seen and marked as used. */
		ERROR_FILE_SECTOR_ALREADY_USED("File sector already used"),
		/** 10. File sector already seen and marked as free. */
		ERROR_FILE_SECTOR_ALREADY_FREE("File sector already free"),
		/** 11. Used sector is marked as free. */
		ERROR_USED_SECTOR_IS_FREE("Used sector is marked as free"),
		/** 12. Unused sector is marked as used. */
		ERROR_UNUSED_SECTOR_IS_ALLOCATED("Unused sector is marked as used"),
		/** 13. BAM free sector mismatch. */
		ERROR_BAM_FREE_SECTOR_MISMATCH("BAM free sector mismatch");

		private final String msg;

		private Error(String msg) {
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public ValidationError getError(int track, int sector) {
			return new ValidationError(track, sector, this);
		}

		public ValidationError getError(TrackSector block) {
			return getError(block.track, block.sector);
		}

		public ValidationError getError(TrackSector block, String fileName) {
			return getError(block.track, block.sector, fileName);
		}

		public ValidationError getError(int track, int sector, String fileName) {
			return new ValidationError(track, sector, this, fileName);
		}
	}

	private final int track;
	private final int sector;
	private final String fileName;
	private final Error error;

	private ValidationError(int track, int sector, Error error) {
		this.track = track;
		this.sector = sector;
		this.error = error;
		this.fileName = null;
	}

	private ValidationError(int track, int sector, Error error, String fileName) {
		this.track = track;
		this.sector = sector;
		this.error = error;
		this.fileName = fileName;
	}

	public int getTrack() {
		return track;
	}

	public int getSector() {
		return sector;
	}

	public Error getError() {
		return error;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("ValidationError[")
		.append(" .track=").append(track)
		.append(" .sector=").append(sector)
		.append(" .error=").append(error)
		.append(" .fileName=").append(fileName)
		.append(']').toString();
	}

}
