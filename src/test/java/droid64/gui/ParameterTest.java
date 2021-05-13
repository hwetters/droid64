package droid64.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.ParameterType;

public class ParameterTest {

	@Test
	public void testGettersPass() {
		Assert.assertTrue("Boolean", new OldParameter("test", ParameterType.BOOLEAN, true).getBooleanValue());
		Assert.assertFalse("Boolean", new OldParameter("test", ParameterType.BOOLEAN, false).getBooleanValue());
		Assert.assertNull("Boolean", new OldParameter("test", ParameterType.BOOLEAN, null).getBooleanValue());
		Assert.assertEquals("Integer", Integer.valueOf(1),
				new OldParameter("test", ParameterType.INTEGER, 1).getIntegerValue());
		Assert.assertEquals("Integer", Integer.valueOf(-1),
				new OldParameter("test", ParameterType.INTEGER, -1).getIntegerValue());
		Assert.assertNull("Integer", new OldParameter("test", ParameterType.INTEGER, null).getIntegerValue());
		Assert.assertEquals("String", "abcd", new OldParameter("test", ParameterType.STRING, "abcd").getStringValue());
		Assert.assertNull("String", new OldParameter("test", ParameterType.STRING, null).getStringValue());
		Assert.assertEquals("Color", Color.BLUE,
				new OldParameter("test", ParameterType.COLOR, Color.BLUE).getColorValue());
		Assert.assertNull("Color", new OldParameter("test", ParameterType.COLOR, null).getColorValue());
		Assert.assertEquals("Font", new Font("Sans", 8, 0),
				new OldParameter("test", ParameterType.FONT, new Font("Sans", 8, 0)).getFontValue());
		Assert.assertNull("Font", new OldParameter("test", ParameterType.FONT, null).getFontValue());

		Assert.assertEquals("List", Arrays.asList(new String[] { "a", "b", "c" }),
				new OldParameter("test", ParameterType.STRING_LIST, Arrays.asList(new String[] { "a", "b", "c" }))
				.getStringListValue());
		Assert.assertEquals("List", Arrays.asList(new String[0]),
				new OldParameter("test", ParameterType.STRING_LIST, null).getStringListValue());
		Assert.assertEquals("List", Arrays.asList(new String[] { "a", "b", "c" }),
				new OldParameter("test", ParameterType.INDEXED_STRING, Arrays.asList(new String[] { "a", "b", "c" }))
				.getIndexedStringValue());
		Assert.assertEquals("Indexed", Arrays.asList(new String[0]),
				new OldParameter("test", ParameterType.INDEXED_STRING, null).getIndexedStringValue());
	}

	@Test
	public void testSettersPass() {

		OldParameter p = new OldParameter("test", ParameterType.BOOLEAN, true);
		Assert.assertTrue(p.getBooleanValue());
		p.setBooleanValue(false);
		Assert.assertFalse(p.getBooleanValue());

		new OldParameter("test", ParameterType.BOOLEAN, true).setBooleanValue(true);
		new OldParameter("test", ParameterType.INTEGER, 1).setIntegerValue(1);
		new OldParameter("test", ParameterType.COLOR, Color.BLUE).setColorValue(Color.BLUE);
		new OldParameter("test", ParameterType.FONT, new Font("Sans", 8, 0)).setFontValue(new Font("Sans", 8, 0));
		new OldParameter("test", ParameterType.STRING_LIST, Arrays.asList(new String[] { "a", "b", "c" })).setStringListValue(Arrays.asList(new String[] { "a", "b", "c" }));
		new OldParameter("test", ParameterType.INDEXED_STRING, Arrays.asList(new String[] { "a", "b", "c" })).setIndexedStringValue(Arrays.asList(new String[] { "a", "b", "c" }));
	}

	@Test
	public void testGettersFail() {
		tryGetParamOfWrongType(ParameterType.BOOLEAN,
				new OldParameter("test", ParameterType.INTEGER, Integer.valueOf(1)));
		tryGetParamOfWrongType(ParameterType.INTEGER, new OldParameter("test", ParameterType.COLOR, Color.RED));
		tryGetParamOfWrongType(ParameterType.STRING, new OldParameter("test", ParameterType.BOOLEAN, Boolean.TRUE));
		tryGetParamOfWrongType(ParameterType.COLOR, new OldParameter("test", ParameterType.BOOLEAN, "x"));
		tryGetParamOfWrongType(ParameterType.FONT, new OldParameter("test", ParameterType.COLOR, Color.RED));
		tryGetParamOfWrongType(ParameterType.STRING_LIST, new OldParameter("test", ParameterType.FONT, Color.RED));
		tryGetParamOfWrongType(ParameterType.INDEXED_STRING, new OldParameter("test", ParameterType.FONT, Color.RED));
	}

	@Test
	public void testSettersFail() {
		trySetParamOfWrongType(ParameterType.BOOLEAN,
				new OldParameter("test", ParameterType.INTEGER, Integer.valueOf(1)));
		trySetParamOfWrongType(ParameterType.INTEGER, new OldParameter("test", ParameterType.COLOR, Color.RED));
		trySetParamOfWrongType(ParameterType.STRING, new OldParameter("test", ParameterType.BOOLEAN, Boolean.TRUE));
		trySetParamOfWrongType(ParameterType.COLOR, new OldParameter("test", ParameterType.BOOLEAN, "x"));
		trySetParamOfWrongType(ParameterType.FONT, new OldParameter("test", ParameterType.COLOR, Color.RED));
		trySetParamOfWrongType(ParameterType.STRING_LIST, new OldParameter("test", ParameterType.FONT, Color.RED));
		trySetParamOfWrongType(ParameterType.INDEXED_STRING, new OldParameter("test", ParameterType.FONT, Color.RED));
	}

	@Test
	public void testGetParamAsString() {
		Assert.assertEquals("Boolean", "test=true\n", new OldParameter("test", ParameterType.BOOLEAN, true).getParamAsString());
		Assert.assertEquals("Integer", "test=1\n", new OldParameter("test", ParameterType.INTEGER, 1).getParamAsString());
		Assert.assertEquals("String", "test=abcd\n", new OldParameter("test", ParameterType.STRING, "abcd").getParamAsString());
		Assert.assertEquals("Color", "test=0,0,255\n", new OldParameter("test", ParameterType.COLOR, Color.BLUE).getParamAsString());
		Assert.assertEquals("Font", "test=Sans;0;8\n", new OldParameter("test", ParameterType.FONT, new Font("Sans", 0, 8)).getParamAsString());
		Assert.assertEquals("List", "test=a;b;c\n", new OldParameter("test", ParameterType.STRING_LIST, Arrays.asList(new String[] { "a", "b", "c" })).getParamAsString());
		Assert.assertEquals("Indexed", "test.0=a\ntest.1=b\ntest.2=c\n", new OldParameter("test", ParameterType.INDEXED_STRING, Arrays.asList(new String[] { "a", "b", "c" })).getParamAsString());
	}

	@Test
	public void testParseValue() {
		Assert.assertEquals("Boolean", new OldParameter("test", ParameterType.BOOLEAN, true), parseValue(ParameterType.BOOLEAN, "yes\n"));
		Assert.assertEquals("Boolean", new OldParameter("test", ParameterType.BOOLEAN, false), parseValue(ParameterType.BOOLEAN, null));
		Assert.assertEquals("Integer", new OldParameter("test", ParameterType.INTEGER, 2), parseValue(ParameterType.INTEGER, "2\n"));
		Assert.assertEquals("Integer", new OldParameter("test", ParameterType.INTEGER, null), parseValue(ParameterType.INTEGER, null));
		Assert.assertEquals("String", new OldParameter("test", ParameterType.STRING, " \n A \n B "), parseValue(ParameterType.STRING, " \n A \n B \n"));
		Assert.assertEquals("String", new OldParameter("test", ParameterType.STRING, null), parseValue(ParameterType.STRING, null));
		Assert.assertEquals("Color", new OldParameter("test", ParameterType.COLOR, Color.YELLOW), parseValue(ParameterType.COLOR, "255,255,0\n"));
		Assert.assertEquals("Color", new OldParameter("test", ParameterType.COLOR, null), parseValue(ParameterType.COLOR, null));
		Assert.assertEquals("Font", new OldParameter("test", ParameterType.FONT, new Font("Sans", 0, 8)), parseValue(ParameterType.FONT, "Sans;0;8\n"));
		Assert.assertEquals("Font", new OldParameter("test", ParameterType.FONT, null), parseValue(ParameterType.FONT, null));
		Assert.assertEquals("StringList", new OldParameter("test", ParameterType.STRING_LIST, new ArrayList<String>()), parseValue(ParameterType.STRING_LIST, null));
		Assert.assertEquals("StringList", new OldParameter("test", ParameterType.STRING_LIST, Arrays.asList(new String[] {"a", "b", "c"})), parseValue(ParameterType.STRING_LIST, "a;b;c\n"));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals() {
		Assert.assertFalse(new OldParameter("Null", ParameterType.BOOLEAN, null).equals(null));
		Assert.assertFalse(new OldParameter("Different class", ParameterType.BOOLEAN, null).equals(Integer.valueOf(1)));
		Assert.assertTrue(new OldParameter("Null value", ParameterType.BOOLEAN, null).equals(new OldParameter("Null value", ParameterType.BOOLEAN, null)));
		Assert.assertFalse(new OldParameter("Different type", ParameterType.BOOLEAN, null).equals(new OldParameter("Different type", ParameterType.INTEGER, null)));
		Assert.assertFalse(new OldParameter("Different name", ParameterType.INTEGER, null).equals(new OldParameter("Name", ParameterType.INTEGER, null)));
		Assert.assertFalse(new OldParameter("Different value", ParameterType.INTEGER, 1).equals(new OldParameter("Different value", ParameterType.INTEGER, 2)));
		Assert.assertFalse(new OldParameter("Different value", ParameterType.INTEGER, 1).equals(new OldParameter("Different value", ParameterType.INTEGER, null)));
		Assert.assertTrue(new OldParameter("Same value", ParameterType.INTEGER, 1).equals(new OldParameter("Same value", ParameterType.INTEGER, 1)));
		OldParameter param = new OldParameter("Equivalent", ParameterType.INTEGER, 1);
		Assert.assertTrue(param.equals(param));
	}

	@Test
	public void testGetFontString() {
		Assert.assertEquals("getFontAsDisplayString plain", "Dialog-Plain-10", OldParameter.getFontAsDisplayString(new Font("Dialog", Font.PLAIN, 10)));
		Assert.assertEquals("getFontAsDisplayString bold", "Dialog-Bold-12", OldParameter.getFontAsDisplayString(new Font("Dialog", Font.BOLD, 12)));
		Assert.assertEquals("getFontAsString plain", "Dialog;0;12", OldParameter.getFontAsString(new Font("Dialog", Font.PLAIN, 12)));
		Assert.assertEquals("getFontAsString bold", "Dialog;1;11", OldParameter.getFontAsString(new Font("Dialog", Font.BOLD, 11)));
		Assert.assertEquals("getFontAsString italic", "Dialog;2;10", OldParameter.getFontAsString(new Font("Dialog", Font.ITALIC, 10)));
		Assert.assertEquals("getFontAsString bold italic", "Dialog;3;9", OldParameter.getFontAsString(new Font("Dialog", Font.ITALIC|Font.BOLD, 9)));
		Assert.assertEquals("getFontAsString courier", "Courier;3;8", OldParameter.getFontAsString(new Font("Courier", Font.ITALIC|Font.BOLD, 8)));
	}

//	@Test
//	public void testHashCode() {
//		Assert.assertEquals("HashCode", 3557732, new Parameter("test", ParameterType.BOOLEAN, Boolean.TRUE).hashCode());
//	}

	private OldParameter parseValue(ParameterType type, String value) {
		OldParameter p = new OldParameter("test", type, null);
		p.parseValue(value);
		return p;
	}

	private void trySetParamOfWrongType(ParameterType type, OldParameter param) {
		try {
			switch (type) {
			case BOOLEAN:
				param.setBooleanValue(true);
				break;
			case COLOR:
				param.setColorValue(Color.RED);
				break;
			case FONT:
				param.setFontValue(new Font("Sans", 8, 0));
				break;
			case INTEGER:
				param.setIntegerValue(1);
				break;
			case STRING:
				param.setStringValue("A");
				break;
			case STRING_LIST:
				param.setStringListValue(Arrays.asList(new String[] { "a", "b", "c" }));
				break;
			case INDEXED_STRING:
				param.setIndexedStringValue(Arrays.asList(new String[] { "a", "b", "c" }));
				break;
			case FILE:
				break;
			}
		} catch (IllegalArgumentException e) {
			return;
		}
		Assert.fail("Expected IllegalArgumentException");
	}

	private void tryGetParamOfWrongType(ParameterType type, OldParameter param) {
		try {
			switch (type) {
			case BOOLEAN:
				param.getBooleanValue();
				break;
			case COLOR:
				param.getColorValue();
				break;
			case FONT:
				param.getFontValue();
				break;
			case INTEGER:
				param.getIntegerValue();
				break;
			case STRING:
				param.getStringValue();
				break;
			case STRING_LIST:
				param.getStringListValue();
				break;
			case INDEXED_STRING:
				param.getIndexedStringValue();
				break;
			case FILE:
				break;
			}
		} catch (IllegalArgumentException e) {
			return;
		}
		Assert.fail("Expected IllegalArgumentException");
	}

}
