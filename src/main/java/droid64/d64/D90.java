package droid64.d64;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import droid64.d64.ValidationError.Error;
import droid64.gui.BAMPanel.BamState;
import droid64.gui.BAMPanel.BamTrack;
import droid64.gui.ConsoleStream;

/**
* Created on 2024-01-10
*
*   droiD64 - A graphical file manager for D64 files
*   Copyright (C) 2024 Henrik Wetterstrom
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
* @author henrik
* </pre>
*/
public class D90 extends DiskImage {

	private static final long serialVersionUID = 4778991787403388396L;
	/** Number of sectors per track (32) */
	private static final int TRACK_SECTORS	= 32;
	/** Number of tracks (153) */
	private static final int TRACK_COUNT	= 153;
	/** D9060 head count (4) */
	private static final int D9060_HEAD_COUNT = 4;
	/** D9060 head count (6) */
	private static final int D9090_HEAD_COUNT = 6;
	/** D90 D9060 size (153 * 32 * 4 * 256) */
	public static final int D9060_SIZE = 5013504;
	/** D90 D9090 size (153 * 32 * 6 * 256 : 29376 blocks) */
	public static final int D9090_SIZE = 7520256;
	
	private static final int DIR_INTERLEAVE = 3;
	private static final int DATA_INTERLEAVE = 10;
	
	/** 1 byte for free sectors on track, and one bit per sector (4 bytes / 32 bits) for each head */
	private static final int BYTES_PER_BAM_GROUP = 5;	
	/** Max number of files in D9060 image */
	protected static final int D9060_FILE_NUMBER_LIMIT = 17275;
	/** Max number of files in D9090 image */
	protected static final int D9090_FILE_NUMBER_LIMIT = 25922;
	
	/** List with track/sector to BAM sectors. Should be 20. */
	private final transient List<TrackSector> bamSectors = new ArrayList<>();

	private int headCount = 0;
	private int d90size = 0;
	
	/**
	 * Default constructor
	 *
	 * @param imageFormat
	 * @param consoleStream
	 */
	public D90(DiskImageType imageFormat, ConsoleStream consoleStream) {
		this.feedbackStream = consoleStream;
		updateImageType(imageFormat);
	}

	private void updateImageType(DiskImageType imageFormat) {
		this.imageFormat  = imageFormat;
		this.d90size = imageFormat == DiskImageType.D90_9090 ? D9090_SIZE : D9060_SIZE;
		this.headCount = imageFormat == DiskImageType.D90_9090 ? D9090_HEAD_COUNT : D9060_HEAD_COUNT;
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_GROUP * headCount - 1);		
		initCbmFile(imageFormat == DiskImageType.D90_9090 ? D9090_FILE_NUMBER_LIMIT : D9060_FILE_NUMBER_LIMIT);
	}
	
	/**
	 * Constructor
	 * @param imageData
	 * @param consoleStream
	 */
	public D90(DiskImageType imageFormat, byte[] imageData, ConsoleStream consoleStream) {
		this.feedbackStream = consoleStream;
		updateImageType(imageData.length == D9060_SIZE ? DiskImageType.D90_9060 : DiskImageType.D90_9090 );
		cbmDisk = imageData;
	}

	@Override
	public int getFirstSector() {
		return DEFAULT_ZERO;
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		return TRACK_SECTORS * headCount;
	}

	@Override
	public int getTrackCount() {
		return TRACK_COUNT;
	}

	@Override
	public int getMaxSectorCount() {
		return TRACK_SECTORS * headCount;
	}

	@Override
	public int getBlocksFree() {
		int blocksFree = 0;
		if (cbmDisk != null) {			
			for (int track = 1; track < getTrackCount(); track++) {
				blocksFree = blocksFree + bam.getFreeSectors(track+1);
			}
		}
		return blocksFree;	
	}

	@Override
	protected DiskImage readImage(File file) throws CbmException {
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_GROUP * headCount - 1);
		var dt = file.length() == D9060_SIZE ? DiskImageType.D90_9060 : DiskImageType.D90_9090;
		updateImageType(dt);
		return readImage(file, dt);
	}

	@Override
	public void readBAM() {
		getBamSectors();
		int headerOffset = getSectorOffset(getHeaderBlock());
		bam.setDiskName(Utility.getString(cbmDisk, headerOffset + 0x06, DISK_NAME_LENGTH));
		bam.setDiskId(Utility.getString(cbmDisk, headerOffset + 0x18, DISK_ID_LENGTH));
		bam.setDiskDosType(getCbmDiskValue(headerOffset + 2 ));
		
		for (int track = 0; track < TRACK_COUNT; track++) {
			try {
				int pos = getBamTrackPos(track);
				int free = 0;
				for (int h = 0; h < headCount; h++) {
					free += getCbmDiskValue(pos + BYTES_PER_BAM_GROUP * h);
					for (int i = 1; i < BYTES_PER_BAM_GROUP; i++) {					
						bam.setTrackBits(track + 1, i + (BYTES_PER_BAM_GROUP - 1) * h, getCbmDiskValue(pos + i + BYTES_PER_BAM_GROUP * h));
					}
				}
				bam.setFreeSectors(track + 1, free);
			} catch (BadSectorException ex) {
				// should not happen
				bam.setFreeSectors(track + 1, 0);				
			}
		}
		checkImageFormat();		
	}

	@Override
	public void readDirectory() {
		var dirBlock = getDirBlock();
		readDirectory(dirBlock.track, dirBlock.sector, D9090_FILE_NUMBER_LIMIT);
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("getFileData: No disk data exist.");
		} else if (number >= getCbmFileSize()) {
			throw new CbmException("getFileData: File number " + number + " does not exist.");
		} else if (isCpmImage()) {
			feedbackStream.append("getFileData: CP/M mode.\n");
			throw new CbmException("Not yet implemented for CP/M format.");
		} else if (getCbmFile(number).isFileScratched()) {
			throw new CbmException("getFileData: File number " + number + " is deleted.");
		}
		feedbackStream.append("getFileData: ").append(number).append(" '").append(getCbmFile(number).getName()).append("'\n");
		feedbackStream.append("Tracks / Sectors: ");
		return getData(getCbmFile(number).getTrack(), getCbmFile(number).getSector());
	}

	@Override
	protected TrackSector saveFileData(byte[] saveData) {
		if (isCpmImage()) {
			feedbackStream.append(NOT_IMPLEMENTED_FOR_CPM);
			return null;
		}
		int usedBlocks = 0;
		int dataRemain = saveData.length;
		feedbackStream.append("SaveFileData: ").append(dataRemain).append(" bytes ("+((dataRemain+253)/254)+" blocks) of data.\n");
		TrackSector firstBlock = findFirstCopyBlock(DATA_INTERLEAVE);
		if (firstBlock == null) {
			feedbackStream.append("\nsaveFileData: Error: No free sectors on disk. Disk is full.\n");
			return null;
		}
		var block = new TrackSector(firstBlock.track, firstBlock.sector);
		int thisTrack;
		int thisSector;
		int dataPos = 0;
		while (dataRemain >= 0 && block != null) {
			feedbackStream.append(dataRemain).append(" bytes remain: block ").append(block.track).append('/').append(block.sector).append('\n');
			thisTrack = block.track;
			thisSector = block.sector;
			markSectorUsed(thisTrack, thisSector);
			if (dataRemain >= (BLOCK_SIZE - 2)) {
				block = findNextCopyBlock(block, DATA_INTERLEAVE, false);
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
			feedbackStream.append("All data written ("+usedBlocks+" blocks).\n");
		}
		return firstBlock;
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
		var dirBlock = getDirBlock();
		feedbackStream.append("writeDirectoryEntry: bufferCbmFile to dirEntryNumber ").append(entryNum).append(".\n");
		if (entryNum > 7) {
			while (entryNum > 7) {
				int entryPos = getSectorOffset(dirBlock);
				dirBlock = new TrackSector(getCbmDiskValue(entryPos + 0),  getCbmDiskValue(entryPos + 1));
				if (dirBlock.track < 0 || dirBlock.sector < 0) {
					throw new IllegalArgumentException("ERROR: Out of dir blocks: "+dirBlock);			
				}
				feedbackStream.append("LongDirectory: ")
					.append(entryNum).append(" dirEntrys remain, next: ").append(dirBlock).append('\n');
				entryNum -= 8;
			}
		}		
		int pos = getSectorOffset(dirBlock) + entryNum * DIR_ENTRY_SIZE;
		setCbmDiskValue(pos + 0, cbmFile.getDirTrack());
		setCbmDiskValue(pos + 1, cbmFile.getDirSector());
		writeSingleDirectoryEntry(cbmFile, pos);
	}

	/**
	 * Copy attributes of bufferCbmFile to a location in cbmDisk.
	 * @param cbmFile
	 * @param where data position where to write to cbmDisk
	 */
	private void writeSingleDirectoryEntry(CbmFile cbmFile, int where){
		if (isCpmImage()) {
			feedbackStream.append("Not yet implemented for CP/M format.\n");
			return ;
		}
		feedbackStream.append("writeSingleDirectoryEntry: dirpos=").append(cbmFile.getDirPosition()).append('\n');
		cbmFile.toBytes(cbmDisk, where);
	}
	
	@Override
	public boolean saveNewImage(File file, String newDiskName, String newDiskID) {
		cbmDisk = new byte[d90size];
		Arrays.fill(cbmDisk, (byte) 0);
		int[] bamBlockTracks = {1,9,17,25,33,41,49,57,65,73,81,89,97,105,113,121,129,137,145,152};
		bamSectors.clear();
		for (int i = 0; i < bamBlockTracks.length; i++) {
			int bo = getSectorOffset(bamBlockTracks[i], 0);
			// next BAM
			setCbmDiskValue(bo + 0x00, i == bamBlockTracks.length - 1 ? 0xff: bamBlockTracks[i + 1]);
			setCbmDiskValue(bo + 0x01, i == bamBlockTracks.length - 1 ? 0xff : 0);
			// previous BAM
			setCbmDiskValue(bo + 0x02, i == 0 ? 0xff : bamBlockTracks[i - 1]);
			setCbmDiskValue(bo + 0x03, i == 0 ? 0xff : 0);	
			// min/low track
			setCbmDiskValue(bo + 0x04, i * 8);
			setCbmDiskValue(bo + 0x05, Math.min((i + 1) * 8, TRACK_COUNT));
			// BAM data
			int tc = i == bamBlockTracks.length - 1 ? 1 : 8;
			for (int b = 0; b < tc * headCount; b++) {
				// make all blocks in track/head free
				setCbmDiskValue(bo + 0x10 + b * BYTES_PER_BAM_GROUP, 0x20, 0xff, 0xff, 0xff, 0xff);
			}
			bamSectors.add(new TrackSector(bamBlockTracks[i], 0));
		}
		// Mark blocks as used
		markSectorUsed(0,0);	// config
		markSectorUsed(0,1);	// badblock
		markSectorUsed(76,10);	// dir
		markSectorUsed(76,20);	// header
		for (int t : bamBlockTracks) {
			markSectorUsed(t, 0);			
		}
		// BadBlock
		Arrays.fill(cbmDisk, BLOCK_SIZE * 1, BLOCK_SIZE * 2, (byte)0xff);
		// ConfigBlock
		setCbmDiskValue(0, 0x00, 0x01, 0x00, 0xff, 76, 10, 76, 20, 1, 0, 0x39, 0x30);
		// first dirBlock has no next
		setCbmDiskValue(getSectorOffset(76, 10) + 0x01, 0xff);
		// headerBlock
		int hdrPos = getSectorOffset(76, 20);
		setCbmDiskValue(hdrPos + 0x00, 76, 10);
		Arrays.fill(cbmDisk, hdrPos + 0x06, hdrPos + 0x21, (byte)0xa0);
		setCbmDiskValue(hdrPos + 0x1b, 0x33, 0x41);
		setDiskName(Utility.cbmFileName(newDiskName, DISK_NAME_LENGTH), Utility.cbmFileName(newDiskID, DISK_ID_LENGTH));

		return saveAs(file);
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int fileTrack, int fileSector, boolean isCopyFile,
			int lengthInBytes) {

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
			int entryPos = getDirectoryEntryPosition(dirEntryNumber);
			if (entryPos != -1) {
				writeSingleDirectoryEntry(cbmFile, entryPos);
				filesUsedCount++;	// increase the maximum file numbers
				return true;
			}
		} 
		feedbackStream.append("Error: Could not find a free directory sector for new directory entries.\n");
		return false;
	}

	/**
	 * Find offset to a directory entry.
	 * @param dirEntryNumber directory entry number to look up
	 * @return offset in image to directory entry, or -1 if dirEntry is not available.
	 */
	private int getDirectoryEntryPosition(int dirEntryNumber) {
		if (dirEntryNumber < 0 || dirEntryNumber >= D9090_FILE_NUMBER_LIMIT) {
			return -1;
		}
		var dirBlock = getDirBlock();
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount && dirBlock.sector != 0xff) {
			int entryPos = getSectorOffset(dirBlock);
			dirBlock = new TrackSector(getCbmDiskValue(entryPos + 0x00),  getCbmDiskValue(entryPos + 0x01));
			entryPosCount += 8;
		}
		if (dirBlock.sector == 0xff) {
			return -1;
		} else {
			return getSectorOffset(dirBlock) + (dirEntryNumber & 0x07) * DIR_ENTRY_SIZE;
		}
	}

	/**
	 * Iterate directory sectors to find the specified directory entry. If needed, attempt to allocate more directory sectors
	 * and continue iterating until either directory entry is available or FILE_NUMBER_LIMIT is reached,
	 * globals written: bufferCbmFile<BR>
	 * @param dirEntryNumber position where to put this entry in the directory
	 * @return returns true if a free directory block was found
	 */
	private boolean setNewDirLocation(CbmFile cbmFile, int dirEntryNumber){
		if (dirEntryNumber < 0 || dirEntryNumber >= D9090_FILE_NUMBER_LIMIT) {
			feedbackStream.append( "Error: Invalid directory entry number ").append(dirEntryNumber).append(" at setNewDirectoryLocation.\n");
			return false;
		} else if ( (dirEntryNumber & 0x07) != 0) {
			// If this is not the eighth entry we are lucky and do not need to do anything...
			cbmFile.setDirTrack(0);
			cbmFile.setDirSector(0);
			return true;
		}
		//find the correct entry where to write new values for dirTrack and dirSector
		var dirBlock = getDirBlock();
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount) {
			int dataPosition = getSectorOffset(dirBlock);
			var nextDirBlock = new TrackSector(getCbmDiskValue(dataPosition + 0),  getCbmDiskValue(dataPosition + 1));
			if (nextDirBlock.track == 0) {
				nextDirBlock = findNextCopyBlock(new TrackSector(dirBlock.track, dirBlock.sector), DIR_INTERLEAVE, true);
				if (nextDirBlock != null) {
					markSectorUsed(nextDirBlock.track, nextDirBlock.sector);
					setCbmDiskValue(dirBlock.track, dirBlock.sector, 0x00, nextDirBlock.track);
					setCbmDiskValue(dirBlock.track, dirBlock.sector, 0x01, nextDirBlock.sector);
					setCbmDiskValue(nextDirBlock.track, nextDirBlock.sector, 0x00, 0);
					setCbmDiskValue(nextDirBlock.track, nextDirBlock.sector, 0x01, -1);
					feedbackStream.append("Allocated additonal directory sector (").append(nextDirBlock)
						.append(") for dir entry ").append(dirEntryNumber).append(".\n");
				} else {
					feedbackStream.append( "Error: no more directory sectors. Can't add file.\n");
					return false;
				}
			}
			dirBlock = nextDirBlock;
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
		var dirBlock = getDirBlock();
		int dirPosition = 0;
		do {
			int dataPosition = getSectorOffset(dirBlock);
			for (int i = 0; i < DIR_ENTRIES_PER_SECTOR; i++) {
				int fileType = cbmDisk[dataPosition + (i * DIR_ENTRY_SIZE) + 0x02] & 0xff;
				if (fileType  == 0) {
					// Free or scratched entry
					return dirPosition;
				}
				dirPosition++;
			}
			dirBlock = new TrackSector(getCbmDiskValue(dataPosition + 0x00),  getCbmDiskValue(dataPosition + 0x01));
		} while (dirBlock.track != 0);
		if (dirPosition < D9090_FILE_NUMBER_LIMIT + 2) {
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
			bamEntry[trk] = new BamTrack(trk + getFirstTrack(), TRACK_SECTORS * headCount + 1);
			Arrays.fill(bamEntry[trk].bam, BamState.INVALID);
		});		
		for (int trk = 0; trk < TRACK_COUNT; trk++) {
			for (int h = 0; h < headCount ; h++) {
				int sec = getFirstSector() + h * TRACK_SECTORS;
				for (int cnt = 1; cnt < BYTES_PER_BAM_GROUP; cnt++) {
					for (int bit = 0; bit < 8; bit++) {
						if (sec < getMaxSectors(trk)) {
							setBamSector(bamEntry, trk, sec+1, cnt + h*4, bit);
							sec++;
						}
					}
				}
			}
		}
		return bamEntry;
	}

	private void setBamSector(BamTrack[] bamEntry, int trk, int sec, int bamByteNum, int bitNum) {
		if ((getBam().getTrackBits(trk+1, bamByteNum) & DiskImage.BYTE_BIT_MASKS[bitNum]) == 0) {
			bamEntry[trk].bam[sec] = BamState.USED;
		} else {
			bamEntry[trk].bam[sec] = BamState.FREE;
		}
	}
	
	@Override
	public int getSectorOffset(int track, int sector) {
		return (track * TRACK_SECTORS * headCount + sector ) * BLOCK_SIZE;
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
	public Integer validate(List<Error> repairList) {
		getValidationErrorList().clear();

		// reset with nulls
		var bamEntry = new Boolean[getTrackCount() + 1][getMaxSectorCount()];
		for (int trk = 0; trk < bamEntry.length; trk++) {
			Arrays.fill(bamEntry[trk], null);
		}

		errors = 0;
		warnings = 0;

		// read all the chains of BAM/directory blocks. Mark each block as used and also check that
		// the block is not already marked as used. It would mean a block is referred to twice.
		// first check the chain of directory blocks.
		var dirBlock = getDirBlock();
		var dirErrorList = new ArrayList<TrackSector>();
		do {
			int dirBlkPos = getSectorOffset(dirBlock);
			if (errors > 1000) {
				getValidationErrorList().add(ValidationError.Error.ERROR_TOO_MANY.getError(dirBlock));
				break;
			} else if (dirBlock.track >= bamEntry.length || dirBlock.sector >= bamEntry[dirBlock.track].length) {
				getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_OUTSIDE_IMAGE.getError(dirBlock));
				errors++;
				break;
			} else if (bamEntry[dirBlock.track][dirBlock.sector] == null) {
				bamEntry[dirBlock.track][dirBlock.sector] = Boolean.FALSE;
			} else {
				errors++;
				var thisBlock = new TrackSector(dirBlock);
				if (dirErrorList.contains(thisBlock)) {
					getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_SEEN.getError(dirBlock));
					break;
				} else {
					dirErrorList.add(thisBlock);
				}
				if (bamEntry[dirBlock.track][dirBlock.sector].equals(Boolean.FALSE)) {
					getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_USED.getError(dirBlock));
				} else {
					getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_FREE.getError(dirBlock));
				}
			}
			dirBlock.track = getCbmDiskValue(dirBlkPos + 0x00);
			dirBlock.sector = getCbmDiskValue(dirBlkPos + 0x01);
		} while (dirBlock.track != 0x00);

		// follow each file and check data blocks
		for (int n=0; n < getCbmFileSize(); n++) {
			var cf = getCbmFile(n);
			var startBlock = new TrackSector(cf.getTrack(), cf.getSector());
			if (cf.getFileType() == FileType.CBM) {
				getValidationErrorList().add(ValidationError.Error.ERROR_PARTITIONS_UNSUPPORTED.getError(startBlock, cf.getName()));
				errors++;
			} else if (cf.getFileType() != FileType.DEL&&startBlock.track != 0) {
					validateFileData(startBlock, bamEntry, n);
					if (cf.getFileType() == FileType.REL && cf.getRelTrack() != 0) {
						// Follow REL file side sectors
						validateFileData(new TrackSector(cf.getRelTrack(), cf.getRelSector()), bamEntry, n);
					}
			}
		}

		// iterate BAM and verify used blocks is matching what we got when following data chains above.
		List<TrackSector> bamList = getBamSectors();
		for (int trk = 1; trk < getTrackCount(); trk++) {
			for (int sec = 0; sec < getMaxSectors(trk); sec++) {
				var block = new TrackSector(trk, sec);
				var bamFree = Boolean.valueOf(isSectorFree(block));
				var fileFree = bamEntry[trk][sec];
				if (fileFree == null && bamFree || bamFree.equals(fileFree)) {
					// no action
				} else if (Boolean.FALSE.equals(fileFree) && !Boolean.FALSE.equals(bamFree)) {
					if (repairList != null && repairList.contains(ValidationError.Error.ERROR_USED_SECTOR_IS_FREE)) {
						markSectorUsed(block);
						feedbackStream.append("Info: marked sector ").append(block).append(" as used.\n");
					} else {
						getValidationErrorList().add(ValidationError.Error.ERROR_USED_SECTOR_IS_FREE.getError(block));
						errors++;
					}
				} else if (trk != 0 && trk != 0xff){
					if (repairList != null && repairList.contains(ValidationError.Error.ERROR_UNUSED_SECTOR_IS_ALLOCATED)) {
						markSectorFree(block);
						feedbackStream.append("Info: marked sector ").append(block).append(" as free.\n");
					} else {
						if (!bamList.contains(block) && !block.equals(getHeaderBlock())) {
							getValidationErrorList().add(ValidationError.Error.ERROR_UNUSED_SECTOR_IS_ALLOCATED.getError(block));
							warnings++;
						}
					}
				}
			}
		}
		return errors + warnings;
	}

	private void validateFileData(TrackSector startBlock, Boolean[][] bamEntry, int fileNum) {
		int track = startBlock.track;
		int sector = startBlock.sector;
		List<TrackSector> fileErrorList = new ArrayList<>();
		do {
			if (errors > 1000) {
				getValidationErrorList().add(ValidationError.Error.ERROR_TOO_MANY.getError(track, sector));
				return;
			} else if (track >= bamEntry.length || sector >= bamEntry[track].length) {
				getValidationErrorList().add(ValidationError.Error.ERROR_FILE_SECTOR_OUTSIDE_IMAGE.getError(track, sector, getCbmFile(fileNum).getName()));
				errors++;
				return;
			} else if (bamEntry[track][sector] == null) {
				bamEntry[track][sector] = Boolean.FALSE;	// OK
			} else {
				errors++;
				// Detect cyclic references by keeping track of all sectors used by one file and check if a sector is already seen.
				var thisBlock = new TrackSector(track, sector);
				if (fileErrorList.contains(thisBlock)) {
					getValidationErrorList().add(ValidationError.Error.ERROR_FILE_SECTOR_ALREADY_SEEN.getError(track, sector, getCbmFile(fileNum).getName()));
					return;
				} else {
					fileErrorList.add(thisBlock);
				}
				if (bamEntry[track][sector].equals(Boolean.FALSE)) {
					getValidationErrorList().add(ValidationError.Error.ERROR_FILE_SECTOR_ALREADY_USED.getError(track, sector));
				} else {
					getValidationErrorList().add(ValidationError.Error.ERROR_FILE_SECTOR_ALREADY_FREE.getError(track, sector));
				}
			}
			int tmpTrack = track;
			int tmpSector = sector;
			track = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x00);
			sector = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x01);
		} while (track != 0);
	}
	
	@Override
	public boolean isSectorFree(int track, int sector) {
		try {
			int trackPos = getBamTrackHeadPos(track, sector);
			int pos = ((sector % TRACK_SECTORS) / 8) + 1;		
			int value = getCbmDiskValue(trackPos + pos) & BYTE_BIT_MASKS[(sector % TRACK_SECTORS)  & 0x07];		
			return value != 0;
		} catch (BadSectorException ex) {
			return false;
		}
	}

	@Override
	public void markSectorFree(int track, int sector) {
		try {
			int trackPos = getBamTrackHeadPos(track, sector);
			int pos = ((sector % TRACK_SECTORS) / 8) + 1;
			setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) | BYTE_BIT_MASKS[(sector % TRACK_SECTORS)  & 0x07] );
			setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) + 1);
		} catch (BadSectorException ex) {
			//ignore
		}
	}

	@Override
	public void markSectorUsed(int track, int sector) {
		try {
			int trackPos = getBamTrackHeadPos(track, sector);
			int pos = ((sector % TRACK_SECTORS) / 8) + 1;
			setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) & INVERTED_BYTE_BIT_MASKS[(sector % TRACK_SECTORS)  & 0x07] );
			setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) - 1);
		} catch (BadSectorException ex) {
			//ignore
		}
	}

	@Override
	public int getNextSector(int track, int sector) {
		if (track < getTrackCount() && sector < TRACK_SECTORS * headCount) {
			return sector + 1;
		}
		return getFirstSector();
	}

	@Override
	public TrackSector getSector(int offset) {
		int t = offset / (TRACK_SECTORS * headCount * BLOCK_SIZE);
		int s = ((offset - t * TRACK_SECTORS * headCount * BLOCK_SIZE) / BLOCK_SIZE);
		return new TrackSector(t, s);
	}

	private List<TrackSector> getBamSectors() {
		try {
			bamSectors.clear();
			var block = new TrackSector(cbmDisk[0x08] & 0xff, cbmDisk[0x09] & 0xff);
			while (block.track >= 0 && block.sector != 0xff) {
				if (bamSectors.contains(block)) {
					throw new BadSectorException("Cyclic BAM sector", block);
				}
				bamSectors.add(block);
				int offset = getSectorOffset(block);
				block = new TrackSector(cbmDisk[offset + 0x00] & 0xff, cbmDisk[offset + 0x01] & 0xff);			
			}
		} catch (IndexOutOfBoundsException | BadSectorException e) {
			// ignore
			feedbackStream.append("Error:" + e.getMessage());
		}
		return bamSectors;
	}
	
	private TrackSector getHeaderBlock() {
		return new TrackSector(cbmDisk[0x06] & 0xff, cbmDisk[0x07] & 0xff);
	}

	private int getSectorOffset(TrackSector ts) {
		return getSectorOffset(ts.track, ts.sector);
	}

	@Override
	public int getFirstTrack() {
		return DEFAULT_ZERO;
	}
	
	@Override
	public String getSectorTitle(int i) {
		return Integer.toString(i-1);
	}
	
	@Override
	public void verifyTrackSector(int track, int sector) throws CbmException {
		if (track < 0 || track > getTrackCount()) {
			throw new CbmException("Track " + track + " is not valid.");
		} else if (sector < 0 || sector >= getMaxSectors(track)) {
			throw new BadSectorException("Invalid sector: ", track, sector);
		}
	}
	
	private int getBamTrackPos(int track) throws BadSectorException {
		for (var b : bamSectors) {
			int o = getSectorOffset(b);
			int p0 = cbmDisk[o + 0x04] & 0xff;
			int p1 = cbmDisk[o + 0x05] & 0xff;
			if (track >= p0 && track < p1) {
				int x = track - p0;				
				return o + 0x10 + x * BYTES_PER_BAM_GROUP * headCount;
			}			
		}
		throw new BadSectorException("", track, 0);
	}
	
	private int getBamTrackHeadPos(int track, int sector) throws BadSectorException {
		return getBamTrackPos(track) + (sector/TRACK_SECTORS) * BYTES_PER_BAM_GROUP;
	}
	
	
	@Override
	public String toString() {
		var buf = new StringBuilder()
		.append("D90[")
		.append(" compressed=").append(compressed)
		.append(" imageFormat=").append(imageFormat)
		.append(" blocksFree=").append(getBlocksFree())
		.append(" cbmFile=[");
		for (int i=0; i<getCbmFileSize() && i<filesUsedCount; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(this.getCbmFile(i));
		}
		buf.append(']')
		.append(" filesUsedCount=").append(filesUsedCount)
		.append(']');
		return buf.toString();
	}
	
	private TrackSector getDirBlock() {
		return new TrackSector(cbmDisk[0x04] & 0xff, cbmDisk[0x05] & 0xff);
	}
	
	/**
	 * Find a sector for the first block of the file,
	 * @return track/sector or null if none is available.
	 */
	private TrackSector findFirstCopyBlock(int interleave) {
		final int dirTrack = getDirBlock().track;
		var block = new TrackSector(0, 0);
		if (geosFormat) {
			// GEOS formatted disk, so use the other routine, from track one upwards.
			block.track = 1;
			block.sector = 0;
			block = findNextCopyBlock(block, interleave, false);
		} else {
			boolean found = false;	// No free sector found yet			
			int distance = 1;		// On a normal disk, start looking checking the tracks just besides the directory track.
			while (!found && distance < 128) {
				// Search until we find a track with free blocks or move too far from the directory track.
				block.track = dirTrack - distance;
				if (block.track >= 1 && block.track < TRACK_COUNT && block.track != dirTrack) {
					// Check track below the directory track
					found = isTrackFree(block.track + 1);
				}
				if (!found) {
					block.track = dirTrack + distance;
					if (block.track < TRACK_COUNT && block.track != dirTrack) {
						// Check track above the directory track
						found = isTrackFree(block.track + 1);
					}
				}
				distance++;	// Step further away from the directory track and try again.
			}
			if (found) {
				// Found track with at least one free sector. Find a free sector in it.
				final int maxSector = TRACK_SECTORS * headCount;	// Determine how many sectors there are on that track.
				block.sector = 0;									// Start off with sector zero.
				do {
					found = isSectorFree(block.track, block.sector);
					if (!found) {
						block.sector++;	// Try next sector.
					}
				} while (!found && block.sector < maxSector);	// Repeat until there is a free sector or run off the track.			
			}
			if (!found) {
				// no track with free sectors, or the track which should have a free sector didn't.
				block = null;
			}
		}
		if (block != null) {
			feedbackStream.append("firstCopyBlock: The first block will be ").append(block).append("\n");
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
	private TrackSector findNextCopyBlock(TrackSector block, int interleave, boolean allowDirTrack) {
		
		final int dirTrack = getDirBlock().track;
		
		boolean found ;
		if ((block.track == 0) || (block.track > TRACK_COUNT)) {
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
					block.sector = block.sector + interleave;	// Move away an "interleave" number of sectors.
					if (geosFormat && block.track >= 25) {
						// Empirical GEOS optimization, get one sector backwards if over track 25.
						block.sector--;
					}
				} else {
					// For a different track of a GEOS-formatted disk, use sector skew.
					block.sector = (block.track - curTrack) << 1 + 4 + interleave;
				}
				final int maxSector = TRACK_SECTORS * headCount;	// Get the number of sectors on the current track.
				while (block.sector >= maxSector) {
					// If we ran off the track then correct the result.
					block.sector = (block.sector - maxSector) + 1;	// Subtract the number of sectors on the track.
					if (block.sector > 0 && !geosFormat) {
						// Empirical optimization, get one sector backwards if beyond sector zero.
						block.sector--;
					}
				}
				int curSector = block.sector;	// Remember the first sector to be checked.
				do {
					found = isSectorFree(block.track, block.sector);
					if (!found) {
						block.sector++;	// Try next sector
					}
					if (block.sector >= maxSector) {
						block.sector = 0;	// Went off track, wrap around to sector 0.
					}
				} while (!found && block.sector != curSector);	// Continue until finding a free sector, or we are back on the curSector again.
				if (!found) {
					// According to the free sector counter in BAM, this track should have free sectors, but it didn't.
					// Try a different track. Obviously, this disk needs to be validated.
					feedbackStream.append("Warning: Track ").append(block.track).append(" should have at least one free sector, but didn't.");
					if (block.track > 1 && block.track <= dirTrack) {
						block.track = block.track - 1 ;
					} else if (block.track < TRACK_COUNT && block.track > dirTrack) {
						block.track = block.track + 1 ;
						if (block.track == dirTrack) {
							block.track = block.track + 1 ;
						}
					} else {
						tries--;
					}
				}
			} else {
				if (block.track == dirTrack && !allowDirTrack) {
					// If we already tried the directory track then there are no more tries.
					tries = 0;
					feedbackStream.append("Already tried dir track "+dirTrack+"\n");
				} else {
					if (block.track < dirTrack) {
						block.track --;	//If we're below the directory track then move one track downwards.
						if (block.track < 1) {
							block.track = dirTrack + 1; //If we ran off the disk then step back to the track just above the directory track and zero the sector number.
							block.sector = 0;
							//If there are no tracks available above the directory track then there are no tries left; otherwise just decrease the number of tries.
							if (block.track <= TRACK_COUNT) {
								tries--;
							} else {
								tries = 0;
							}
						}
					} else {
						block.track++;	//If we're above the directory track then move one track upwards.
						if (block.track == dirTrack) {
							block.track++;
						}
						if (block.track > TRACK_COUNT) {
							block.track = dirTrack - 1;	//If we ran off the disk then step back to the track just below the directory track and zero the sector number.
							block.sector = 0;
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
}
