package droid64.d64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import droid64.db.Disk;
import droid64.db.DiskFile;
import droid64.gui.BAMPanel.BamTrack;
import droid64.gui.ConsoleStream;
import droid64.gui.Setting;

/**<pre style='font-family:sans-serif;'>
 * Created on 1.09.2015
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
 *   http://droid64.sourceforge.net
 *
 * @author henrik
 * </pre>
 */
public abstract class DiskImage implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_ZERO = 0;
	public static final int DEFAULT_ONE = 1;

	/** Size of a disk block */
	public static final int BLOCK_SIZE = 256;
	/** CP/M used byte marker. Single density disks are filled with this value from factory. CP/M use this to detect empty disks are blank. */
	public static final byte UNUSED = (byte) 0xe5;
	/** Max size of a PRG file */
	protected static final int MAX_PRG = 65536;
	/** Eight masks used to mask a bit out of a byte. Starting with LSB. */
	protected static final int[] BYTE_BIT_MASKS = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };
	/** Eight masks used to mask a bit out of a byte. Starting with MSB. */
	protected static final int[] REVERSE_BYTE_BIT_MASKS = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
	/** Eight masks used to mask all but one bits out of a byte. Starting with LSB. */
	protected static final int[] INVERTED_BYTE_BIT_MASKS = { 254, 253, 251, 247, 239, 223, 191, 127 };
	protected static final String CPM_DISKNAME_1 = "CP/M PLUS";
	protected static final String CPM_DISKNAME_2 = "CP/M DISK";
	protected static final String CPM_DISKID_GCR = "65 2A";
	protected static final String CPM_DISKID_1581 = "80 3D";
	/** The GEOS label found in BAM sector on GEOS formatted images */
	protected static final String DOS_LABEL_GEOS = "GEOS format";
	/** C1541 sector interleave. The gap between two blocks when saving a file */
	protected static final int C1541_INTERLEAVE = 10;
	/** C1571 sector interleave. The gap between two blocks when saving a file */
	protected static final int C1571_INTERLEAVE = 6;
	/** C1581 sector interleave. The gap between two blocks when saving a file */
	protected static final int C1581_INTERLEAVE = 1;
	/** Size of each directory entry on DIR_TRACK. */
	protected static final int DIR_ENTRY_SIZE = 32;
	/** Number of directory entries per directory sector */
	protected static final int DIR_ENTRIES_PER_SECTOR = 8;
	/** Maximum length of disk name */
	public static final int DISK_NAME_LENGTH = 16;
	/** Maximum length of disk ID */
	protected static final int DISK_ID_LENGTH = 5;
	/** Size of a CP/M records (128 bytes) */
	protected static final int CPM_RECORD_SIZE = 128;
	/** Type of image (D64, D71, D81, CP/M ... ) */
	protected DiskImageType imageFormat = DiskImageType.UNDEFINED;
	protected static final String NOT_IMPLEMENTED_FOR_CPM = "Not yet implemented for CP/M format.\n";
	/** When True, this is a GEOS-formatted disk, therefore files must be saved the GEOS way. */
	protected boolean geosFormat = false;
	/** True if image is compressed */
	protected boolean compressed;
	/** Error messages are appended here, and get presented in GUI */
	protected transient ConsoleStream feedbackStream;
	/** Data of the whole image. */
	protected byte[] cbmDisk = null;
	/** Number of files in image */
	protected int filesUsedCount;
	/** The file where this image is stored */
	protected File file = null;
	/**
	 * A cbmFile holds all additional attributes (like fileName, fileType etc) for a file on the image.<br>
	 * These attributes are used in the directory and are initialized in initCbmFiles() and filled with data in readDirectory().<br>
	 * Their index is the directory-position they have in the image file (see readDirectory()).
	 */
	protected final transient List<CbmFile> cbmFile = new ArrayList<>();
	/** All attributes which are stored in the BAM of a image file - gets filled with data in readBAM() */
	protected CbmBam bam;
	/** The number of validation errors, or null is no validation has been done. */
	protected Integer errors = null;
	protected Integer warnings = null;
	private final List<ValidationError> validationErrorList = new ArrayList<>();

	public int getFirstTrack() {
		return DEFAULT_ONE;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public abstract int getFirstSector();

	/**
	 * Get number of sectors on specified track
	 * @param trackNumber track number
	 * @return number of sectors on specified track.
	 */
	public abstract int getMaxSectors(int trackNumber);
	/**
	 * Get numbers of tracks on image.
	 * @return number of tracks.
	 */
	public abstract int getTrackCount();

	/**
	 * Get maximum number of sectors on any track.
	 * @return maximum number of sectors
	 */
	public abstract int getMaxSectorCount();

	/**
	 * @return The sector number of the highest sector. Usually maxSectorCount + firstSector
	 */
	public int getLastSector() {
		return getMaxSectorCount() + getFirstSector();
	}


	/**
	 * Get number of free blocks.
	 * @return blocks free
	 */
	public abstract int getBlocksFree();

	/**
	 * Reads image file.
	 * @param file	the file
	 * @return DiskImage
	 * @throws CbmException when error
	 */
	protected abstract DiskImage readImage(File file) throws CbmException;
	/**
	 * Reads the BAM of the D64 image and fills bam[] with entries.
	 */
	public abstract void readBAM();
	/**
	 * Reads the directory of the image, fills cbmFile[] with entries.
	 */
	public abstract void readDirectory();

	/**
	 * Reads the directory of the partition
	 * @param track track
	 * @param sector sector
	 * @param numBlocks number of blocks
	 * @throws CbmException if partition is not supported on the image.
	 */
	public void readPartition(int track, int sector, int numBlocks) throws CbmException {
		throw new CbmException("No partition support in " + getClass().getSimpleName() + ".");
	}

	/**
	 * Get data of a single file.
	 * @param number the file number in the image
	 * @return byte array file file contents
	 * @throws CbmException when error
	 */
	public abstract byte[] getFileData(int number) throws CbmException;
	/**
	 * Write the data of a single file to image.
	 * @param saveData byte[]
	 * @return the first track/sector of the file (for use in directory entry).
	 */
	protected abstract TrackSector saveFileData(byte[] saveData);
	/**
	 * Set a disk name and disk-id in BAM.
	 * @param newDiskName the new name of the disk
	 * @param newDiskID the new id of the disk
	 */
	protected abstract void setDiskName(String newDiskName, String newDiskID);
	/**
	 * Copy attributes of bufferCbmFile to a directoryEntry in cbmDisk.
	 * @param cbmFile cbm file
	 * @param dirEntryNumber position where to put this entry in the directory
	 */
	protected abstract void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber);
	/**
	 *
	 * @param file the file of the disk image
	 * @param newDiskName new disk name
	 * @param newDiskID new disk id
	 * @return true when successful
	 */
	public abstract boolean saveNewImage(File file, String newDiskName, String newDiskID);
	/**
	 * Add a directory entry of a single file to the image.<BR>
	 * @param cbmFile the CbmFile
	 * @param destTrack track where file starts
	 * @param destSector sector where file starts
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @param lengthInBytes length in bytes
	 * @return returns true is adding the entry to the directory was successful
	 */
	public abstract boolean addDirectoryEntry(CbmFile cbmFile, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes);
	/**
	 * Parse BAM track bits and store allocated/free blocks as BamState.
	 * @return BamTrack[]
	 */
	public abstract BamTrack[] getBamTable();
	/**
	 * Get offset to start of sector from beginning of image.
	 * @param track track
	 * @param sector sector
	 * @return offset offset to position in image where sector starts
	 * */
	public abstract int getSectorOffset(int track, int sector);
	/**
	 * Delete a file from disk image
	 * @param cbmFile The file to be deleted
	 * @throws CbmException when error
	 */
	public abstract void deleteFile(CbmFile cbmFile) throws CbmException;
	/**
	 * Validate image
	 * @param repairList list of errors which should be corrected if found.
	 * @return number or validation errors
	 */
	public abstract Integer validate(List<ValidationError.Error> repairList);
	public abstract boolean isSectorFree(int track, int sector);
	public abstract void markSectorFree(int track, int sector);
	public abstract void markSectorUsed(int track, int sector);

	/**
	 * @param block the block to be marked as free
	 */
	public void markSectorFree(TrackSector block) {
		markSectorFree(block.track, block.sector);
	}

	/**
	 * @param block the block to be marked as used
	 */
	public void markSectorUsed(TrackSector block) {
		markSectorUsed(block.track, block.sector);
	}

	/**
	 * @param block the block to check
	 * @return true if block is free
	 */
	public boolean isSectorFree(TrackSector block) {
		return isSectorFree(block.track, block.sector);
	}

	/**
	 * Initiate image structure.
	 * @param fileNumberLimit file number limit
	 */
	protected void initCbmFile(int fileNumberLimit) {
		cbmFile.clear();
		for (int i = 0; i < fileNumberLimit+1; i++) {
			cbmFile.add(new CbmFile());
		}
	}

	public boolean isCpmImage() {
		return 	imageFormat == DiskImageType.D64_CPM_C64 ||
				imageFormat == DiskImageType.D64_CPM_C128 ||
				imageFormat == DiskImageType.D71_CPM ||
				imageFormat == DiskImageType.D81_CPM;
	}

	public static DiskImage getDiskImage(File file, byte[] imageData, ConsoleStream consoleStream) throws CbmException {
		return Setting.getDiskImageType(file).getInstance(imageData, consoleStream);
	}

	/**
	 * Load disk image from file. Use file name extension to identify type of disk image.
	 * @param file the file
	 * @param consoleStream the stream for error messages
	 * @return DiskImage
	 * @throws CbmException if image could not be loaded (file missing, file corrupt out of memory etc).
	 */
	public static DiskImage getDiskImage(File file, ConsoleStream consoleStream) throws CbmException {
		var image = Setting.getDiskImageType(file).getInstance(consoleStream).readImage(file);
		if (image != null) {
			image.setFile(file);
		}
		return image;
	}

	/**
	 * Load image from disk
	 * @param file file of disk image
	 * @param type the type of image to load.
	 * @return DiskImage
	 * @throws CbmException when error
	 */
	protected DiskImage readImage(File file,  DiskImageType type) throws CbmException {
		feedbackStream.append("Trying to load ").append(type).append(" image ").append(file).append('\n');
		this.cbmDisk = null;
		if (Utility.isGZipped(file)) {
			feedbackStream.append("GZIP compressed file detected.\n");
			cbmDisk = Utility.readGZippedFile(file);
			compressed = true;
		} else {
			if (!file.isFile()) {
				throw new CbmException("File is not a regular file.");
			} else if (file.length() <= 0) {
				throw new CbmException("File is empty.");
			} else if (file.length() > Integer.MAX_VALUE) {
				throw new CbmException("File is too large.");
			} else if (file.length() < type.expectedSize && type.expectedSize > 0) {
				throw new CbmException("File smaller than normal size. A "+type+" file should be " + type.expectedSize + " bytes.");
			} else if (file.length() > type.expectedSize && type.expectedSize > 0) {
				feedbackStream.append("Warning: File larger than normal size. A "+type+" file should be ").append(type.expectedSize).append(" bytes.\n");
			}
			this.cbmDisk = Utility.readFile(file);
		}
		this.file = file;
		feedbackStream.append(type+" disk image was loaded.\n");
		return this;
	}

	/**
	 * @param dirTrack directory track
	 * @param dirSectors directory sectors
	 * @param use16bitau true if using 16 bit allocation units
	 */
	protected void readCpmDirectory(int dirTrack, int[] dirSectors, boolean use16bitau) {
		if (!isCpmImage()) {
			return;
		}
		int filenumber = 0;
		CpmFile entry = null;
		for (int s=0; s<dirSectors.length; s++) {
			int idx = getSectorOffset(dirTrack, dirSectors[s]);
			for (int i=0; i < DIR_ENTRIES_PER_SECTOR; i++) {
				var newFile = getCpmFile(entry, idx + i * DIR_ENTRY_SIZE, use16bitau);
				if (newFile != null) {
					cbmFile.set(filenumber++, newFile);
					entry = newFile;
				}
			}
		}
		filesUsedCount = filenumber;
	}

	/**
	 * Write the data and the directory entry of a single file to disk image.
	 * @param cbmFile the cbm file to save
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @param saveData the data to write to the file
	 * @return true if writing was successful (if there was enough space on disk image etc)
	 */
	public boolean saveFile(CbmFile cbmFile, boolean isCopyFile, byte[] saveData) {
		return saveFile(cbmFile, isCopyFile, saveData, saveData.length);
	}

	/**
	 * Write the data and the directory entry of a single file to disk image.
	 * @param cbmFile the cbm file to save
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @param saveData the data to write to the file
	 * @param overrideByteSize the length in bytes
	 * @return true if writing was successful (if there was enough space on disk image etc)
	 */
	public boolean saveFile(CbmFile cbmFile, boolean isCopyFile, byte[] saveData, int overrideByteSize) {
		if (isCpmImage()) {
			feedbackStream.append("saveFile: Not yet implemented for CP/M format.\n");
			return false;
		}
		if (!isCopyFile) {
			for (String ext : Setting.EXT_REMOVAL.getList()) {
				if (!Utility.isEmpty(ext) && cbmFile.getName().toLowerCase().endsWith(ext.toLowerCase())) {
					cbmFile.setName(cbmFile.getName().substring(0, cbmFile.getName().length() - ext.length()));
				}
			}
		}
		TrackSector firstBlock;
		if (cbmFile.getFileType() == FileType.DEL && saveData.length == 0) {
			feedbackStream.append("saveFile: '").append(cbmFile.getName()).append("'  (empty DEL file)\n");
			firstBlock = new TrackSector(0, 0);
		} else {
			feedbackStream.append("saveFile: '").append(cbmFile.getName()).append("'  ("+saveData.length+" bytes)\n");
			firstBlock = saveFileData(saveData);
		}
		if (firstBlock != null) {
			if (addDirectoryEntry(cbmFile, firstBlock.track, firstBlock.sector, isCopyFile, overrideByteSize)) {
				return true;
			}
		} else {
			feedbackStream.append("saveFile: Error occurred.\n");
		}
		return false;
	}

	/**
	 * Renames a disk image (label) <BR>
	 * @param newDiskName	the new name (label) of the disk
	 * @param newDiskID	the new disk-ID
	 * @return <code>true</code> when writing of the image file was successful
	 */
	public boolean renameImage(String newDiskName, String newDiskID){
		feedbackStream.append("renameImage(): ").append(newDiskName).append(", ").append(newDiskID);
		if (isCpmImage()) {
			feedbackStream.append(NOT_IMPLEMENTED_FOR_CPM);
			return false;
		}
		setDiskName(newDiskName, newDiskID);
		return save();
	}

	/**
	 * Sets new attributes for a single file.
	 * @param cbmFileNumber which file to rename
	 * @param newFileName the new name of the file
	 * @param newFileType the new type of the file (PRG, REL, SEQ, DEL, USR)
	 */
	public void renameFile(int cbmFileNumber, String newFileName, FileType newFileType) {
		feedbackStream.append("renameFile: oldName '").append(getCbmFile(cbmFileNumber).getName()).append(" newName '").append(newFileName).append("'\n");
		var newFile = new CbmFile(getCbmFile(cbmFileNumber));
		newFile.setName(newFileName);
		newFile.setNameAsBytes(newFile.getName().getBytes());
		newFile.setFileType(newFileType);
		writeDirectoryEntry(newFile, newFile.getDirPosition());
	}

	/**
	 * @param previousFile the previously found entry, or null if nothing yet.
	 * @param pos offset into disk image
	 * @param use16bitau use 16 bit allocation units
	 * @return CpmFile if a new file entry was found and prepared. If previous was updated or entry was scratched null is returned.
	 */
	protected CpmFile getCpmFile(CpmFile previousFile, int pos, boolean use16bitau) {
		CpmFile newFile = null;
		int userNum = cbmDisk[pos + 0x00] & 0xff;
		if (userNum >=0x00 && userNum <= 0x0f) {
			return getCpmFileEntry(previousFile, pos, use16bitau);
		} else if (userNum == 0x20) {
			var label = Utility.getCpmString(cbmDisk, pos + 0x01, 8);
			var labelType = Utility.getCpmString(cbmDisk, pos + 0x09, 3);
			bam.setDiskName(label+"."+labelType);
			feedbackStream.append("CP/M label "+label+"."+labelType);
		} else if (userNum != (UNUSED & 0xff)) {
			// 0x10 - 0x1f: password entries
			// 0x21: timestamp
		}
		return newFile;
	}

	private CpmFile getCpmFileEntry(CpmFile previousFile, int pos, boolean use16bitau) {
		CpmFile newFile = null;
		CpmFile tempFile = null;
		String name = Utility.getCpmString(cbmDisk, pos + 0x01, 8);
		String nameExt = Utility.getCpmString(cbmDisk, pos + 0x09, 3);
		boolean readOnly = (cbmDisk[pos + 0x09] & 0x80 ) == 0x80;
		boolean hidden   = (cbmDisk[pos + 0x0a] & 0x80 ) == 0x80;
		boolean archive  = (cbmDisk[pos + 0x0b] & 0x80 ) == 0x80;
		int extNum       =  cbmDisk[pos + 0x0c] & 0xff | ((cbmDisk[pos + 0x0e] & 0xff) << 8);
		int s1           =  cbmDisk[pos + 0x0d] & 0xff;	// Last Record Byte Count
		int rc           =  cbmDisk[pos + 0x0f] & 0xff;	// Record Count
		// Obviously, extNum is in numerical order, but it doesn't always start with 0, and it can skip some numbers.
		if (previousFile == null || !(previousFile.getCpmName().equals(name) && previousFile.getCpmNameExt().equals(nameExt)) ) {
			newFile = new CpmFile();
			newFile.setName(name + "." + nameExt);
			newFile.setFileType(FileType.PRG);
			newFile.setCpmName(name);
			newFile.setCpmNameExt(nameExt);
			newFile.setReadOnly(readOnly);
			newFile.setArchived(archive);
			newFile.setHidden(hidden);
			newFile.setFileScratched(false);
			newFile.setSizeInBlocks(rc);
			newFile.setSizeInBytes(rc * CPM_RECORD_SIZE);
			tempFile = newFile;
		} else {
			previousFile.setSizeInBlocks(previousFile.getSizeInBlocks() + rc);
			previousFile.setSizeInBytes(previousFile.getSizeInBlocks() * CPM_RECORD_SIZE);
			tempFile = previousFile;
		}
		tempFile.setLastExtNum(extNum);
		tempFile.setLastRecordByteCount(s1);
		tempFile.setRecordCount(extNum * 128 + rc);
		readCpmAllocUnits(use16bitau, pos, tempFile);
		return newFile;
	}

	private void readCpmAllocUnits(boolean use16bitau, int pos, CpmFile cpmFile ) {
		if (use16bitau) {
			for (int al=0; al < 8; al++) {
				int au = ((cbmDisk[pos + 0x10 + al * 2 + 1] & 0xff) << 8) | (cbmDisk[pos + 0x10 + al * 2 + 0] & 0xff);
				if (au != 0) {
					cpmFile.addAllocUnit(au);
				}
			}
		} else {
			for (int al=0; al < 16; al++) {
				int au = cbmDisk[pos + 0x10 + al] & 0xff;
				if (au != 0) {
					cpmFile.addAllocUnit(au);
				}
			}
		}
	}

	/**
	 * Set up variables in a new cbmFile which will be appended to the directory.
	 * These variables will inserted into the directory later.
	 * @param cbmFile cbmFile
	 * @param thisFilename this file name
	 * @param thisFileType  this file type
	 * @param destTrack track number
	 * @param destSector sector number
	 * @param lengthInBytes file length in bytes
	 */
	protected void setNewDirEntry(CbmFile cbmFile, String thisFilename, FileType thisFileType, int destTrack, int destSector, int lengthInBytes) {
		cbmFile.setFileScratched(false);
		cbmFile.setFileType(thisFileType);
		cbmFile.setFileLocked(false);
		cbmFile.setFileClosed(true);
		cbmFile.setTrack(destTrack);
		cbmFile.setSector(destSector);
		cbmFile.setName(thisFilename);
		cbmFile.setRecordLength(0);
		cbmFile.setRelTrack( 0);
		cbmFile.setRelSector( 0);
		cbmFile.setRecordLength( 0);
		for (int i = 0; i < 7; i++) {
			cbmFile.setGeos(i, 0);
		}
		cbmFile.setSizeInBytes(lengthInBytes);
		cbmFile.setSizeInBlocks((cbmFile.getSizeInBytes() - 2) / 254	);
		if ( ((cbmFile.getSizeInBytes()-2) % 254) >0 ) {
			cbmFile.setSizeInBlocks(cbmFile.getSizeInBlocks()+1);
		}
	}

	/**
	 * Get <code>Disk</code> instance of current image. This is used when saving to database.
	 * @return Disk
	 */
	public Disk getDisk() {
		var disk = new Disk();
		disk.setLabel(Utility.rinseCtrlChars(getBam().getDiskName()));
		disk.setImageType(imageFormat);
		disk.setErrors(errors);
		disk.setWarnings(warnings);
		for (int filenumber = 0; filenumber <= getFilesUsedCount() - 1;	filenumber++) {
			var cf =  getCbmFile(filenumber);
			if (cf != null) {
				disk.getDiskFiles().add(new DiskFile(cf, filenumber));
			}
		}
		return disk;
	}

	/**
	 * Return a string from a specified position on a block and having the specified length.
	 * @param track track
	 * @param sector sector
	 * @param pos position within block
	 * @param length the length of the returned string
	 * @return String, or null if outside of disk image.
	 */
	private String getStringFromBlock(int track, int sector, int pos, int length) {
		int dataPos = getSectorOffset(track, sector) + pos;
		if (dataPos + length < cbmDisk.length) {
			return new String(Arrays.copyOfRange(cbmDisk, dataPos, dataPos + length));
		} else {
			return null;
		}
	}

	private boolean checkCpmImageFormat() {
		String diskName = bam.getDiskName()!=null ? bam.getDiskName().replace("\\u00a0", Utility.EMPTY).trim() : null;
		if (!CPM_DISKNAME_1.equals(diskName) && !CPM_DISKNAME_2.equals(diskName)) {
			return false;
		}
		String diskId = bam.getDiskId() != null ? bam.getDiskId().replace("\\u00a0", Utility.SPACE).trim() : null;
		if (CPM_DISKID_GCR.equals(diskId)) {
			if ("CBM".equals(getStringFromBlock(1, 0, 0, 3))) {
				if (this instanceof D71 && (getCbmDiskValue(BLOCK_SIZE - 1) & 0xff) == 0xff) {
					feedbackStream.append("CP/M C128 double sided disk detected.\n");
					imageFormat = DiskImageType.D71_CPM;
					return true;
				} else if (this instanceof D64) {
					feedbackStream.append("CP/M C128 single sided disk detected.\n");
					imageFormat = DiskImageType.D64_CPM_C128;
					return true;
				}
			} else if (this instanceof D64 ) {
				feedbackStream.append("CP/M C64 single sided disk detected.\n");
				imageFormat = DiskImageType.D64_CPM_C64;
				return true;
			}
		} else if (this instanceof D81 && CPM_DISKID_1581.equals(diskId)) {
			feedbackStream.append("CP/M 3.5\" disk detected.\n");
			imageFormat = DiskImageType.D81_CPM;
			return true;
		}
		return false;
	}

	/**
	 * Checks, sets and return image format.
	 * @return image format
	 */
	public DiskImageType checkImageFormat() {
		if (checkCpmImageFormat()) {
			return imageFormat;
		} else if (this instanceof D64) {
			imageFormat = DiskImageType.D64;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D64.BAM_TRACK, D64.BAM_SECTOR, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof D67) {
			imageFormat = DiskImageType.D67;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D67.BAM_TRACK, D67.BAM_SECTOR, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof D71) {
			imageFormat = DiskImageType.D71;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D71.BAM_TRACK_1, D71.BAM_SECT, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof D81) {
			imageFormat = DiskImageType.D81;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D81.HEADER_TRACK, D81.HEADER_SECT, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof T64) {
			imageFormat = DiskImageType.T64;
			geosFormat = false;
		} else if (this instanceof D80) {
			imageFormat = DiskImageType.D80;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D80.HEADER_TRACK, D80.HEADER_SECT, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof D82) {
			imageFormat = DiskImageType.D82;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D82.HEADER_TRACK, D82.HEADER_SECT, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof LNX) {
			imageFormat = DiskImageType.LNX;
			geosFormat = false;
		} else if (this instanceof D88) {
			imageFormat = DiskImageType.D88;
			geosFormat = false;
		} else if (this instanceof D90) {
			imageFormat = cbmDisk.length == D90.D9060_SIZE ? DiskImageType.D90_9060 : DiskImageType.D90_9090;
			geosFormat = false;
		} else {
			imageFormat = DiskImageType.UNDEFINED;
			geosFormat = false;
		}
		if (geosFormat) {
			feedbackStream.append("GEOS formatted image detected.\n");
		}
		return imageFormat;
	}

	public DiskImageType getDiskImageType() {
		return imageFormat;
	}

	/**
	 * Writes a image to file system
	 * @param file the file
	 * @return true if successfully written
	 */
	public boolean saveAs(File file) {
		if (cbmDisk == null || file == null) {
			feedbackStream.append("No disk data. Nothing to write.\n");
			return false;
		}
		feedbackStream.append("writeImage: Trying to save ").append(compressed ? " compressed " : Utility.EMPTY).append(file).append("... \n");
		try {
			if (compressed) {
				Utility.writeGZippedFile(file, cbmDisk);
			} else {
				Utility.writeFile(file, cbmDisk);
			}
			return true;
		} catch (Exception e) {	//NOSONAR
			feedbackStream.append("Error: Could not write filedata.\n").append(e.getMessage()).append('\n');
			return false;
		}
	}

	/**
	 * Save disk image to file
	 * @return true if save was successful
	 */
	public boolean save() {
		return saveAs(file);
	}

	/**
	 * Switch directory locations of two files to move one of them upwards and the other downwards in the listing.
	 * @param cbmFile1 cbm file 1
	 * @param cbmFile2 cbm file 2
	 */
	public void switchFileLocations(CbmFile cbmFile1, CbmFile cbmFile2) {
		if (!isCpmImage()) {
			feedbackStream.append("DiskImage.switchFileLocations: '"+cbmFile1.getName() + "'  '"+cbmFile2.getName()+"'\n");
			int tmpDirTrack = cbmFile2.getDirTrack();
			int tmpDirSector = cbmFile2.getDirSector();
			cbmFile2.setDirTrack(cbmFile1.getDirTrack());
			cbmFile2.setDirSector(cbmFile1.getDirSector());
			cbmFile1.setDirTrack(tmpDirTrack);
			cbmFile1.setDirSector(tmpDirSector);
			writeDirectoryEntry(cbmFile1, cbmFile2.getDirPosition());
			writeDirectoryEntry(cbmFile2, cbmFile1.getDirPosition());
		}
	}

	/**
	 * Determine if there's, at least, one free sector on a track.
	 * @param trackNumber the track number of sector to check.
	 * @return when true, there is at least one free sector on the track.
	 */
	protected boolean isTrackFree(int trackNumber) {
		readBAM();
		return bam.getFreeSectors(trackNumber) > 0;
	}

	/**
	 * Get byte from a position within disk image.
	 * @param position position
	 * @return data at position, or 0 if position is not within the size of image.
	 */
	protected int getCbmDiskValue(int position){
		try {
			return cbmDisk[ position ] & 0xff;
		} catch (ArrayIndexOutOfBoundsException e) {	// NOSONAR
			feedbackStream.append("Error: reading outside of image at position ").append(position).append('\n');
			return 0;
		}
	}

	/**
	 * Get byte from a block
	 * @param track the track
	 * @param sector the sector
	 * @param offset the offset within the block
	 * @return data at position, or 0 if position is not within the size of image.
	 */
	protected int getCbmDiskValue(int track, int sector, int offset){
		int pos = getSectorOffset(track, sector) + offset;
		try {
			return cbmDisk[ pos ] & 0xff;
		} catch (ArrayIndexOutOfBoundsException e) {	// NOSONAR
			feedbackStream.append("Error: reading outside of image at position ").append(pos).append('\n');
			return 0;
		}
	}

	/**
	 * Set a byte at a position on the disk image.
	 * @param position the position within disk image
	 * @param value value
	 */
	public void setCbmDiskValue(int position, int value){
		if (cbmDisk != null) {
			cbmDisk[position] = (byte) value;
		}
	}

	protected void setCbmDiskValue(int position, int...values) {
		int i = 0;
		for (int value : values) {
			cbmDisk[position + (i++)] = (byte) value;
		}
	}

	/**
	 * Set a byte at a position on the disk image.
	 * @param track the track
	 * @param sector the sector
	 * @param offset offset within block
	 * @param value value
	 */
	protected void setCbmDiskValue(int track, int sector, int  offset, int value){
		int pos = getSectorOffset(track, sector) + offset;
		if (cbmDisk != null) {
			cbmDisk[ pos] = (byte) value;
		}
	}

	/**
	 * @return max file number
	 */
	public int getFilesUsedCount() {
		return filesUsedCount;
	}

	/**
	 * @return maximum number of file entries which can be stored
	 */
	public int getFileCapacity() {
		return  cbmFile.size();
	}

	/**
	 * @param number file number
	 * @return cbm file
	 */
	public CbmFile getCbmFile(int number) {
		if (number < cbmFile.size() && number >= 0) {
			return cbmFile.get(number);
		} else {
			return null;
		}
	}

	/**
	 * @param number file number
	 * @param file cbm file
	 */
	public void setCbmFile(int number, CbmFile file) {
		cbmFile.set(number, file);
	}

	public int getCbmFileSize() {
		return cbmFile.size();
	}
	
	/**
	 * @return BAM
	 */
	public CbmBam getBam() {
		return bam;
	}

	public void setCompressed(boolean compressed) {
		this.compressed  = compressed;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public DiskImageType getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(DiskImageType imageFormat) {
		this.imageFormat  = imageFormat;
	}

	/**
	 * @return the number of validation errors, or null if validation has not been performed.
	 */
	public Integer getErrors() {
		return this.errors;
	}
	public Integer getWarnings() {
		return warnings;
	}
	public List<ValidationError> getValidationErrorList() {
		return validationErrorList;
	}
	public static String getImageTypeName(DiskImageType imageType) {
		return imageType.id;
	}

	/**
	 * Get data on block.
	 * @param track track
	 * @param sector sector
	 * @return data from specified block
	 * @throws CbmException when error
	 */
	public byte[] getBlock(int track, int sector) throws CbmException {
		verifyTrackSector(track, sector);
		int pos = getSectorOffset(track, sector);
		return Arrays.copyOfRange(cbmDisk, pos, pos + BLOCK_SIZE);
	}

	public void verifyTrackSector(int track, int sector) throws CbmException {
		if (track < 1 || track > getTrackCount()) {
			throw new CbmException("Track "+track+" is not valid.");
		} else if (sector < 0 || sector >= getMaxSectors(track)) {
			throw new BadSectorException("Invalid sector: ", track, sector);
		}
	}

	/**
	 * Lookup first file matching criteria.
	 * @param name the name of the file
	 * @param fileType the type of file to look up
	 * @return found file or null if nothing found.
	 */
	public CbmFile findFile(String name, FileType fileType) {
		if (name == null) {
			return null;
		}
		for (CbmFile cf : cbmFile) {
			if (cf != null && cf.getName() != null && cf.getName().equals(name) && cf.getFileType() == fileType) {
				return cf;
			}
		}
		return null;
	}

	public Stream<CbmFile> getFileEntries() {
		return cbmFile.stream().filter(f -> f != null && f.getName() != null && !f.isFileScratched());
	}

	/**
	 * Fill sector in image with data. Pad with zeroes if saveData is smaller than BLOCK_SIZE - 2.
	 * @param track track number
	 * @param sector sector number
	 * @param dataPosition start filling sector at this position in saveData
	 * @param nextTrack next track
	 * @param nextSector next sector
	 * @param saveData data to fill sector with
	 */
	protected void fillSector(int track, int sector, int dataPosition, int nextTrack, int nextSector, byte[] saveData) {
		final int pos = getSectorOffset(track, sector);
		Arrays.fill(cbmDisk, pos + 0x02, pos + BLOCK_SIZE, (byte) 0);
		setCbmDiskValue(pos + 0x00, nextTrack);
		setCbmDiskValue(pos + 0x01, nextSector);
		for (int i = 0; i < (BLOCK_SIZE - 2); i++) {
			int value = saveData.length > dataPosition + i ? saveData[dataPosition + i] & 0xff : 0;
			setCbmDiskValue(pos + 0x02 + i, value);
		}
	}

	/**
	 * @return true if mounted image support directories (or partitions)
	 */
	public boolean supportsDirectories() {
		return false;
	}

	/**
	 * Create a directory (or partition)
	 * @param dirName name of directory to make
	 * @param size number of blocks of partition
	 * @param diskId the disk id
	 * @return the CbmFile for the new partition
	 * @throws CbmException if partition could not be created
	 */
	public CbmFile makedir(String dirName, int size, String diskId) throws CbmException {
		throw new CbmException("Image does not support directories/partitions.");
	}

	public String getSectorTitle(int i) {
		return Integer.toString(i - 1);
	}

	/**
	 * Read file entries from a chain of disk blocks
	 * @param dirTrack track of first directory block
	 * @param dirSector sector of first directory block
	 * @param maxNumFiles maximum number of files supported by this image
	 */
	protected void readDirectory(final int dirTrack, final int dirSector, final int maxNumFiles) {
		int track = dirTrack;
		int sector = dirSector;
		try {
			boolean fileLimitReached = false;
			int dirPosition = 0;
			int filenumber = 0;
			do {
				verifyTrackSector(track, sector);
				int dataPosition = getSectorOffset(track, sector);
				for (int i = 0; i < DIR_ENTRIES_PER_SECTOR; i ++) {
					cbmFile.set(filenumber, new CbmFile(cbmDisk, dataPosition + i * DIR_ENTRY_SIZE));
					if (!getCbmFile(filenumber).isFileScratched()) {
						getCbmFile(filenumber).setDirPosition(dirPosition);
						if (filenumber < maxNumFiles)  {
							filenumber++;
						} else {
							// Too many files in directory check
							fileLimitReached = true;
						}
					}
					dirPosition++;
				}
				track = getCbmDiskValue(dataPosition + 0);
				sector = getCbmDiskValue(dataPosition + 1);
			} while (track >= 0 && !fileLimitReached && sector != 0xff);
			if (fileLimitReached) {
				feedbackStream.append("Error: Too many entries in directory (more than ").append(maxNumFiles).append(")!\n");
			}
			filesUsedCount = filenumber;
		} catch (ArrayIndexOutOfBoundsException | CbmException e) { //NOSONAR
			feedbackStream.append("Error: Sector ").append(track).append('/').append(sector).append(" is outside of image.\n");
		}
	}

	public abstract int getNextSector(int track, int sector);

	public abstract TrackSector getSector(int offset);

	public boolean isPartitionOpen() {
		return false;
	}

	public int size() {
		return cbmDisk != null ? cbmDisk.length : -1;
	}

	public Integer getCurrentPartition() {
		return null;
	}

	public CbmFile setCurrentPartition(Integer partitionTrack) {
		return null;
	}

	public byte[] getData(int track, int sector) throws CbmException {
		var out = new ByteArrayOutputStream();
		var guard = new CyclicGuard<TrackSector>();
		var ts = new TrackSector(track,sector);
		do {
			if (ts.track > getTrackCount()) {
				throw new CbmException("Track " + ts.track + " outside of image.");
			}
			int blockPos = getSectorOffset(ts.track, ts.sector);
			int nextTrack  = getCbmDiskValue(blockPos + 0x00);
			int nextSector = getCbmDiskValue(blockPos + 0x01);
			feedbackStream.append(ts.track).append('/').append(ts.sector).append(Utility.SPACE);
			if (nextTrack > 0) {
				out.write(cbmDisk, blockPos + 2, BLOCK_SIZE - 2);
			} else {
				feedbackStream.append("\nRemaining bytes: ").append(nextSector).append('\n');
				out.write(cbmDisk, blockPos + 2, nextSector - 2 + 1);
			}
			if (!guard.addSilent(ts)) {
				throw new CbmException("Cyclic error. Sector " + ts + " already seen. "+guard);
			}
			ts = new TrackSector(nextTrack, nextSector);
		} while (ts.track != 0);
		return out.toByteArray();
	}

	protected void freeBlocks(int track, int sector) {
		while (track != 0) {
			int tmpTrack = getCbmDiskValue(track, sector, 0);
			int tmpSector = getCbmDiskValue(track, sector, 1);
			markSectorFree(track, sector);
			track = tmpTrack;
			sector = tmpSector;
		}
	}
}
