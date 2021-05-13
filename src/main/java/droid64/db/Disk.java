package droid64.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import droid64.d64.DiskImageType;

/**
 * Persistent value class for representing one disk image.
 * @author Henrik
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Disk", propOrder = {
	"diskId",
	"label",
	"filePath",
	"fileName",
	"updated",
	"imageType",
	"errors",
	"warnings",
	"hostName",
	"diskFiles"
})
public class Disk extends Value implements Serializable {
	private static final long serialVersionUID = -1L;

	@XmlElement(required = true)
	private long diskId;
	@XmlElement(required = true)
	private String label;
	@XmlElement(required = true)
	private String filePath;
	@XmlElement(required = true)
	private String fileName;
	@XmlElement(required = true, type = String.class)
	@XmlJavaTypeAdapter(DateAdapter .class)
	@XmlSchemaType(name = "dateTime")
	private Date updated;
	@XmlElement(required = true, defaultValue = "UNDEFINED")
	@XmlSchemaType(name = "droid64.d64.DiskImageType")
	private DiskImageType imageType;
	@XmlElement(required = true, defaultValue = "0")
	private Integer errors;
	@XmlElement(required = true, defaultValue = "0")
	private Integer warnings;
	@XmlElement(required = true)
	private String hostName;

	@XmlElementWrapper(name="diskFiles")
	@XmlElement(name="diskFile")
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
		var builder = new StringBuilder();
		builder.append("Disk[");
		builder.append(" .diskId=").append(diskId);
		builder.append(" .label=").append(label);
		builder.append(" .filePath=").append(filePath);
		builder.append(" .fileName=").append(fileName);
		builder.append(" .updated=").append(updated);
		builder.append(" .imageType=").append(imageType);
		builder.append(" .errors=").append(errors);
		builder.append(" .warnings=").append(warnings);
		builder.append(" .diskFiles=").append(diskFiles);
		builder.append(" .hostName=").append(hostName);
		builder.append(" .state=").append(getState());
		builder.append(']');
		return builder.toString();
	}

}
