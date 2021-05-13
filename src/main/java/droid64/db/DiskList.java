package droid64.db;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "disks" })
@XmlRootElement(name = "DiskList")
public class DiskList implements Serializable {

	private static final long serialVersionUID = -1L;

	@XmlElement(name = "Disk")
	private List<Disk> disks;

	public List<Disk> getDisks() {
		if (disks == null) {
			disks = new ArrayList<>();
		}
		return this.disks;
	}

	public static DiskList readXml(File file) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("droid64.db");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return (DiskList) unmarshaller.unmarshal(file);
		} catch (JAXBException ex) {
			JOptionPane.showMessageDialog(null, "Can't read file\n"+ex, "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public static void saveXml(File file, DiskList diskList) {
		try  {
			JAXBContext jaxbContext = JAXBContext.newInstance("droid64.db");
			Marshaller marshaller   = jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(diskList, file);
		} catch (JAXBException ex) {
			JOptionPane.showMessageDialog(null, "Can't write file\n"+ex, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static int export(File f) throws DatabaseException {
		DiskList dl = new DiskList();
		DaoFactory.getDaoFactory().getDiskDao().getAllDisks(true).forEach(d -> dl.getDisks().add(d));
		saveXml(f, dl);
		return dl.getDisks().size();
	}
}
