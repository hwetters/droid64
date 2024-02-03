package droid64.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/** Disk List */
public class DiskList implements Serializable {

	private static final long serialVersionUID = -1L;

	private List<Disk> disks;

	public List<Disk> getDisks() {
		if (disks == null) {
			disks = new ArrayList<>();
		}
		return this.disks;
	}

	private static void saveXml(File file, DiskList diskList) {
		try (var writer = new FileWriter(file)) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			writer.write("<DiskList>\n");
			for (var d: diskList.getDisks()) {
				writer.write(d.toXML());
			}
			writer.write("</DiskList>\n");
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Can't write file\n"+ex, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static int export(File f) throws DatabaseException {
		var dl = new DiskList();
		DaoFactory.getDaoFactory().getDiskDao().getAllDisks(true).forEach(d -> dl.getDisks().add(d));
		saveXml(f, dl);
		return dl.getDisks().size();
	}
}
