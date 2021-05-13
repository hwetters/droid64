package droid64.db;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import droid64.d64.CbmFile;
import droid64.d64.FileType;

/**
 * Persistent value class for representing one file on a disk image
 * @author Henrik
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiskFile", propOrder = {
	"fileId",
	"diskId",
	"name",
	"fileType",
	"size",
	"fileNum",
	"flags"
})
public class DiskFile extends Value implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(required = true)
	private long fileId;
	@XmlElement(required = true)
	private long diskId;
	@XmlElement(required = true)
	private String name;
	@XmlElement(required = true, defaultValue = "PRG")
	@XmlSchemaType(name = "droid64.d64.FileType")
	private FileType fileType;
	private int size;
	private int fileNum;
	private int flags;

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiskFile[");
		builder.append(" .fileId=").append(fileId);
		builder.append(" .diskId=").append(diskId);
		builder.append(" .name=").append(name);
		builder.append(" .size=").append(size);
		builder.append(" .flags=").append(flags);
		builder.append(" .fileNum=").append(fileNum);
		builder.append(" .fileType=").append(fileType);
		builder.append(" .state=").append(getState());
		builder.append(']');
		return builder.toString();
	}
}
