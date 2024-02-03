package droid64.db;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;

/**
 * Class to save search criteria when searching in the database for disks and files.
 * @author Henrik
 */
public class DiskSearchCriteria {

	private String diskLabel;
	private String fileName;
	private Integer fileSizeMin;
	private Integer fileSizeMax;
	private FileType fileType;
	private String diskPath;
	private String diskFileName;
	private DiskImageType imageType;
	private String hostName;

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Integer getFileSizeMin() {
		return fileSizeMin;
	}
	public void setFileSizeMin(Integer fileSizeMin) {
		this.fileSizeMin = fileSizeMin;
	}
	public Integer getFileSizeMax() {
		return fileSizeMax;
	}
	public void setFileSizeMax(Integer fileSizeMax) {
		this.fileSizeMax = fileSizeMax;
	}
	public FileType getFileType() {
		return fileType;
	}
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
	public String getDiskLabel() {
		return diskLabel;
	}
	public void setDiskLabel(String diskLabel) {
		this.diskLabel = diskLabel;
	}

	public void setDiskPath(String diskPath) {
		this.diskPath = diskPath;
	}
	public String getDiskPath() {
		return diskPath;
	}

	public void setDiskFileName(String diskFileName) {
		this.diskFileName = diskFileName;
	}

	public String getDiskFileName() {
		return diskFileName;
	}

	public DiskImageType getImageType() {
		return imageType;
	}

	public void setImageType(DiskImageType imageType) {
		this.imageType = imageType;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Has search criteria.
	 * @return false if all values are null or empty.
	 */
	public boolean hasCriteria() {
		return !( (fileName==null || fileName.trim().isEmpty()) &&
				(diskLabel==null || diskLabel.trim().isEmpty()) &&
				(diskPath==null || diskPath.trim().isEmpty()) &&
				(diskFileName==null || diskFileName.trim().isEmpty()) &&
				(hostName==null || hostName.trim().isEmpty()) &&
				(imageType==null) &&
				fileSizeMin==null && fileSizeMax==null && fileType == null);
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("DiskSearchCriteria[")
		.append(" .diskLabel=").append(diskLabel)
		.append(" .diskPath=").append(diskPath)
		.append(" .diskFileName=").append(diskFileName)
		.append(" .fileName=").append(fileName)
		.append(" .fileSizeMin=").append(fileSizeMin)
		.append(" .fileSizeMax=").append(fileSizeMax)
		.append(" .imageType=").append(imageType)
		.append(" .hostName=").append(hostName)
		.append(" .fileType=").append(fileType).append(']')
		.toString();
	}
}
