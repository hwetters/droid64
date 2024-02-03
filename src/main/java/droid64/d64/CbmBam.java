package droid64.d64;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <pre style='font-family:sans-serif;'>
 * Created on 21.06.2004
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2004 Wolfram Heyer
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   eMail: wolfvoz@users.sourceforge.net
 *   http://droid64.sourceforge.net
 * </pre>
 *
 * @author wolf
 */
public class CbmBam implements Serializable {

	private static final long serialVersionUID = 1L;
	private int diskDosType;
	private final int[] freeSectors;
	private final int[][] trackBits;
	private String diskName; // string[16]
	private String diskId; // string[5]

	public CbmBam(int numTracks, int numTrackBytes) {
		freeSectors = new int[numTracks];
		trackBits = new int[numTracks][numTrackBytes - 1];
	}

	/**
	 * @return disk dos type
	 */
	public int getDiskDosType() {
		return diskDosType;
	}

	/**
	 * @return disk id
	 */
	public String getDiskId() {
		return diskId;
	}

	/**
	 * @return disk name
	 */
	public String getDiskName() {
		return diskName;
	}

	/**
	 * @param trackNumber the track number (1..LastTrack)
	 * @return number of free sectors
	 */
	public int getFreeSectors(int trackNumber) {
		return freeSectors[trackNumber - 1];
	}

	/**
	 * @param trackNumber the track number (1..LastTrack)
	 * @param byteNumber  the byte number (1..3)
	 * @return track bits
	 */
	public int getTrackBits(int trackNumber, int byteNumber) {
		return trackBits[trackNumber - 1][byteNumber - 1];
	}

	/**
	 * @param b disk dos type
	 */
	public void setDiskDosType(int b) {
		diskDosType = b;
	}

	/**
	 * @param string disk id
	 */
	public void setDiskId(String string) {
		diskId = string;
	}

	/**
	 * @param string disk name
	 */
	public void setDiskName(String string) {
		diskName = string;
	}

	/**
	 * @param track the track, starting at 1 until last track number.
	 * @param value value
	 */
	public void setFreeSectors(int track, int value) {
		freeSectors[track - 1] = value;
	}

	/**
	 * @param track   track which is a number between 1 and last track.
	 * @param byteNum a number starting 1 and is the number of bytes per track.
	 * @param value   value
	 */
	public void setTrackBits(int track, int byteNum, int value) {
		trackBits[track - 1][byteNum - 1] = value;
	}

	/**
	 * @return hexdump of all BAM entries and the free sectors
	 */
	public String dump() {
		var b = new StringBuilder();
		if (trackBits != null && trackBits.length > 0) {
			for (int t = 0; t < trackBits.length; t++) {
				b.append('[').append(t).append("]\t");
				for (int s = 0; s < trackBits[t].length; s++) {
					b.append(Utility.getByteString(trackBits[t][s])).append(' ');
				}
				b.append('\t').append(freeSectors[t]).append('\n');
			}
		} else {
			b.append("null");
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return new StringBuilder().append("CbmBam [").append(" diskDosType=").append(diskDosType)
				.append(" freeSectors=").append(Arrays.toString(freeSectors)).append(" trackBits=")
				.append(Arrays.toString(trackBits)).append(" diskName=").append(diskName).append(" diskId=")
				.append(diskId).append(']').toString();
	}
}
