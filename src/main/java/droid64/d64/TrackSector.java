package droid64.d64;

public class TrackSector implements Comparable<TrackSector> {

	int track;
	int sector;

	/** Constructor
	 *
	 * @param track
	 * @param sector
	 */
	public TrackSector(int track, int sector) {
		this.track = track;
		this.sector = sector;
	}

	/** Constructor
	 *
	 * @param trackSector to be cloned
	 */
	public TrackSector(TrackSector that) {
		this.track = that.track;
		this.sector = that.sector;
	}

	public int getTrack() {
		return track;
	}

	public void setTrack(int track) {
		this.track = track;
	}

	public int getSector() {
		return sector;
	}

	public void setSector(int sector) {
		this.sector = sector;
	}

	@Override
	public String toString() {
		return "[" + track + ':' + sector + ']';
	}

	public void toString(StringBuilder buf) {
		buf.append('[').append(track).append('/').append(sector).append(']');
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof TrackSector)) {
			return false;
		} else {
			TrackSector other = (TrackSector) obj;
			return this.track == other.track && this.sector == other.sector;
		}
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + sector;
		result = 31 * result + track;
		return result;
	}

	@Override
	public int compareTo(TrackSector other) {
		if (equals(other)) {
			return 0;
		}
		if (this.track == other.track) {
			return Integer.compare(this.sector, other.sector);
		}
		return Integer.compare(this.track, other.track);
	}
}
