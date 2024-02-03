package droid64.d64;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import droid64.gui.BAMPanel.BamState;
import droid64.gui.BAMPanel.BamTrack;
import droid64.gui.ConsoleStream;

/**<pre style='font-family:sans-serif;'>
 * Created on 2015-Oct-15
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
public class D81 extends DiskImage {

	private static final long serialVersionUID = 1L;
	/** Track of disk header block */
	protected static final int HEADER_TRACK	= 40;
	/** Sector of disk header block */
	protected static final int HEADER_SECT	= 0;
	/** Max number of directory entries in image : (40 - 3) * 8 = 296 */
	protected static final int FILE_NUMBER_LIMIT = 296;
	/** The normal size of a D81 image (80 * 40  * 256) */
	private static final int D81_SIZE = 819200;
	/** Number of sectors per track (40) */
	private static final int TRACK_SECTORS	= 40;
	/** Number of tracks (80) of image */
	private static final int TRACK_COUNT	= 80;
	/** Track of BAM block 1 and BAM block 2 */
	private static final int BAM_TRACK	    = 40;
	/** Sector of BAM block 1 (40/1) */
	private static final int BAM_SECT_1	    = 1;
	/** Sector of BAM block 2 (40/2) */
	private static final int BAM_SECT_2	    = 2;
	/** Track of first directory block */
	private static final int DIR_TRACK		= 40;
	/** Sector of first directory block (40/3) */
	private static final int DIR_SECT		= 3;
	/** 1 byte for free sectors on track, and one bit per sector (5 bytes / 40 bits) */
	private static final int BYTES_PER_BAM_TRACK = 6;
	/** Blocks per CP/M allocation unit (8 * 256 = 2048). */
	private static final int BLOCKS_PER_ALLOC_UNIT = 8;
	/** Track number of first track (may be above one for sub directories on 1581 disks) */
	private static final int FIRST_TRACK    = 1;

	/** Track of currently open partition .*/
	private Integer currentPartitionTrack = null;
	/** Maps track to the CbmFile for partition. Note: partitions may contain partitions. */
	private Map<Integer,CbmFile> partMap = new HashMap<>();

	/** Constructor
	 * @param imageFormat
	 * @param consoleStream the stream for errors
	 */
	public D81(DiskImageType imageFormat, ConsoleStream consoleStream) {
		this.imageFormat  = imageFormat;
		this.feedbackStream = consoleStream;
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_TRACK);
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	public D81(DiskImageType imageFormat, byte[] imageData, ConsoleStream consoleStream) {
		this.imageFormat  = imageFormat;
		this.feedbackStream = consoleStream;
		cbmDisk = imageData;
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_TRACK);
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		return TRACK_SECTORS;
	}

	@Override
	public int getBlocksFree() {
		if (cbmDisk != null) {
			final IntStream trackStream;
			if (getCurrentPartition() != null) {
				CbmFile pf = partMap.get(getCurrentPartition());
				trackStream = IntStream.range(pf.getTrack(), (pf.getTrack() + pf.getSizeInBlocks() / TRACK_SECTORS) + 1);
			} else {
				trackStream = IntStream.range(1, getTrackCount() + 1);
			}
			return trackStream.filter(t -> t != DIR_TRACK && !partMap.containsKey(t))
					.map(bam::getFreeSectors).reduce(Integer::sum).orElse(0);
		}
		return 0;
	}

	@Override
	protected DiskImage readImage(File file) throws CbmException {
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_TRACK);
		return readImage(file, DiskImageType.D81);
	}

	@Override
	public void readBAM() {
		int headerOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);
		int bamOffset1 = getSectorOffset(BAM_TRACK, BAM_SECT_1) + 0x10;
		int bamOffset2 = getSectorOffset(BAM_TRACK, BAM_SECT_2) + 0x10;
		bam.setDiskName(Utility.EMPTY);
		bam.setDiskId(Utility.EMPTY);
		bam.setDiskDosType(getCbmDiskValue(headerOffset + 2 ));
		for (int track = 1; track <= TRACK_COUNT; track ++) {
			int bamOffset = ((track-1) < 40 ? bamOffset1 : bamOffset2 ) + ((track-1) % 40) * BYTES_PER_BAM_TRACK;
			bam.setFreeSectors(track, (byte) getCbmDiskValue(bamOffset));
			for (int cnt = 1; cnt < BYTES_PER_BAM_TRACK; cnt ++) {
				bam.setTrackBits(track, cnt, (byte) getCbmDiskValue(bamOffset + cnt));
			}
		}
		bam.setDiskName(Utility.getString(cbmDisk, headerOffset + 0x04, DISK_NAME_LENGTH));
		bam.setDiskId(Utility.getString(cbmDisk, headerOffset + 0x16, DISK_ID_LENGTH));
		checkImageFormat();
		partMap.values().stream().distinct().forEach(cf -> readBAM(cf.getTrack(), cf.getSizeInBlocks()/TRACK_SECTORS));
	}


	/**
	 * Read BAM from partition
	 * @param track
	 * @param numTracks
	 */
	private void readBAM(final int track, int numTracks) {
		feedbackStream.append("readBAM: track=").append(track).append(" numTracks=").append(numTracks).append('\n');
		final int headerOffset = getSectorOffset(track, 0);
		final int bamOffset1 = getSectorOffset(track, 1) + 0x10;
		final int bamOffset2 = getSectorOffset(track, 2) + 0x10;

		if (currentPartitionTrack == null && track == BAM_TRACK || currentPartitionTrack != null && currentPartitionTrack == track) {
			bam.setDiskName(Utility.EMPTY);
			bam.setDiskId(Utility.EMPTY);
			bam.setDiskDosType(getCbmDiskValue(headerOffset + 2 ));
			bam.setDiskName(Utility.getString(cbmDisk, headerOffset + 0x04, DISK_NAME_LENGTH));
			bam.setDiskId(Utility.getString(cbmDisk, headerOffset + 0x16, DISK_ID_LENGTH));
		}
		for (int t = track; t < track+numTracks && t <= TRACK_COUNT; t++) {
			int bamOffset = (t<=40 ? bamOffset1 : bamOffset2) + ((t % 40) - 1) * BYTES_PER_BAM_TRACK;
			bam.setFreeSectors(t, (byte) getCbmDiskValue(bamOffset));
			for (int cnt = 1; cnt < BYTES_PER_BAM_TRACK; cnt ++) {
				bam.setTrackBits(t, cnt, (byte) getCbmDiskValue(bamOffset + cnt));
			}
		}
	}

	@Override
	public BamTrack[] getBamTable() {
		BamTrack[] bamEntry = new BamTrack[TRACK_COUNT];
		IntStream.range(0, TRACK_COUNT).forEach(trk -> {
			bamEntry[trk] = new BamTrack(trk, TRACK_SECTORS + 1);
			Arrays.fill(bamEntry[trk].bam, BamState.INVALID);
		});
		for (int trk = 1; trk <= TRACK_COUNT; trk++) {
			int bitCounter = 1;
			for (int cnt = 1; cnt < BYTES_PER_BAM_TRACK; cnt++) {
				for (int bit = 0; bit < 8; bit++) {
					if (bitCounter > getMaxSectors(trk)) {
						break;
					} else if ((getBam().getTrackBits(trk, cnt) & DiskImage.BYTE_BIT_MASKS[bit]) == 0) {
						bamEntry[trk-1].bam[bitCounter++] = BamState.USED;
					} else {
						bamEntry[trk-1].bam[bitCounter++] = BamState.FREE;
					}
				}
			}
		}
		return bamEntry;
	}

	@Override
	public void readDirectory() {
		if (isCpmImage()) {
			readCpmDirectory(D81Constants.C1581_DIR_TRACK, D81Constants.C1581_DIR_SECTORS, true);
		} else {
			int dirTrack = currentPartitionTrack == null ? DIR_TRACK : currentPartitionTrack;
			readDirectory(dirTrack, DIR_SECT, false, 0);
			getFileEntries().forEach(cf ->{
				if (cf!=null && cf.getFileType() == FileType.CBM) {
					int numTrk = cf.getSizeInBlocks() / TRACK_SECTORS;
					int trk = cf.getTrack();
					if (cf.getSector() == 0 && numTrk >= 3 && (trk < BAM_TRACK && trk+numTrk < BAM_TRACK || trk > BAM_TRACK && trk+numTrk > BAM_TRACK)) {
						// partition must start on sector 0, must not cross BAM track and must be at least 3 tracks
						for (int t=trk; t < trk + numTrk; t++) {
							partMap.put(t, cf);
						}
						readBAM(trk, numTrk);
					}
				}
			});
			feedbackStream.append("partTracks: ").append(partMap.keySet().stream().sorted(Integer::compareTo).map(String::valueOf).collect(Collectors.joining(","))).append('\n');
		}
	}

	/** Read normal Commodore directory structure */
	private void readDirectory(final int dirTrack, final int dirSector, boolean isPartition, int partitionSectorCount) {
		readDirectory(dirTrack, dirSector, FILE_NUMBER_LIMIT);
		validate(dirTrack, dirSector, isPartition, partitionSectorCount);
	}

	/**
	 * Read D81 partition
	 * @param track track
	 * @param sector sector
	 * @param numBlocks number of blocks
	 * @throws CbmException in case of errors
	 */
	@Override
	public void readPartition(int track, int sector, int numBlocks) throws CbmException {
		if (sector!=0 || numBlocks/TRACK_SECTORS < 3) {
			feedbackStream.append("readPartition: No sub directory partition").append('\n');
			currentPartitionTrack = null;
			return;
		}
		int dirTrack = cbmDisk[getSectorOffset(track, sector) + 0x00] & 0x0ff;
		int dirSector = cbmDisk[getSectorOffset(track, sector) + 0x01] & 0x0ff;
		feedbackStream.append("readPartition: ").append(dirTrack).append('/').append(dirSector).append('\n');
		readDirectory(dirTrack, dirSector, true, numBlocks);
		currentPartitionTrack = track;
		readBAM(track, numBlocks / TRACK_SECTORS);
	}

	@Override
	public boolean isPartitionOpen() {
		return currentPartitionTrack!=null;
	}


	public CbmFile getPartitionFile(int track) {
		return partMap.get(track);
	}

	@Override
	public CbmFile setCurrentPartition(Integer partitionTrack) {
		if (partitionTrack == null) {
			this.currentPartitionTrack = null;
			return null;
		}
		CbmFile part = partMap.get(partitionTrack);
		if (part != null) {
			this.currentPartitionTrack = partitionTrack;
		}
		return part;
	}

	@Override
	public Integer getCurrentPartition() {
		return currentPartitionTrack;
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("getFileData: No disk data exist.");
		} else if (number >= getCbmFileSize()) {
			throw new CbmException("getFileData: File number " + number + " does not exist.");
		} else if (isCpmImage()) {
			feedbackStream.append("getFileData: CP/M mode.\n");
			if (getCbmFile(number) instanceof CpmFile) {
				CpmFile cpm = (CpmFile)getCbmFile(number);
				int dstPos = 0;
				byte[] data = new byte[ cpm.getRecordCount() * CPM_RECORD_SIZE ];
				for (Integer au : cpm.getAllocList()) {
					int srcPos;
					if (au< 195) {
						// (39 * TRACK_SECTORS * BLOCK_SIZE) / (BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE) = 195
						srcPos = au * BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE;
					} else {
						srcPos = (au * BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE) + (20*BLOCK_SIZE);
					}
					for (int j=0; j < BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE && dstPos < data.length; j++) {
						data[dstPos++] = cbmDisk[srcPos + j];
					}
				}
				return data;
			} else {
				throw new CbmException("Unknown CP/M format.");
			}
		} else if (getCbmFile(number).isFileScratched()) {
			throw new CbmException("getFileData: File number " + number + " is deleted.");
		} else if (getCbmFile(number).getFileType() == FileType.CBM) {
			int blockPos = getSectorOffset(getCbmFile(number).getTrack(), 0);
			int len = getCbmFile(number).getSizeInBlocks() * BLOCK_SIZE;
			return Arrays.copyOfRange(cbmDisk, blockPos, blockPos + len);
		}
		feedbackStream.append("getFileData: ").append(number).append(" '").append(getCbmFile(number).getName()).append("'\n");
		feedbackStream.append("Tracks / Sectors: ");

		return getData(getCbmFile(number).getTrack(), getCbmFile(number).getSector());
	}

	@Override
	protected void setDiskName(String newDiskName, String newDiskID) {
		feedbackStream.append("setDiskName('").append(newDiskName).append("', '").append(newDiskID).append("')\n");
		int bam1Offset = getSectorOffset(BAM_TRACK, BAM_SECT_1);
		int bam2Offset = getSectorOffset(BAM_TRACK, BAM_SECT_2);
		Utility.setPaddedString(cbmDisk, getSectorOffset(HEADER_TRACK, HEADER_SECT) + 0x04, newDiskName, DISK_NAME_LENGTH);
		char id0 = newDiskID.length() > 0 ? newDiskID.charAt(0) : '\u0240';
		char id1 = newDiskID.length() > 1 ? newDiskID.charAt(1) : '\u0240';
		setCbmDiskValue(bam1Offset + 0x04, id0);
		setCbmDiskValue(bam1Offset + 0x05, id1);
		setCbmDiskValue(bam2Offset + 0x04, id0);
		setCbmDiskValue(bam2Offset + 0x05, id1);
		Utility.setPaddedString(cbmDisk, getSectorOffset(HEADER_TRACK, HEADER_SECT) + 0x16, newDiskID, DISK_ID_LENGTH);
	}

	@Override
	public boolean saveNewImage(File file, String newDiskName, String newDiskID) {
		final int hdrOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);
		final int dirOffset = getSectorOffset(DIR_TRACK, DIR_SECT);
		cbmDisk = new byte[D81_SIZE];
		Arrays.fill(cbmDisk, (byte) 0);
		setCbmDiskValue( hdrOffset + 0x00,	40);
		setCbmDiskValue( hdrOffset + 0x01,	3);
		setCbmDiskValue( hdrOffset + 0x02,	0x44);
		setCbmDiskValue( hdrOffset + 0x18,	Utility.BLANK);
		setCbmDiskValue( hdrOffset + 0x19,	0x32);
		setCbmDiskValue( hdrOffset + 0x1a,	0x44);
		setCbmDiskValue( hdrOffset + 0x1b,	Utility.BLANK);
		setCbmDiskValue( hdrOffset + 0x1c,	Utility.BLANK);
		setCbmDiskValue( dirOffset + 0x01,	-1);	// next sector on first dir sector
		final int bamOffset1 = getSectorOffset(BAM_TRACK, BAM_SECT_1);
		final int bamOffset2 = getSectorOffset(BAM_TRACK, BAM_SECT_2);
		Utility.copyBytes(D81Constants.EMPTY_BAM1, cbmDisk, 0x00000, bamOffset1, D81Constants.EMPTY_BAM1.length);
		Utility.copyBytes(D81Constants.EMPTY_BAM2, cbmDisk, 0x00000, bamOffset2, D81Constants.EMPTY_BAM2.length);
		setDiskName(Utility.cbmFileName(newDiskName, DISK_NAME_LENGTH), Utility.cbmFileName(newDiskID, DISK_NAME_LENGTH));
		return saveAs(file);
	}

	@Override
	public int getSectorOffset(int track, int sector) {
		return ((track - 1) * TRACK_SECTORS + sector) * BLOCK_SIZE;
	}

	@Override
	public int getTrackCount() {
		return TRACK_COUNT;
	}

	@Override
	public int getMaxSectorCount() {
		return TRACK_SECTORS;
	}

	@Override
	public Integer validate(List<ValidationError.Error> repairList) {
		getValidationErrorList().clear();
		return validate(DIR_TRACK, DIR_SECT, false, 0);
	}

	/**
	 * Validate disk image
	 * @param dirTrack directory track
	 * @param dirSector directory sector
	 * @param isPartition if i is a C1581 partition
	 * @param partitionSectorCount size of partition is if it a partition.
	 * @return number of validation errors
	 */
	private Integer validate(final int dirTrack, final int dirSector, boolean isPartition, int partitionSectorCount) {
		feedbackStream.append("validate: D81 dirSector ").append(dirTrack).append('/').append(dirSector).append(isPartition ? " partition " : Utility.SPACE);
		// init to null
		Boolean[][] bamEntry = new Boolean[getTrackCount() + 1][getMaxSectorCount()];
		for (int trk = 0; trk < bamEntry.length; trk++) {
			Arrays.fill(bamEntry[trk], null);
		}
		// read all the chains of BAM/directory blocks. Mark each block as used and also check that
		// the block is not already marked as used. It would mean a block is referred to twice.
		// first check the chain of directory blocks.
		int sector = dirSector;
		int track = dirTrack;
		int bamTrack = dirTrack;
		errors = 0;
		warnings = 0;
		validateDirEntries(track, sector, bamEntry);
		// follow each file and check data blocks
		for (int n=0; n < getCbmFileSize(); n++) {
			var cf = getCbmFile(n);

			track = cf.getTrack();
			sector = cf.getSector();
			if (cf.getFileType() == FileType.CBM) {
				int blocks = cf.getSizeInBlocks();
				for (int i=0; i<blocks; i++) {
					if (bamEntry[track][sector] == null) {
						bamEntry[track][sector] = Boolean.FALSE;	// OK
					} else {
						getValidationErrorList().add(ValidationError.Error.ERROR_PARTITIONS_UNSUPPORTED.getError(track, sector, cf.getName()));
						errors++;
					}
					if (sector < 39) {
						sector++;
					} else {
						track++;
						sector = 0;
					}
				}
			} else if (cf.getFileType() != FileType.DEL && track != 0) {
				validateFileData(track, sector, bamEntry, n);
				if (cf.getFileType() == FileType.REL && cf.getRelTrack() != 0) {
					// Follow REL file side sectors
					validateFileData(cf.getRelTrack(), cf.getRelSector(), bamEntry, n);
				}
			}
		}
		// iterate BAM and verify used blocks is matching what we got when following data chains above.
		if (isPartition) {
			int count = 0;
			for (int trk = bamTrack; count<partitionSectorCount && trk <= getTrackCount(); trk++) {
				for (int sec = 0; count<partitionSectorCount && sec < getMaxSectors(trk); sec++, count++) {
					Boolean bamFree = Boolean.valueOf(isSectorFree(trk,sec, bamTrack, 0));
					Boolean fileFree = bamEntry[trk][sec];
					validateBam(bamFree, fileFree, trk, sec);
				}
			}
		} else {
			for (int trk = 1; trk <= getTrackCount(); trk++) {
				for (int sec = 0; sec < getMaxSectors(trk); sec++) {
					Boolean bamFree = Boolean.valueOf(isSectorFree(trk,sec));
					Boolean fileFree = bamEntry[trk][sec];
					validateBam(bamFree, fileFree, trk, sec);
				}
			}
		}
		return errors + warnings;
	}

	private void validateBam(Boolean bamFree, Boolean fileFree, int track, int sector) {
		if (fileFree == null && bamFree || bamFree.equals(fileFree)) {
			// OK
		} else if (Boolean.FALSE.equals(fileFree)) {
			getValidationErrorList().add(ValidationError.Error.ERROR_USED_SECTOR_IS_FREE.getError(track, sector));
			errors++;
		} else if (track != BAM_TRACK){
			getValidationErrorList().add(ValidationError.Error.ERROR_UNUSED_SECTOR_IS_ALLOCATED.getError(track, sector));
			warnings++;
		}
	}

	private void validateDirEntries(int dirTrack, int dirSector, Boolean[][] bamEntry) {
		int track = dirTrack;
		int sector = dirSector;
		List<TrackSector> dirErrorList = new ArrayList<>();
		do {
			if (errors > 1000) {
				getValidationErrorList().add(ValidationError.Error.ERROR_TOO_MANY.getError(track, sector));
				return;
			} else if (track >= bamEntry.length || sector >= bamEntry[track].length) {
				getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_OUTSIDE_IMAGE.getError(track, sector));
				errors++;
				return;
			} else if (bamEntry[track][sector] == null) {
				bamEntry[track][sector] = Boolean.FALSE;
			} else {
				errors++;
				// Detect cyclic references by keeping track of all sectors used by one file and check if a sector is already seen.
				TrackSector thisBlock = new TrackSector(track, sector);
				if (dirErrorList.contains(thisBlock)) {
					getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_SEEN.getError(track, sector));
					return;
				} else {
					dirErrorList.add(thisBlock);
				}

				if (bamEntry[track][sector].equals(Boolean.FALSE)) {
					getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_USED.getError(track, sector));
				} else {
					getValidationErrorList().add(ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_FREE.getError(track, sector));
				}
			}
			int tmpTrack = track;
			int tmpSector = sector;
			track = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x00);
			sector = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x01);
		} while (track != 0);
	}

	private void validateFileData(int startTrack, int startSector, Boolean[][] bamEntry, int fileNum) {
		int track = startTrack;
		int sector = startSector;
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
				TrackSector thisBlock = new TrackSector(track, sector);
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

	/**
	 * Determine if a sector is free.
	 * @param track the track number of sector to check
	 * @param sector the sector number of sector to check
	 * @return when True, the sector is free; otherwise used
	 */
	@Override
	public boolean isSectorFree(int track, int sector) {
		return isSectorFree(track, sector, currentPartitionTrack == null ? BAM_TRACK : currentPartitionTrack, track<=40 ? BAM_SECT_1 : BAM_SECT_2);
	}

	/**
	 * Determine if a sector is free<BR>
	 * @param track the track number of sector to check
	 * @param sector the sector number of sector to check
	 * @return when True, the sector is free; otherwise used
	 */
	private boolean isSectorFree(int track, int sector, int bamTrack, int bamSector) {
		int trackPos = getSectorOffset(bamTrack, bamSector) + 0x10 + ((track - 1) % 40) * BYTES_PER_BAM_TRACK + 1;
		int value = getCbmDiskValue(trackPos + (sector / 8)) & BYTE_BIT_MASKS[sector & 0x07];
		return value != 0;
	}

	/**
	 * Mark a sector in BAM as used.
	 * @param track trackNumber
	 * @param sector sectorNumber
	 */
	@Override
	public void markSectorUsed(int track, int sector) {
		if (!isTrackInCurrentPartition(track)) {
			feedbackStream.append("\nmark used in wrong BAM!\n");
			return;
		}
		final int bamTrack = currentPartitionTrack == null ? BAM_TRACK : currentPartitionTrack;
		final int trackPos;
		if (track <= 40) {
			trackPos = getSectorOffset(bamTrack, BAM_SECT_1) + BYTES_PER_BAM_TRACK * (track - 1) + 0x10;
		} else {
			trackPos = getSectorOffset(bamTrack, BAM_SECT_2) + BYTES_PER_BAM_TRACK * (track - 41) + 0x10;
		}
		int pos = (sector / 8) + 1;
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) & INVERTED_BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) - 1);
	}

	/**
	 * Mark a sector in BAM as free.
	 * @param track trackNumber
	 * @param sector sectorNumber
	 */
	@Override
	public void markSectorFree(int track, int sector) {
		if (!isTrackInCurrentPartition(track)) {
			feedbackStream.append("\n mark free in wrong BAM!\n");
			return;
		}
		final int bamTrack = currentPartitionTrack == null ? BAM_TRACK : currentPartitionTrack;
		final int trackPos;
		if (track <= 40) {
			trackPos = getSectorOffset(bamTrack, BAM_SECT_1) + BYTES_PER_BAM_TRACK * (track - 1) + 0x10;
		} else {
			trackPos = getSectorOffset(bamTrack, BAM_SECT_2) + BYTES_PER_BAM_TRACK * (track - 41) + 0x10;
		}
		int pos = (sector / 8) + 1;
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) | BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) + 1);
	}

	protected boolean isTrackInCurrentPartition(int track) {
		return currentPartitionTrack == null || Optional.ofNullable(partMap.get(track)).map(CbmFile::getTrack).filter(currentPartitionTrack::equals).isPresent();
	}


	/**
	 * Determine if there's, at least, one free sector on a track.
	 * @param trackNumber the track number of sector to check.
	 * @return when true, there is at least one free sector on the track.
	 */
	@Override
	protected boolean isTrackFree(int trackNumber) {
		readBAM();
		return bam.getFreeSectors(trackNumber) > 0;
	}

	/**
	 * Find a sector for the first block of the file,
	 * @return track/sector or null if none is available.
	 */
	private TrackSector findFirstCopyBlock() {
		TrackSector block = new TrackSector(0, 0);
		if (geosFormat) {
			// GEOS formatted disk, so use the other routine, from track one upwards.
			block.track = 1;
			block.sector = 0;
			block = findNextCopyBlock(block);
		} else if (currentPartitionTrack != null) {
			CbmFile partFile = partMap.get(currentPartitionTrack);
			if (partFile == null) {
				feedbackStream.append("findFirstCopyBlock: track " + currentPartitionTrack + " is no parttion");
				return null;
			}
			// first track in a partition is reserved for header, BAM and dir blocks. The rest are used with interleave 1.
			int lastTrack = partFile.getTrack() + partFile.getSizeInBlocks() / TRACK_SECTORS;
			feedbackStream.append("findFirstCopyBlock: in partition: " + currentPartitionTrack + "-" + lastTrack+"\n");
			for (int t=partFile.getTrack() + 1; t < lastTrack; t++) {
				for (int s=0; s<getMaxSectors(t);s++) {
					if (isSectorFree(t, s, currentPartitionTrack, t <= 40 ? 1 : 2)) {
						return new TrackSector(t,s);
					}
				}
			}
			return null;
		} else {
			boolean found = false;	// No free sector found yet
			int distance = 1;		// On a normal disk, start looking checking the tracks just besides the directory track.
			while (!found && distance < 128) {
				// Search until we find a track with free blocks or move too far from the directory track.
				block.track = BAM_TRACK - distance;
				if (block.track >= FIRST_TRACK && block.track <= TRACK_COUNT && block.track != BAM_TRACK) {
					// Track within disk limits
					found = isTrackFree(block.track);
				}
				if (!found) {
					// Check the track above the directory track
					block.track = BAM_TRACK + distance;
					if (block.track <= TRACK_COUNT && block.track != BAM_TRACK) {
						// Track within disk limits
						found = isTrackFree(block.track);
					}
				}
				if (!found) {
					// Move one track further away from the directory track and try again.
					distance++;
				}
			}
			if (found) {
				// Found a track with, at least one free sector, so search for a free sector in it.
				int maxSector = TRACK_SECTORS;		// Determine how many sectors there are on that track.
				block.sector = 0;									// Start off with sector zero.
				do {
					found = isSectorFree(block.track, block.sector);
					if (!found) {
						block.sector++;	// Try the next sector.
					}
				} while (!found && block.sector <= maxSector);	// Repeat until there is a free sector or run off the track.
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
	 * Find a sector for the next block of the file, using variables Track and Sector<BR>
	 * @param block
	 * @return sector when found, or null if no more sectors left
	 */
	private TrackSector findNextCopyBlock(TrackSector block) {
		boolean found ;
		if ((block.track == 0) || (block.track > TRACK_COUNT)) {
			// If we somehow already ran off the disk then there are no more free sectors left.
			return null;
		}
		if (currentPartitionTrack != null) {

			CbmFile cf = partMap.get(currentPartitionTrack);

			feedbackStream.append("findNextCopyBlock: in partition: "+currentPartitionTrack);

			// 1. search where track >= block.track
			for (int t=block.track; t < cf.getTrack() + cf.getSizeInBlocks() / TRACK_SECTORS; t++) {
				if (t==block.track) {
					// look for blocks in same track in a sector after block
					for (int s=block.sector+1; s < getMaxSectors(t); s++) {
						if (isSectorFree(t,s, currentPartitionTrack, t <= 40 ? 1 : 2)) {
							return new TrackSector(t,s);
						}
					}
					// look for blocks in same track in a sector before block
					for (int s = block.sector-1; s >= 0; s--) {
						if (isSectorFree(t, s, currentPartitionTrack, t <= 40 ? 1 : 2)) {
							return new TrackSector(t, s);
						}
					}
				} else {
					for (int s=0; s<getMaxSectors(t);s++) {
						if (isSectorFree(t,s, currentPartitionTrack, t <= 40 ? 1 : 2)) {
							return new TrackSector(t,s);
						}
					}
				}
			}

			// 2. search where track < block.track
			for (int t=block.track-1; t > cf.getTrack(); t--) {
				for (int s=0; s<getMaxSectors(t);s++) {
					if (isSectorFree(t,s, currentPartitionTrack, t <= 40 ? 1 : 2)) {
						return new TrackSector(t,s);
					}
				}
			}

			return null;
		}
		// No partition
		int tries = 3;			// Set the number of tries to three.
		found = false;			// We found no free sector yet.
		int curTrack = block.track;		// Remember the current track number.
		while (!found && tries > 0) {
			// Keep trying until we find a free sector or run out of tries.
			if (isTrackFree(block.track)) {

				// If there's, at least, one free sector on the track then get searching.
				if (block.track == curTrack || !geosFormat) {
					// If this is a non-GEOS disk or we're still on the same track of a GEOS-formatted disk then...
					block.sector = block.sector + C1581_INTERLEAVE;	// Move away an "interleave" number of sectors.
					if (geosFormat && block.track >= 25) {
						// Empirical GEOS optimization, get one sector backwards if over track 25.
						block.sector--;
					}
				} else {
					// For a different track of a GEOS-formatted disk, use sector skew.
					block.sector = (block.track - curTrack) << 1 + 4 + C1581_INTERLEAVE;
				}
				int maxSector = TRACK_SECTORS;	// Get the number of sectors on the current track.
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
					if (block.track > FIRST_TRACK && block.track <= BAM_TRACK) {
						block.track = block.track - 1 ;
					} else if (block.track < TRACK_COUNT && block.track > BAM_TRACK) {
						block.track = block.track + 1 ;
						if (block.track == BAM_TRACK) {
							block.track = block.track + 1 ;
						}
					} else {
						tries--;
					}
				}
			} else {
				if (block.track == DIR_TRACK) {
					// If we already tried the directory track then there are no more tries.
					tries = 0;
				} else {
					if (block.track < DIR_TRACK) {
						block.track --;	//If we're below the directory track then move one track downwards.
						if (block.track < FIRST_TRACK) {
							block.track = DIR_TRACK + 1; //If we ran off the disk then step back to the track just above the directory track and zero the sector number.
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
						if (block.track == BAM_TRACK) {
							block.track++;
						}
						if (block.track > TRACK_COUNT) {
							block.track = DIR_TRACK - 1;	//If we ran off the disk then step back to the track just below the directory track and zero the sector number.
							block.sector = 0;
							//If there are no tracks available below the directory track then there are no tries left; otherwise just decrease the number of tries.
							if (block.track >= FIRST_TRACK) {
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
	protected TrackSector saveFileData(byte[] saveData) {
		if (isCpmImage()) {
			feedbackStream.append(NOT_IMPLEMENTED_FOR_CPM);
			return null;
		}
		int usedBlocks = 0;
		int dataRemain = saveData.length;
		feedbackStream.append("SaveFileData: ").append(dataRemain).append(" bytes ("+((dataRemain+253)/254)+" blocks) of data.\n");
		TrackSector firstBlock = findFirstCopyBlock();
		if (firstBlock == null) {
			feedbackStream.append("\nsaveFileData: Error: No free sectors on disk. Disk is full.\n");
			return null;
		}
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
			feedbackStream.append("All data written ("+usedBlocks+" blocks).\n");
		}
		return firstBlock;
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int fileTrack, int fileSector, boolean isCopyFile, int lengthInBytes) {
		feedbackStream.append(String.format("addDirectoryEntry: \"%s\", %s, %d/%d%n", cbmFile.getName(), cbmFile.getFileType(), fileTrack, fileSector));
		if (isCpmImage()) {
			feedbackStream.append("Not yet implemented for CP/M format.\n");
			return false;
		} else if (isCopyFile) {
			// This a substitute for setNewDirectoryEntry(thisFilename, thisFiletype, destTrack, destSector, dirPosition)
			// since we do not need to set other values than destTrack and destSector when copying a file.
			cbmFile.setTrack(fileTrack);
			cbmFile.setSector(fileSector);
		} else {
			setNewDirEntry(cbmFile, cbmFile.getName(), cbmFile.getFileType(), fileTrack, fileSector, lengthInBytes);
		}
		cbmFile.setDirTrack(0);
		cbmFile.setDirSector(-1);
		int dirTrack = currentPartitionTrack == null ? DIR_TRACK : currentPartitionTrack;
		int dirEntryNumber = findFreeDirEntry(dirTrack, DIR_SECT);
		if (dirEntryNumber != -1 && setNewDirLocation(cbmFile, dirEntryNumber)) {
			writeSingleDirectoryEntry(cbmFile, getDirectoryEntryPosition(dirTrack, DIR_SECT, dirEntryNumber));
			filesUsedCount++;	// increase the maximum file numbers
			return true;
		} else {
			feedbackStream.append("Error: Could not find a free sector on track "+dirTrack+" for new directory entries.\n");
			return false;
		}
	}

	/**
	 * Find first free directory entry.
	 * Looks through the allocated directory sectors.
	 * @param track track of first dir block
	 * @param sector of first dir block
	 * @return number of next free directory entry, or -1 if none is free.
	 */
	private int findFreeDirEntry(int track, int sector) {
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
			track = getCbmDiskValue( dataPosition + 0);
			sector = getCbmDiskValue( dataPosition + 1);
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
		int thisTrack = currentPartitionTrack != null ? currentPartitionTrack : DIR_TRACK;
		int thisSector = DIR_SECT;
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount) {
			int nextTrack = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00);
			int nextSector = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01);
			if (nextTrack == 0) {
				nextTrack = thisTrack;
				boolean found = false;
				for (int sec=0; !found && sec < TRACK_SECTORS; sec++) {
					found = isSectorFree(nextTrack, sec);
					nextSector = sec;
				}
				if (found) {
					nextTrack = thisTrack;
					markSectorUsed(nextTrack, nextSector);
					setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00, nextTrack);
					setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01, nextSector);
					setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x00, 0);
					setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x01, -1);
					feedbackStream.append("Added another directory block ").append(nextTrack).append('/').append(nextSector).append(") for dir entry ").append(dirEntryNumber).append(".\n");
				} else {
					feedbackStream.append( "Error: No more directory sectors. Can't add file.\n");
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
	 * Find offset to a directory entry.
	 * @param track
	 * @param sector
	 * @param dirEntryNumber directory entry number to look up
	 * @return offset in image to directory entry, or -1 if dirEntry is not available.
	 */
	private int getDirectoryEntryPosition(int track, int sector, int dirEntryNumber) {
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			return -1;
		}
		int entryPosCount = DIR_ENTRIES_PER_SECTOR;
		while (dirEntryNumber >= entryPosCount && track != 0) {
			int offset = getSectorOffset(track, sector);
			track = getCbmDiskValue(offset+ 0x00);
			sector = getCbmDiskValue(offset + 0x01);
			entryPosCount += DIR_ENTRIES_PER_SECTOR;
		}
		return track == 0 ? -1 : getSectorOffset(track, sector) + (dirEntryNumber & 0x07) * DIR_ENTRY_SIZE;
	}

	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {
		feedbackStream.append("writeDirectoryEntry: cbmFile to dirEntryNumber ").append(dirEntryNumber).append('\n');
		CbmFile p = partMap.get(cbmFile.getTrack());
		int pos = getDirectoryEntryPosition(p == null ? DIR_TRACK : p.getTrack(), DIR_SECT, dirEntryNumber);
		if (pos >= 0) {
			setCbmDiskValue(pos + 0, cbmFile.getDirTrack());
			setCbmDiskValue(pos + 1, cbmFile.getDirSector());
			writeSingleDirectoryEntry(cbmFile, pos);
		} else {
			feedbackStream.append("Error: writeDirectoryEntry failed for entry ").append(dirEntryNumber).append('\n');
		}
	}

	/**
	 * Copy attributes of cbmFile to a location in cbmDisk.
	 * @param cnmFile
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
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		if (isCpmImage()) {
			throw new CbmException("Delete not yet implemented for CP/M format.");
		}
		int dirEntryNumber = cbmFile.getDirPosition();
		int dirEntryPos = getDirectoryEntryPosition(currentPartitionTrack==null ? DIR_TRACK : currentPartitionTrack, DIR_SECT, dirEntryNumber);
		if (dirEntryPos != -1) {
			FileType fileType = cbmFile.getFileType();
			cbmFile.setFileType(FileType.DEL);
			cbmFile.setFileScratched(true);
			setCbmDiskValue(dirEntryPos + 0x02, 0);
			// Free used blocks
			if (fileType == FileType.CBM) {
				int last = cbmFile.getTrack() + cbmFile.getSizeInBlocks()/TRACK_SECTORS;
				for (int trk= cbmFile.getTrack(); trk < last; trk++) {
					partMap.remove(trk);
					for (int sec = 0; sec < TRACK_SECTORS; sec++) {
						markSectorFree(trk, sec);
					}
				}
			} else {
				freeBlocks(cbmFile.getTrack(), cbmFile.getSector());
				if (fileType == FileType.REL && cbmFile.getRelTrack() != 0) {
					freeBlocks(cbmFile.getRelTrack(), cbmFile.getRelSector());
				}
			}
		} else {
			feedbackStream.append("Error: Failed to delete ").append(cbmFile.getName());
		}
	}

	@Override
	public boolean supportsDirectories() {
		return true;
	}


	@Override
	public CbmFile makedir(String dirName, int numBlocks, String diskId) throws CbmException {
		int numTracks = Math.max((numBlocks + TRACK_SECTORS - 1) / TRACK_SECTORS, 3);
		int tc = 0;
		int t0 = 1;
		int t1;
		for (t1 = t0; t1 <= TRACK_COUNT; t1++) {
			if (bam.getFreeSectors(t1) == TRACK_SECTORS && t1 != BAM_TRACK && !partMap.containsKey(t1)) {
				if (tc==0) {
					t0=t1;
				}
				feedbackStream.append("D81.makedir: free tracks: "+t0+"-"+t1+"  "+tc).append('\n');
				if (++tc == numTracks) {
					break;
				}
			} else {
				feedbackStream.append("D81.makedir: track is busy: "+t1+"  "+tc).append('\n');
				tc = 0;
			}
		}
		if (tc != numTracks) {
			throw new CbmException("Not enough free space for partiton");
		}
		feedbackStream.append("D81.makedir: track ").append(t0).append('-').append(t1).append('\n');
		CbmFile cbmFile = new CbmFile();
		cbmFile.setName(dirName);
		cbmFile.setNameAsBytes(dirName.getBytes());
		cbmFile.setFileType(FileType.CBM);
		cbmFile.setSizeInBlocks(numTracks * TRACK_SECTORS);
		addDirectoryEntry(cbmFile, t0, 0, false, numTracks * TRACK_SECTORS * 254);
		feedbackStream.append("D81.makedir: mark used tracks: " + t0 + "-" + t1 + "\n");
		for (int t=t0; t <= t1; t++) {
			partMap.put(t, cbmFile);
			bam.setFreeSectors(t, 0);
			for (int s=0; s < TRACK_SECTORS; s++) {
				markSectorUsed(t, s);
			}
		}
		formatPartition(t0, 0, numBlocks, t1 - t0 + 1,  dirName, diskId);
		return cbmFile;
	}

	private void formatPartition(final int track, final int sector, int numBlocks, int numTracks, String partName, String partId) {
		int  p0 = getSectorOffset(track, sector + 0); // header
		int  p1 = getSectorOffset(track, sector + 1); // bam1
		int  p2 = getSectorOffset(track, sector + 2); // bam2
		int  p3 = getSectorOffset(track, sector + 3); // first dir
		Arrays.fill(cbmDisk, p0, p0 + numBlocks * BLOCK_SIZE, (byte) 0);
		setCbmDiskValue(p0 + 0x00, track, sector+3, 0x44, 0x00);
		setCbmDiskValue(p0 + 0x18, 0xa0, 0x33, 0x44, 0xa0, 0xa0);
		Utility.setPaddedString(cbmDisk, p0 + 0x04, partName, DISK_NAME_LENGTH);
		Utility.setPaddedString(cbmDisk, p0 + 0x16, partId, 2);
		// BAM1
		Utility.copyBytes(D81Constants.EMPTY_BAM1, cbmDisk, 0, p1, 16);
		for (int t=1; t<=TRACK_COUNT; t++) {
			int bamIdx = (t <= 40 ? p1 : p2) + 0x10 + ( (t % 40) - 1 ) * 6;
			if (t == track) {
				setCbmDiskValue(bamIdx, 0x24, 0xf0, 0xff, 0xff, 0xff, 0xff); // 40 - header+bam1+bam2+dir
			} else if (t > track && t < track+numTracks) {
				setCbmDiskValue(bamIdx, 0x28, 0xff, 0xff, 0xff, 0xff, 0xff); // free
			} else {
				setCbmDiskValue(bamIdx, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00); // used
			}
		}
		setCbmDiskValue(p1 + 0x00, track, sector+2);
		Utility.setPaddedString(cbmDisk, p1 + 0x04, partId, 2);
		Utility.setPaddedString(cbmDisk, p2 + 0x04, partId, 2);
		// BAM2
		setCbmDiskValue(p2 + 0x00, 0x00, 0xff, 0x44, 0xbb);
		setCbmDiskValue(p2 + 0x06, 0xc0);
		// Dir1
		setCbmDiskValue(p3 + 0x00, 0x00, 0xff);
	}

	@Override
	public int getNextSector(int track, int sector) {
		if (track < getTrackCount() && sector < TRACK_SECTORS) {
			return sector + 1;
		}
		return this.getFirstSector();
	}

	@Override
	public TrackSector getSector(int offset) {
		int t = offset / (TRACK_SECTORS * BLOCK_SIZE);
		int s = ((offset - t * TRACK_SECTORS * BLOCK_SIZE) / BLOCK_SIZE);
		return new TrackSector(t + 1,s);
	}

	@Override
	public int getFirstSector() {
		return DEFAULT_ZERO;
	}

}
