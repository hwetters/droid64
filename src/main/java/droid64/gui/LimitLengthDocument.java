package droid64.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;

public class LimitLengthDocument extends PlainDocument {
	private static final long serialVersionUID = 1L;
	private int limit;

	public LimitLengthDocument() {
		super();
		this.limit = Integer.MAX_VALUE;
	}

	public LimitLengthDocument(int limit) {
		super();
		this.limit = limit;
	}

	public LimitLengthDocument(int limit, String text) {
		super();
		this.limit = limit;
		try {
			insertString(0, text,  new SimpleAttributeSet());
		} catch (BadLocationException e) { /* ignore */ }
	}

	@Override
	public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException {
		if (str == null) {
			return;
		}
		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}

	public void setText(String text) {
		try {
			var attrs = new SimpleAttributeSet();
			super.replace(0, getLength(), text, attrs);
			insertString(0, text, attrs);
		} catch (BadLocationException e) { /* ignore */ }
	}

	public int size() {
		return super.getLength();
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
