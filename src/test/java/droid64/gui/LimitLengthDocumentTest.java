package droid64.gui;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

import org.junit.Assert;
import org.junit.Test;

public class LimitLengthDocumentTest {

	@Test
	public void test() throws BadLocationException {
		LimitLengthDocument doc = new LimitLengthDocument(5, "text");
		doc.insertString(4, "STR", new SimpleAttributeSet());
		doc.insertString(4, null, new SimpleAttributeSet());
		doc.setText("TEST");
		Assert.assertEquals("", 4 , doc.size());
		new LimitLengthDocument(1);
	}

}
