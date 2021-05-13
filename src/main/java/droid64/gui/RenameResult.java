package droid64.gui;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;
import droid64.d64.Utility;

public class RenameResult {

	private String fileName = Utility.EMPTY;
	private FileType fileType = FileType.DEL;
	private String diskName = Utility.EMPTY;
	private String diskID = Utility.EMPTY;
	private DiskImageType diskType = DiskImageType.UNDEFINED;
	private boolean compressedDisk = false;
	private boolean cpmDisk = false;
	private int partitionSectorCount;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public void setDiskType(DiskImageType diskType) {
		this.diskType = diskType;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getDiskID() {
		return diskID;
	}

	public void setDiskID(String diskID) {
		this.diskID = diskID;
	}

	public DiskImageType getDiskType() {
		return diskType;
	}

	public boolean isCompressedDisk() {
		return compressedDisk;
	}

	public void setCompressedDisk(boolean compressedDisk) {
		this.compressedDisk = compressedDisk;
	}

	public boolean isCpmDisk() {
		return cpmDisk;
	}

	public void setCpmDisk(boolean cpmDisk) {
		this.cpmDisk = cpmDisk;
	}

	public int getPartitionSectorCount() {
		return partitionSectorCount;
	}

	public void setPartitionSectorCount(int partitionSectorCount) {
		this.partitionSectorCount = partitionSectorCount;
	}

	@Override
	public String toString() {
		return "RenameResult [fileName=" + fileName + ", fileType="
				+ fileType + ", diskName=" + diskName + ", diskID=" + diskID + ", diskType=" + diskType
				+ ", compressedDisk=" + compressedDisk + ", cpmDisk=" + cpmDisk + ", partitionSectorCount="
				+ partitionSectorCount + "]";
	}
}
