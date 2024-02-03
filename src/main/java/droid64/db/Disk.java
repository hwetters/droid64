package droid64.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import droid64.d64.DiskImageType;

/**
 * Persistent value class for representing one disk image.
 * @author Henrik
 */
public class Disk extends Value implements Serializable {
	private static final long serialVersionUID = -1L;

	private long diskId;
	private String label;
	private String filePath;
	private String fileName;
	private Date updated;
	private DiskImageType imageType;
	private Integer errors;
	private Integer warnings;
	private String hostName;

	private List<DiskFile> diskFiles = null;

	public void setDiskId(long id) {
		this.diskId = id;
	}

	public long getDiskId() {
		return diskId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<DiskFile> getDiskFiles() {
		if (diskFiles == null) {
			diskFiles = new ArrayList<>();
		}
		return diskFiles;
	}

	public void setDiskFiles(List<DiskFile> diskFiles) {
		this.diskFiles = diskFiles;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public DiskImageType getImageType() {
		return imageType;
	}

	public void setImageType(DiskImageType imageType) {
		this.imageType = imageType;
	}

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getWarnings() {
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("Disk[")
		.append(" .diskId=").append(diskId)
		.append(" .label=").append(label)
		.append(" .filePath=").append(filePath)
		.append(" .fileName=").append(fileName)
		.append(" .updated=").append(updated)
		.append(" .imageType=").append(imageType)
		.append(" .errors=").append(errors)
		.append(" .warnings=").append(warnings)
		.append(" .diskFiles=").append(diskFiles)
		.append(" .hostName=").append(hostName)
		.append(" .state=").append(getState())
		.append(']').toString();
	}

	public String toXML() {
		return new StringBuilder().append("<Disk>")
				.append("<diskId>").append(diskId).append("</diskId>")
				.append("<label>").append(label).append("</label>")
				.append("<filePath>").append(filePath).append("</filePath>")
				.append("<fileName>").append(fileName).append("</fileName>")
				.append("<updated>").append(updated).append("</updated>")
				.append("<imageType>").append(imageType).append("</imageType>")
				.append("<errors>").append(errors).append("</errors>")
				.append("<warnings>").append(warnings).append("</warnings>")
				.append("<hostName>").append(hostName).append("</hostName>")
				.append("<state>").append(getState()).append("</state>")
				.append("\n<diskFiles>\n")
				.append(diskFiles.stream().map(DiskFile::toXML).collect(Collectors.joining()))
				.append("</diskFiles>\n")
				.append("</Disk>\n").toString();
	}
}
