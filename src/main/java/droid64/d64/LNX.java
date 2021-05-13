package droid64.d64;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import droid64.gui.BAMPanel.BamTrack;
import droid64.gui.ConsoleStream;

/**
 * <p> Created on 2015-Oct-15 </p>
 * <p>  droiD64 - A graphical file manager for D64 files<br>
 * Copyright (C) 2016 Henrik Wetterström </p>
 * <p> LNX file structure:</p>
 * <pre>
 * [BASIC program] \0\0\0 \r\s [numDirBlocks] \s\s [lnxName] \r\s [numDirEntries] \s\r
 * [fileName1] \r\s [numBlocks1] \s\r [fileType1] \r\s [lsu1] \s\r
 * [fileName2] \r\s [numBlocks2] \s\r [fileType2] \r\s [lsu2] \s\r
 * ... <br>
 * [fileNameN] \r\s [numBlocksN] \s\r [fileTypeN] \r\s [lsuN] \s\r
 * [padding up to numDirBlocks * 254]
 * [first data blocks without track/sector bytes. I.e. 254 bytes. ]
 * ...
 * [last data blocks without track/sector bytes. I.e. 254 bytes. ]
 * </pre>
 */
public class LNX extends DiskImage {

	private static final long serialVersionUID = 1L;
	/** All LNX blocks are 254 bytes. No track/sector byte first in each block.*/
	private static final int LNX_BLOCK_SIZE = 254;
	/** The CR delimiter used in the LNX directory structure. */
	private static final int LNX_MARK = 0x0d;

	public LNX(ConsoleStream consoleStream) {
		this.feedbackStream = consoleStream;
		bam = new CbmBam(0, 1);
	}

	public LNX(byte[] imageData, ConsoleStream consoleStream) {
		this.feedbackStream = consoleStream;
		cbmDisk = imageData;
		bam = new CbmBam(0, 1);
	}

	@Override
	public byte[] getBlock(int track, int sector) throws CbmException {
		return new byte[0];
	}

	@Override
	protected DiskImage readImage(File file) throws CbmException {
		bam = new CbmBam(1, 1);
		return readImage(file, DiskImageType.LNX);
	}

	@Override
	public void readBAM() {
		checkImageFormat();
	}

	@Override
	public void readDirectory() {
		int pos = 0;
		int count = 0;
		int num = 0;
		try {
			// Look for 0x00,0x00,0x00 after end of BASIC
			while (count != 3 && pos < cbmDisk.length) {
				if ((cbmDisk[pos++]& 0xff) == 0x00) {
					count++;
				}
			}
			if (count == 3 && (pos + 1) < cbmDisk.length && (cbmDisk[++pos] & 0xff) == LNX_MARK) {
				// Found end of basic.
				// Get numDirBlocks and lnxName string
				int end = findNextMark(++pos);
				if (end < 0) {
					feedbackStream.append("Failed to find number of dir entries.\n");
					return;
				}
				// numDirBlocks & lnxName
				String str = new String(Arrays.copyOfRange(cbmDisk, pos, end));
				String[] dirItems = str.trim().split(" +", 2);
				int numDirBlocks = Integer.parseInt(dirItems[0]);
				String diskName = dirItems.length > 1 ? dirItems[1] : Utility.EMPTY;
				int dataStart = numDirBlocks * LNX_BLOCK_SIZE;
				setDiskName(diskName, Integer.toString(numDirBlocks));
				// fileCount
				pos = end + 1;
				end = findNextMark(pos);
				int fileCount = getInt(pos, end);
				cbmFile = new CbmFile[fileCount];
				filesUsedCount = fileCount;
				// Get file entries
				int dataPos = dataStart;
				for (num = 0 ; num < fileCount && pos < cbmDisk.length; num++) {
					// file name
					pos = end + 1;
					end = findNextMark(pos);
					String name = Utility.getPetsciiString(cbmDisk, pos, end);
					// size in blocks
					pos = end + 1;
					end = findNextMark(pos);
					int size = getInt(pos, end);
					// file type
					pos = end + 1;
					end = findNextMark(pos);
					String ftype = Utility.getPetsciiString(cbmDisk, pos, end);
					// Get LSU (last sector usage, i.e. the number of bytes used in last block + 1).
					pos = end + 1;
					end = findNextMark(pos);
					int lsu = getInt(pos, end);
					// Create entry
					storeFileEntry(num, dataPos, name, size, ftype, lsu);
					// Point to first block of data for next entry
					dataPos += size * LNX_BLOCK_SIZE;
				}
			} else {
				feedbackStream.append("Error: No end of BASIC header found.\n");
			}
		} catch (NumberFormatException | ParseException e) {	//NOSONAR
			feedbackStream.append("Error: Failed to parse LNX file ").append(num+1).append(" at index 0x").append(Integer.toHexString(pos)).append(".\n").append(e.getMessage()).append('\n');
		}
	}

	private void storeFileEntry(int num, int dataPos, String name, int size, String ftype, int lsu) {
		var cf = new CbmFile();
		cf.setDirPosition(num);
		cf.setOffSet(dataPos);
		cf.setName(name);
		cf.setSizeInBlocks(size);
		cf.setFileType(getTypeFromLnxType(ftype));
		cf.setSizeInBytes(cf.getSizeInBlocks() * LNX_BLOCK_SIZE - (lsu > 0 ?  LNX_BLOCK_SIZE - lsu + 1 : 0));
		cf.setFileScratched(false);
		cf.setFileLocked(false);
		cf.setFileClosed(true);
		cf.setLsu(lsu);
		cbmFile[num] = cf;
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (cbmFile != null && number < cbmFile.length) {
			CbmFile cf = cbmFile[number];
			return Arrays.copyOfRange(cbmDisk, cf.getOffSet(), cf.getOffSet() + cf.getSizeInBytes());
		} else {
			throw new CbmException("LNX file number "+number+" does not exist.");
		}
	}

	@Override
	protected TrackSector saveFileData(byte[] saveData) {
		return null;
	}

	@Override
	protected void setDiskName(String newDiskName, String newDiskID) {
		bam.setDiskName(newDiskName);
		bam.setDiskId(newDiskID);
	}

	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {
		// Save not implemented
	}

	@Override
	public boolean saveNewImage(File file, String newDiskName, String newDiskID) {
		return false;
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes) {
		return false;
	}

	@Override
	public BamTrack[] getBamTable() {
		return new BamTrack[0];
	}

	@Override
	public int getSectorOffset(int track, int sector) {
		return 0;
	}

	@Override
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		// Delete not implemented
	}

	@Override
	public Integer validate(List<ValidationError.Error> repairList) {
		return null;
	}

	@Override
	public boolean isSectorFree(int track, int sector) {
		return false;
	}

	@Override
	public void markSectorFree(int track, int sector) {
		// Save not implemented
	}

	@Override
	public void markSectorUsed(int track, int sector) {
		// Save not implemented
	}

	/**
	 * Find next <code>LNX_MARK</code> in LNX file, on or after <code>start</code>.
	 * @param start int
	 * @return position of next <code>LNX_MARK</code> or -1 if none was found.
	 */
	private int findNextMark(int start) {
		for (int i=start; i<cbmDisk.length; i++) {
			if ((cbmDisk[i]&0xff) == LNX_MARK) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get our standard file type out of the ones used in LNX archives.
	 * @param lnxType
	 * @return DiskImage.TYPE_&lt;<i>fileType</i>&gt;
	 */
	private FileType getTypeFromLnxType(String lnxType) {
		switch (lnxType) {
		case "D": return FileType.DEL;
		case "R": return FileType.REL;
		case "S": return FileType.SEQ;
		case "U": return FileType.USR;
		default:  return FileType.PRG;
		}
	}

	/**
	 * Get integer value stored between <code>start</code> and <code>end</code>.
	 * @param start
	 * @param end
	 * @return int
	 * @throws NumberFormatException of <code>start</code> or <code>end</code> is not in LNX file.
	 */
	private int getInt(int start, int end) {
		if (end > start && end < cbmDisk.length) {
			String str = new String(Arrays.copyOfRange(cbmDisk, start, end)).trim();
			if (str.isEmpty()) {
				feedbackStream.append("Warning: Tried parsing empty string to integer at 0x").append(start).append('\n');
				return 0;
			} else {
				try {
					return Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Failed to parse int at 0x"+Integer.toHexString(start)+" ("+str+").");
				}
			}
		} else {
			throw new NumberFormatException("Failed to parse int at 0x"+Integer.toHexString(start)+" (no string).");
		}
	}

	@Override
	public int getNextSector(int track, int sector) {
		return sector;
	}

	@Override
	public TrackSector getSector(int offset) {
		return null;
	}

	@Override
	public int getBlocksFree() {
		return DEFAULT_ZERO;
	}

	@Override
	public int getFirstSector() {
		return DEFAULT_ZERO;
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		return DEFAULT_ZERO;
	}

	@Override
	public int getTrackCount() {
		return DEFAULT_ZERO;
	}

	@Override
	public int getMaxSectorCount() {
		return DEFAULT_ZERO;
	}

}
