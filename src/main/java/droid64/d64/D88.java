package droid64.d64;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import droid64.gui.BAMPanel.BamState;
import droid64.gui.BAMPanel.BamTrack;
import droid64.gui.ConsoleStream;

/**<pre style='font-family:sans-serif;'>
 * Created on 2019-Jan-15
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2015 Henrik Wetterström
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
 *   http://droid64.sourceforge.net
 *
 * @author Henrik
 * </pre>
 */
public class D88 extends DiskImage {

	private static final long serialVersionUID = 1L;
	/** Max number of directory entries in image : (26*2-1)*8 = 408 */
	protected static final int FILE_NUMBER_LIMIT = 408;
	/** The normal size of a D88 image (77 * 52 * 256) */
	private static final int D88_SIZE       = 1025024;
	/** Maximum number of sectors on any track */
	private static final int MAX_SECTORS    = 52;
	/** Number of tracks of image */
	private static final int TRACK_COUNT	= 77;
	/** Double sided, thus two heads. */
	private static final int HEAD_COUNT = 2;
	/** Array with track/sector to BAM sectors */
	private transient TrackSector[] bamSectors = {null, null, null, null}; // 0-24, 25-49, 50-74, 75-76
	/** 1 byte for free sectors on track, and one bit per sector (5 bytes / 40 bits) for each head */
	private static final int BYTES_PER_BAM_GROUP = 5;

	public D88(ConsoleStream consoleStream) {
		this.feedbackStream = consoleStream;
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_GROUP * HEAD_COUNT - 1);
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	public D88(byte[] imageData, ConsoleStream consoleStream) {
		this.feedbackStream = consoleStream;
		cbmDisk = imageData;
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_GROUP * HEAD_COUNT - 1);
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	private TrackSector getDirBlock() {
		return new TrackSector(cbmDisk[0x04] & 0xff, cbmDisk[0x05] & 0xff);
	}

	private TrackSector getHeaderBlock() {
		return new TrackSector(cbmDisk[0x06] & 0xff, cbmDisk[0x07] & 0xff);
	}

	private TrackSector[] getBamSectors() {
		try {
			int i=0;
			for (TrackSector t = new TrackSector(getCbmDiskValue(0, 1, 0x08), getCbmDiskValue(0, 1, 0x09));
					t.track != 0xff && i < bamSectors.length; t = nextBlock(t)) {
				bamSectors[i++] = t;
			}
		} catch (IndexOutOfBoundsException | CbmException e) {
			// ignore
		}
		return bamSectors;
	}

	private TrackSector nextBlock(TrackSector ts) throws CbmException {
		verifyTrackSector(ts.track, ts.sector);
		return new TrackSector(getCbmDiskValue(ts.track, ts.sector, 0), getCbmDiskValue(ts.track, ts.sector, 1));
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		return MAX_SECTORS;
	}

	@Override
	public int getTrackCount() {
		return TRACK_COUNT;
	}

	@Override
	public int getMaxSectorCount() {
		return MAX_SECTORS;
	}

	@Override
	public int getBlocksFree() {
		int blocksFree = 0;
		if (cbmDisk != null) {
			for (int track = getFirstTrack(); track < getTrackCount() + getFirstTrack(); track++) {
				blocksFree += bam.getFreeSectors(track + 1);
			}
		}
		return blocksFree;
	}
	@Override
	protected DiskImage readImage(File file) throws CbmException {
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_GROUP * HEAD_COUNT - 1);
		return readImage(file, DiskImageType.D88);
	}

	@Override
	public void readBAM() {
		getBamSectors();
		int headerOffset = getSectorOffset(getHeaderBlock());
		bam.setDiskName(Utility.getString(cbmDisk, headerOffset + 0x06, DISK_NAME_LENGTH));
		bam.setDiskId(Utility.getString(cbmDisk, headerOffset + 0x18, DISK_ID_LENGTH));
		bam.setDiskDosType(getCbmDiskValue(headerOffset + 2 ));
		for (int track = 0; track < TRACK_COUNT; track++) {
			int pos = getBamTrackPos(track);
			if (track==0) {
				bam.setFreeSectors(track+1,0);
			} else {
				bam.setFreeSectors(track+1, getCbmDiskValue(pos) + getCbmDiskValue(pos + BYTES_PER_BAM_GROUP));
			}
			for (int i = 1; i < BYTES_PER_BAM_GROUP; i++) {
				bam.setTrackBits(track+1, i, getCbmDiskValue(pos + i));
				bam.setTrackBits(track+1, i + BYTES_PER_BAM_GROUP-1, getCbmDiskValue(pos + i + BYTES_PER_BAM_GROUP ));
			}
		}
		checkImageFormat();
	}

	@Override
	public void verifyTrackSector(int track, int sector) throws CbmException {
		if (track < getFirstTrack() || track > getTrackCount() + getFirstTrack()) {
			throw new CbmException("Track " + track + " is not valid.");
		} else if ((sector > 26 && sector < 33)
				|| sector < getFirstSector()
				|| sector > 58) {
			throw new BadSectorException("Invalid sector:", track, sector);
		}
	}

	@Override
	public int getFirstTrack() {
		return 0;
	}

	@Override
	public int getFirstSector() {
		return 1;
	}

	@Override
	public String getSectorTitle(int i) {
		if (i <= 26) {
			return Integer.toString(i);
		} else {
			return Integer.toString(i+6);
		}
	}

	@Override
	public void readDirectory() {
		if (isCpmImage()) {
			// Read CP/M directory here
		} else {
			TrackSector dirBlock = getDirBlock();
			readDirectory(dirBlock.track, dirBlock.sector, FILE_NUMBER_LIMIT);
			validate(null);
		}
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("getFileData: No disk data exist.");
		} else if (number >= cbmFile.length) {
			throw new CbmException("getFileData: File number " + number + " does not exist.");
		} else if (isCpmImage()) {
			feedbackStream.append("getFileData: CP/M mode.\n");
			throw new CbmException("Not yet implemented for CP/M format.");
		} else if (cbmFile[number].isFileScratched()) {
			throw new CbmException("getFileData: File number " + number + " is deleted.");
		}
		feedbackStream.append("getFileData: ").append(number).append(" '").append(cbmFile[number].getName()).append("'\n");
		feedbackStream.append("Tracks / Sectors: ");
		return getData(cbmFile[number].getTrack(), cbmFile[number].getSector());
	}

	@Override
	protected TrackSector saveFileData(byte[] saveData) {
		if (isCpmImage()) {
			feedbackStream.append(NOT_IMPLEMENTED_FOR_CPM);
			return null;
		}
		int usedBlocks = 0;
		int dataRemain = saveData.length;
		feedbackStream.append("SaveFileData: ").append(dataRemain).append(" bytes of data.\n");
		TrackSector firstBlock = findFirstCopyBlock();
		if (firstBlock != null) {
			TrackSector block = new TrackSector(firstBlock.track, firstBlock.sector);
			int thisTrack;
			int thisSector;
			int dataPos = 0;
			while (dataRemain >= 0 && block != null) {
				feedbackStream.append(dataRemain).append(" bytes remain: block ").append(block.track).append('/').append(block.sector).append('\n');
				thisTrack = block.track;
				thisSector = block.sector;
				markSectorUsed(thisTrack, thisSector);
				if (dataRemain >= (BLOCK_SIZE - 2)) {
					block = findNextCopyBlock(block);
					if (block != null) {
						fillSector(thisTrack, thisSector, dataPos, block.track, block.sector, saveData);
						usedBlocks++;
						dataRemain = dataRemain - (BLOCK_SIZE - 2);
						dataPos = dataPos + (BLOCK_SIZE - 2);
					} else {
						feedbackStream.append("\nsaveFileData: Error: Not enough free sectors on disk. Disk is full.\n");
						firstBlock = null;
					}
				} else {
					fillSector(thisTrack, thisSector, dataPos, 0, dataRemain + 1, saveData);
					usedBlocks++;
					dataRemain = -1;
				}
			}
			if (dataRemain <= 0) {
				feedbackStream.append("All data written (").append(usedBlocks).append(" blocks).\n");
			}
		} else {
			feedbackStream.append("\nsaveFileData: Error: No free sectors on disk. Disk is full.\n");
			return null;
		}
		return firstBlock;
	}

	/**
	 * Find a sector for the first block of the file,
	 * @return track/sector or null if none is available.
	 */
	private TrackSector findFirstCopyBlock() {
		TrackSector dirBlock = getDirBlock();
		TrackSector block = new TrackSector(0, 0);
		if (geosFormat) {
			// GEOS formatted disk, so use the other routine, from track one upwards.
			block.track = 0;
			block.sector = 1;
			block = findNextCopyBlock(block);
		} else {
			boolean found = false;	// No free sector found yet
			int distance = 1;		// On a normal disk, start looking checking the tracks just besides the directory track.
			while (!found && distance < 200) {
				// Search until we find a track with free blocks or move too far from the directory track.
				block.track = dirBlock.track - distance;
				if (block.track >= getFirstTrack() && block.track < TRACK_COUNT+getFirstTrack() && block.track != dirBlock.track) {
					// Track within disk limits
					found = isTrackFree(block.track+1);
				}
				if (!found){
					// Check the track above the directory track
					block.track = dirBlock.track + distance;
					if (block.track <= TRACK_COUNT && block.track != dirBlock.track) {
						// Track within disk limits
						found = isTrackFree(block.track+1);
					}
				}
				if (!found) {
					// Move one track further away from the directory track and try again.
					distance++;
				}
			}
			if (found) {
				// Found a track with, at least one free sector, so search for a free sector in it.
				block.sector = 1;									// Start off with sector 1.
				do {
					found = isSectorFree(block.track, block.sector);
					if (!found) {
						block.sector++;	// Try the next sector.
						if (block.sector > 26 && block.sector < 33) {
							block.sector = 33;
						}
					}
				} while (!found && block.sector <= 58);	// Repeat until there is a free sector or run off the track.
				if (!found) {
					feedbackStream.append("firstCopyBlock: Error: "+block.track +" should have free sectors but didn't.\n");
					block = null;
				}
			} else {
				// Disk full. No tracks with any free blocks.
				block = null;
			}
		}
		if (block != null) {
			feedbackStream.append("firstCopyBlock: The first block will be ").append(block.track).append('/').append(block.sector).append(".\n");
		} else {
			feedbackStream.append("firstCopyBlock: Error: Disk is full!\n");
		}
		return block;
	}

	/**
	 * Find a sector for the next block of the file, using variables Track and Sector.
	 * @param block
	 * @return sector found, null if no more sectors left
	 */
	private TrackSector findNextCopyBlock(TrackSector block) {
		TrackSector dirBlock = getDirBlock();
		boolean found;
		if ((block.track == 0) || (block.track >= TRACK_COUNT)) {
			// If we somehow already ran off the disk then there are no more free sectors left.
			return null;
		}
		int tries = 3;			// Set the number of tries to three.
		found = false;			// We found no free sector yet.
		int curTrack = block.track;		// Remember the current track number.
		while (!found && tries > 0) {
			// Keep trying until we find a free sector or run out of tries.
			if (isTrackFree(block.track+1)) {
				// If there's, at least, one free sector on the track then get searching.
				if (block.track == curTrack || !geosFormat) {
					// If this is a non-GEOS disk or we're still on the same track of a GEOS-formatted disk then...
					block.sector = block.sector + C1581_INTERLEAVE;	// Move away an "interleave" number of sectors.
					if (geosFormat && block.track >= 25) {
						// Empirical GEOS optimization, get one sector backwards if over track 25.
						block.sector--;
						if (block.sector > 26 && block.sector < 33) {
							block.sector = 26;
						}
					}
				} else {
					// For a different track of a GEOS-formatted disk, use sector skew.
					block.sector = (block.track - curTrack) << 1 + 4 + C1581_INTERLEAVE;
				}
				while (block.sector > 58) {
					// If we ran off the track then correct the result.
					block.sector = block.sector - 58 + 1;	// Subtract the number of sectors on the track.
					if (block.sector > 0 && !geosFormat) {
						// Empirical optimization, get one sector backwards if beyond sector zero.
						block.sector--;
						if (block.sector > 26 && block.sector < 33) {
							block.sector = 26;
						}
					}
				}
				int curSector = block.sector;	// Remember the first sector to be checked.
				do {
					found = isSectorFree(block.track, block.sector) && block.track!=dirBlock.track;
					if (!found) {
						block.sector++;	// Try next sector
					}
					if (block.sector > 26 && block.sector < 33) {
						block.sector = 33;
					}
					if (block.sector > 58) {
						block.sector = 1;	// Went off track, wrap around to sector 1.
					}
				} while (!found && block.sector != curSector);	// Continue until finding a free sector, or we are back on the curSector again.
				if (!found) {
					// According to the free sector counter in BAM, this track should have free sectors, but it didn't.
					// Try a different track. Obviously, this disk needs to be validated.
					feedbackStream.append("Warning: Track ").append(block.track).append(" should have at least one free sector, but didn't.");
					if (block.track > getFirstTrack() && block.track <= dirBlock.track) {
						block.track = block.track - 1 ;
					} else if (block.track < TRACK_COUNT && block.track > dirBlock.track) {
						block.track = block.track + 1 ;
						if (block.track == getHeaderBlock().track) {
							block.track = block.track + 1 ;
						}
					} else {
						tries--;
					}
				}
			} else {
				if (block.track == dirBlock.track) {
					// If we already tried the directory track then there are no more tries.
					tries = 0;
				} else {
					if (block.track < dirBlock.track) {
						block.track --;	//If we're below the directory track then move one track downwards.
						if (block.track < getFirstTrack()) {
							block.track = dirBlock.track + 1; //If we ran off the disk then step back to the track just above the directory track and 1 the sector number.
							block.sector = 1;
							//If there are no tracks available above the directory track then there are no tries left; otherwise just decrease the number of tries.
							if (block.track <= TRACK_COUNT) {
								tries--;
							} else {
								tries = 0;
							}
						}
					} else {
						block.track++;	//If we're above the directory track then move one track upwards.
						if (block.track == dirBlock.track) {
							block.track++;
						}
						if (block.track >= TRACK_COUNT) {
							block.track = dirBlock.track - 1;	//If we ran off the disk then step back to the track just below the directory track and 1 the sector number.
							block.sector = 1;
							//If there are no tracks available below the directory track then there are no tries left; otherwise just decrease the number of tries.
							if (block.track >= getFirstTrack()) {
								tries--;
							} else {
								tries = 0;
							}
						}
					}
				}
			}
		}
		return found ? block : null;
	}

	@Override
	protected void setDiskName(String newDiskName, String newDiskID) {
		feedbackStream.append("setDiskName: '").append(newDiskName).append("', '").append(newDiskID).append("'\n");
		int headerPos = getSectorOffset(getHeaderBlock());
		Utility.setPaddedString(cbmDisk, headerPos + 0x06, newDiskName, DISK_NAME_LENGTH);
		Utility.setPaddedString(cbmDisk, headerPos + 0x18, newDiskID, DISK_ID_LENGTH);
	}

	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {
		int entryNum = dirEntryNumber;
		TrackSector ts = getDirBlock();
		int thisTrack = ts.track;
		int thisSector = ts.sector;
		feedbackStream.append("writeDirectoryEntry: bufferCbmFile to dirEntryNumber ").append(entryNum).append(".\n");
		if (entryNum > 7) {
			while (entryNum > 7) {
				thisTrack  = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00);
				thisSector = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01);
				feedbackStream.append("LongDirectory: ").append(entryNum).append(" dirEntrys remain, next block: ").append(thisTrack).append('/').append(thisSector).append('\n');
				entryNum = entryNum - 8;
			}
		}
		int pos = getSectorOffset(thisTrack, thisSector) + entryNum * 32;
		setCbmDiskValue(pos + 0, cbmFile.getDirTrack());
		setCbmDiskValue(pos + 1, cbmFile.getDirSector());
		writeSingleDirectoryEntry(cbmFile, pos);
	}

	@Override
	public boolean saveNewImage(File file, String newDiskName, String newDiskID) {
		cbmDisk = new byte[D88_SIZE];
		Arrays.fill(cbmDisk, (byte) 0);
		if (!isCpmImage()) {
			// BadSectorBlock (0/2), ?? (00/ff), dirBlock (38/10), headerBlock (38/20), BAM (1/1)
			int[] hdr= {0, 2, 0, 0xff, 38, 10, 38, 20, 1, 1, 0x30, 0x31};
			for (int i=0; i<hdr.length;i++) {
				setCbmDiskValue(i, hdr[i]);
			}
			Arrays.fill(cbmDisk, BLOCK_SIZE, BLOCK_SIZE*2, (byte) 0xff);	// no bad sectors
			int[][] bamSectorSetup = {
					{ 1,1,  26,1, -1,-1,  0,24},
					{26,1,  51,1,  1,1,  25,49},
					{51,1,  76,1, 26,1,  50,74},
					{76,1, -1,-1, 51,1,  75,76}};
			prepareBam(bamSectorSetup);

			setCbmDiskValue(0x7cb00, 38);
			setCbmDiskValue(0x7cb01, 10);
			setCbmDiskValue(0x7cb1b, '3');
			setCbmDiskValue(0x7cb1c, 'A');
			// track 0:  reserve 0/1 and 0/2  in BAM
			setCbmDiskValue(1, 1, 6, 24);
			setCbmDiskValue(1, 1, 7, 0xf8);
			// track 1: reserve BAM1 1/1
			setCbmDiskValue(1, 1, 16, 25);
			setCbmDiskValue(1, 1, 17, 0xfc);
			// track 26: reserve BAM2 26/1
			setCbmDiskValue(26, 1, 16, 25);
			setCbmDiskValue(26, 1, 17, 0xfc);
			// track 51: reserve BAM3 51/1
			setCbmDiskValue(51, 1, 16, 25);
			setCbmDiskValue(51, 1, 17, 0xfc);
			// track 76: reserve BAM4 76/1
			setCbmDiskValue(76, 1, 16, 25);
			setCbmDiskValue(76, 1, 17, 0xfc);

			// track 38: reserve dirblock1 (38/10) and headerblock (38/20)
			setCbmDiskValue(26, 1, 0x88, 24);
			setCbmDiskValue(26, 1, 0x89, 0xfe);
			setCbmDiskValue(26, 1, 0x8a, 0xfb);
			setCbmDiskValue(26, 1, 0x8b, 0xef);
			setCbmDiskValue(26, 1, 0x8c, 0x07);

			setCbmDiskValue(38, 10, 1, 0xff);	// first dir block pointer to next.

			setDiskName(Utility.cbmFileName(newDiskName, DISK_NAME_LENGTH), Utility.cbmFileName(newDiskID, DISK_ID_LENGTH));
			return saveAs(file);
		}
		return false;
	}

	/**
	 * Setup BAM for all tracks where all valid blocks are free.
	 * Argument is a 2D matrix where each row is:
	 * <ol start="0">
	 * <li>thisBamTrack</li><li>thisBamSector</li><li>nextBamTrack</li><li>nextbamSector</li><li>previousBamTrack</li><li>previousBamSector</li><li>firstTrack</li><li>lastTrack</li>
	 * </ol>
	 * @param bam
	 */
	private void prepareBam(int[][] bam) {
		int[] bamBytes = {26, 0xfe, 0xff, 0xff, 0x07};
		for (int b=0; b<bam.length; b++) {
			int pos = getSectorOffset(bam[b][0], bam[b][1]);
			// next bam
			setCbmDiskValue(pos+0, bam[b][2]);
			setCbmDiskValue(pos+1, bam[b][3]);
			// previous bam
			setCbmDiskValue(pos+2, bam[b][4]);
			setCbmDiskValue(pos+3, bam[b][5]);
			// first/last tracks
			setCbmDiskValue(pos+4, bam[b][6]);
			setCbmDiskValue(pos+5, bam[b][7] + 1);
			int numTracks = bam[b][7] - bam[b][6];
			for (int i = 0; i <= numTracks; i++) {
				for (int n=0; n  < bamBytes.length;n++) {
					setCbmDiskValue(pos+6 + i*BYTES_PER_BAM_GROUP*2 + n, bamBytes[n]);	// head 0
					setCbmDiskValue(pos+6 + i*BYTES_PER_BAM_GROUP*2 + n + bamBytes.length, bamBytes[n]);	// head 1
				}
			}
		}
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int fileTrack, int fileSector, boolean isCopyFile, int lengthInBytes) {
		feedbackStream.append(String.format("addDirectoryEntry: \"%s\", %s, %d/%d%n", cbmFile.getName(), cbmFile.getFileType(), fileTrack, fileSector));
		if (isCpmImage()) {
			feedbackStream.append("Not yet implemented for CP/M format.\n");
			return false;
		}
		if (isCopyFile) {
			// This a substitute for setNewDirectoryEntry(thisFilename, thisFiletype, destTrack, destSector, dirPosition)
			// since we do not need to set other values than destTrack and destSector when copying a file.
			cbmFile.setTrack(fileTrack);
			cbmFile.setSector(fileSector);
		} else {
			setNewDirEntry(cbmFile, cbmFile.getName(), cbmFile.getFileType(), fileTrack, fileSector, lengthInBytes);
		}
		cbmFile.setDirTrack(0);
		cbmFile.setDirSector(-1);
		int dirEntryNumber = findFreeDirEntry();
		if (dirEntryNumber != -1 && setNewDirLocation(cbmFile, dirEntryNumber)) {
			writeSingleDirectoryEntry(cbmFile, getDirectoryEntryPosition(dirEntryNumber));
			filesUsedCount++;	// increase the maximum file numbers
			return true;
		} else {
			feedbackStream.append("Error: Could not find a free sector on track ").append(getDirBlock().track).append(" for new directory entries.\n");
			return false;
		}
	}

	/**
	 * Copy attributes of bufferCbmFile to a location in cbmDisk.
	 * @param cbmFile
	 * @param where data position where to write to cbmDisk
	 */
	private void writeSingleDirectoryEntry(CbmFile cbmFile, int where){
		if (isCpmImage()) {
			feedbackStream.append("Not yet implemented for CP/M format.\n");
		} else {
			feedbackStream.append("writeSingleDirectoryEntry: dirpos=").append(cbmFile.getDirPosition()).append('\n');
			cbmFile.toBytes(cbmDisk, where);
		}
	}

	/**
	 * Find offset to a directory entry.
	 * @param dirEntryNumber directory entry number to look up
	 * @return offset in image to directory entry, or -1 if dirEntry is not available.
	 */
	private int getDirectoryEntryPosition(int dirEntryNumber) {
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			return -1;
		}
		TrackSector ts = getDirBlock();
		int track =	ts.track;
		int sector = ts.sector;
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount && track != 0) {
			track = getCbmDiskValue(getSectorOffset(track, sector) + 0x00);
			sector = getCbmDiskValue(getSectorOffset(track, sector) + 0x01);
			entryPosCount += 8;
		}
		if (track == 0) {
			return -1;
		} else {
			return getSectorOffset(track, sector) + (dirEntryNumber & 0x07) * DIR_ENTRY_SIZE;
		}
	}

	/**
	 * Iterate directory sectors to find the specified directory entry. If needed, attempt to allocate more directory sectors
	 * and continue iterating until either directory entry is available or FILE_NUMBER_LIMIT is reached,
	 * @param cbmFile
	 * @param dirEntryNumber position where to put this entry in the directory
	 * @return returns true if a free directory block was found
	 */
	private boolean setNewDirLocation(CbmFile cbmFile, int dirEntryNumber){
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			feedbackStream.append( "Error: Invalid directory entry number ").append(dirEntryNumber).append(" at setNewDirectoryLocation.\n");
			return false;
		} else if ( (dirEntryNumber & 0x07) != 0) {
			// If this is not the eighth entry we are lucky and do not need to do anything...
			cbmFile.setDirTrack(0);
			cbmFile.setDirSector(0);
			return true;
		}
		//find the correct entry where to write new values for dirTrack and dirSector
		TrackSector ts = getDirBlock();
		int thisTrack = ts.track;
		int thisSector = ts.sector;
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount) {
			int nextTrack = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00);
			int nextSector = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01);
			if (nextTrack == 0) {
				nextTrack = thisTrack;
				boolean found = false;
				for (int i=0; !found && i<D88Constants.DIR_SECTORS.length; i++ ) {
					nextSector = D88Constants.DIR_SECTORS[i];
					found = isSectorFree(nextTrack, nextSector);
				}
				if (found) {
					nextTrack = thisTrack;
					markSectorUsed(nextTrack, nextSector);
					setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00, nextTrack);
					setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01, nextSector);
					setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x00, 0);
					setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x01, -1);
					feedbackStream.append("Allocated additonal directory sector (").append(nextTrack).append('/').append(nextSector).append(") for dir entry ").append(dirEntryNumber).append(".\n");
				} else {
					feedbackStream.append( "Error: no more directory sectors. Can't add file.\n");
					return false;
				}
			}
			thisTrack = nextTrack;
			thisSector = nextSector;
			entryPosCount += 8;
		}
		return true;
	}

	/**
	 * Find first free directory entry.
	 * Looks through the allocated directory sectors.
	 * @return number of next free directory entry, or -1 if none is free.
	 */
	private int findFreeDirEntry() {
		TrackSector ts = getDirBlock();
		int track = ts.track;
		int sector = ts.sector;
		int dirPosition = 0;
		do {
			int dataPosition = getSectorOffset(track, sector);
			for (int i = 0; i < DIR_ENTRIES_PER_SECTOR; i++) {
				int fileType = cbmDisk[dataPosition + (i * DIR_ENTRY_SIZE) + 0x02] & 0xff;
				if (fileType  == 0) {
					// Free or scratched entry
					return dirPosition;
				}
				dirPosition++;
			}
			track = getCbmDiskValue(dataPosition + 0);
			sector = getCbmDiskValue(dataPosition + 1);
		} while (track != 0);
		if (dirPosition < FILE_NUMBER_LIMIT + 2) {
			// next entry, on a new dir sector. not yet hit max number of entries.
			return dirPosition;
		} else {
			// Hit max number of file entries. can't add more.
			feedbackStream.append("Error: No free directory entry avaiable.\n");
			return -1;
		}
	}

	@Override
	public BamTrack[] getBamTable() {
		BamTrack[] bamEntry = new BamTrack[TRACK_COUNT];
		IntStream.range(0, TRACK_COUNT).forEach(trk -> {
			bamEntry[trk] = new BamTrack(trk, MAX_SECTORS + 1);
			Arrays.fill(bamEntry[trk].bam, BamState.INVALID);
		});
		for (int trk = getFirstTrack(); trk < TRACK_COUNT + getFirstTrack(); trk++) {
			for (int s=1; s<=26; s++) {
				if (trk ==0) {
					bamEntry[trk].bam[s] = BamState.RESERVED;
					bamEntry[trk].bam[s+26] = BamState.RESERVED;
				} else {
					boolean u1 = (bam.getTrackBits(trk+1, (s/8)+1) & DiskImage.BYTE_BIT_MASKS[s%8]) == 0;
					bamEntry[trk].bam[s] = u1 ? BamState.USED : BamState.FREE;

					boolean u2 = (bam.getTrackBits(trk+1, (s/8)+1+4) & DiskImage.BYTE_BIT_MASKS[s%8]) == 0;
					bamEntry[trk].bam[s+26] = u2 ? BamState.USED : BamState.FREE;
				}
			}
		}
		return bamEntry;
	}

	private int getBamTrackPos(int track) {
		if (track <= 24 ) {
			return getSectorOffset(bamSectors[0]) + track * BYTES_PER_BAM_GROUP*2 + 6;
		} else if (track <= 49) {
			return getSectorOffset(bamSectors[1]) + (track - 25) * BYTES_PER_BAM_GROUP*2 + 6;
		} else if (track <= 74) {
			return getSectorOffset(bamSectors[2]) + (track - 50) * BYTES_PER_BAM_GROUP*2 + 6;
		} else {
			return getSectorOffset(bamSectors[3]) + (track - 75) * BYTES_PER_BAM_GROUP*2 + 6;
		}
	}

	private int getSectorOffset(TrackSector ts) {
		return getSectorOffset(ts.track, ts.sector);
	}

	@Override
	public int getSectorOffset(int track, int sector) {
		if (sector > 32) {
			sector -= 6;
		}
		return (track * MAX_SECTORS + sector - 1) * BLOCK_SIZE;
	}

	@Override
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		if (isCpmImage()) {
			throw new CbmException("Delete not yet implemented for CP/M format.");
		}
		var fileType = cbmFile.getFileType();
		cbmFile.setFileType(FileType.DEL);
		cbmFile.setFileScratched(true);
		int dirEntryNumber = cbmFile.getDirPosition();
		int dirEntryPos = getDirectoryEntryPosition(dirEntryNumber);
		if (dirEntryPos != -1) {
			setCbmDiskValue(dirEntryPos + 0x02, 0);
			// Free used blocks
			freeBlocks(cbmFile.getTrack(), cbmFile.getSector());
			if (fileType == FileType.REL && cbmFile.getRelTrack() != 0) {
				freeBlocks(cbmFile.getRelTrack(), cbmFile.getRelSector());
			}
		} else {
			feedbackStream.append("Error: Failed to delete ").append(cbmFile.getName());
		}
	}

	@Override
	public Integer validate(List<ValidationError.Error> repairList) {
		return 0;
	}

	@Override
	public boolean isSectorFree(int track, int sector) {
		int pos;
		if (track == 0) {
			return false;
		} else if (sector <= 32) {
			pos = getBamTrackPos(track) + (sector / 8) + 1;
		} else {
			sector -= 32;
			pos = getBamTrackPos(track) + (sector / 8) + 1 + BYTES_PER_BAM_GROUP;
		}
		return 0 != (getCbmDiskValue(pos) & BYTE_BIT_MASKS[sector & 0x07]);
	}

	@Override
	public void markSectorFree(int track, int sector) {
		int trackPos;
		int pos;
		if (sector <= 32) {
			trackPos = getBamTrackPos(track);
			pos = (sector / 8) + 1;
		} else {
			trackPos = getBamTrackPos(track) + BYTES_PER_BAM_GROUP;
			sector -= 32;
			pos = (sector / 8) + 1;
		}
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) | BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) + 1);
	}

	@Override
	public void markSectorUsed(int track, int sector) {
		int trackPos;
		int pos;
		if (sector <= 32) {
			trackPos = getBamTrackPos(track);
			pos = (sector / 8) + 1;
		} else {
			trackPos = getBamTrackPos(track) + BYTES_PER_BAM_GROUP;
			sector -= 32;
			pos = (sector / 8) + 1;
		}
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) & INVERTED_BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) - 1);
	}

	@Override
	public int getNextSector(int track, int sector) {
		if (track < 76 && sector > 0 && (sector < 26 || (sector >32 && sector < 58))) {
			return sector + 1;
		}
		return getFirstSector();
	}

	@Override
	public TrackSector getSector(int offset) {
		if (offset < 0 || offset >= D88_SIZE) {
			return null;
		}
		int t = offset / (26 * 2 * BLOCK_SIZE);
		int s = ((offset - t * 26 * 2 * BLOCK_SIZE) / BLOCK_SIZE) + 1;
		if (s > 26) {
			s += 6;
		}
		return new TrackSector(t,s);
	}

}
