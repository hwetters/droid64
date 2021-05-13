package droid64.db;

import java.io.Serializable;

/**
 * Class to keep one row returned from the search query.
 * @author Henrik
 */
public class SearchResultRow implements Serializable {

	private static final long serialVersionUID = 1L;
	private String path;
	private String disk;
	private String label;
	private String file;
	private String type;
	private Integer size;
	private String hostName;

	/**
	 * Constructor.
	 * @param path String
	 * @param disk String
	 * @param label String
	 * @param file String
	 * @param type String
	 * @param size Integer
	 * @param hostName String
	 */
	public SearchResultRow(String path, String disk, String label, String file, String type, Integer size, String hostName) {
		this.path = path;
		this.disk = disk;
		this.label = label;
		this.file = file;
		this.type = type;
		this.size = size;
		this.hostName = hostName;
	}

	public SearchResultRow(Disk disk, DiskFile file) {
		this(disk.getFilePath(), disk.getFileName(), disk.getLabel(), file.getName(), file.getFileTypeString(), Integer.valueOf(file.getSize()), disk.getHostName());
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDisk() {
		return disk;
	}

	public void setDisk(String disk) {
		this.disk = disk;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
		builder.append("SearchResultRow[");
		builder.append(" .path=").append(path);
		builder.append(" .disk=").append(disk);
		builder.append(" .label=").append(label);
		builder.append(" .file=").append(file);
		builder.append(" .type=").append(type);
		builder.append(" .size=").append(size);
		builder.append(" .hostName=").append(hostName);
		builder.append(']');
		return builder.toString();
	}

}
