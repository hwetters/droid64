package droid64.db;

import java.io.Serializable;
import java.util.Arrays;

import droid64.d64.CbmFile;
import droid64.d64.FileType;
import droid64.d64.Utility;

/**
 * Persistent value class for representing one file on a disk image
 * @author Henrik
 */
public class DiskFile extends Value implements Serializable {

	private static final long serialVersionUID = 1L;

	private long fileId;
	private long diskId;
	private String name;
	private FileType fileType;
	private int size;
	private int fileNum;
	private int flags;
	private final byte[] nameAsBytes = new byte[CbmFile.MAX_NAME_LENGTH];

	public static final int FLAG_LOCKED = 1;
	public static final int FLAG_NOT_CLOSED = 2;

	public DiskFile() {
		super();
	}

	public DiskFile(CbmFile cf, int fileNumber) {
		this.name = cf.getName();
		this.size = cf.getSizeInBlocks();
		this.fileType = cf.getFileType();
		this.fileNum = fileNumber;
		this.flags = (cf.isFileLocked() ? DiskFile.FLAG_LOCKED : 0) | (cf.isFileClosed() ? 0 : DiskFile.FLAG_NOT_CLOSED);
		System.arraycopy(cf.getNameAsBytes(), 0, nameAsBytes, 0, CbmFile.MAX_NAME_LENGTH);
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getFileNum() {
		return fileNum;
	}

	public void setFileNum(int fileNum) {
		this.fileNum = fileNum;
	}

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public long getDiskId() {
		return diskId;
	}

	public void setDiskId(long diskId) {
		this.diskId = diskId;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileTypeString() {
		return fileType != null ? fileType.name() : "???";
	}

	public void setNameAsBytes(byte[] bytes) {
		if (bytes == null) {
			Arrays.fill(nameAsBytes, (byte) 0);
		} else {
			for (int i = 0; i < CbmFile.MAX_NAME_LENGTH; i++) {
				nameAsBytes[i] = bytes.length > i ? bytes[i] : 0;
			}
		}
	}

	public byte[] getNameAsBytes() {
		return nameAsBytes;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("DiskFile[")
		.append(" .fileId=").append(fileId)
		.append(" .diskId=").append(diskId)
		.append(" .name=").append(name)
		.append(" .size=").append(size)
		.append(" .flags=").append(flags)
		.append(" .fileNum=").append(fileNum)
		.append(" .fileType=").append(fileType)
		.append(" .state=").append(getState())
		.append(" .nameAsBytes=").append(Utility.hexDumpData(nameAsBytes))
		.append(']').toString();
	}

	public String toXML() {
		return new StringBuilder().append("<DiskFile>\n").append("<fileId>").append(fileId).append("</fileId>")
				.append("<diskId>").append(diskId).append("</diskId>").append("<name>").append(name).append("</name>")
				.append("<size>").append(size).append("</size>").append("<flags>").append(flags).append("</flags>")
				.append("<fileNum>").append(fileNum).append("</fileNum>").append("<fileType>").append(fileType)
				.append("</fileType>").append("<state>").append(getState()).append("</state>").append("<nameAsBytes>")
				.append(Utility.hexDumpData(nameAsBytes)).append("</nameAsBytes>").append("</DiskFile>\n").toString();
	}
}
