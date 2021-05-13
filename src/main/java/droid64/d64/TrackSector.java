package droid64.d64;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrackSector", propOrder = {
	"track",
	"sector"
    })
public class TrackSector {

	@XmlElement(required = true)
	int track;
	@XmlElement(required = true)
	int sector;

	public TrackSector(int track, int sector) {
		this.track = track;
		this.sector = sector;
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
}
