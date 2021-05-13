package droid64.db;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
	public Date unmarshal(String value) {
		return DatatypeConverter.parseDateTime(value).getTime();
    }

    @Override
	public String marshal(Date value) {
		var cal = new GregorianCalendar();
		cal.setTime(value);
		return DatatypeConverter.printDateTime(cal);
	}
}
