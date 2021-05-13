package droid64.d64;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class UtilityTest {

	@Test
	public void testCbmFileName() {
		Assert.assertEquals("ABC@", Utility.cbmFileName("abc@", 16));
		Assert.assertEquals("0123456789", Utility.cbmFileName("0123456789ABCDEF", 10));
	}

	@Test
	public void testPcFilename() {
		CbmFile cbmFile = new CbmFile();
		cbmFile.setFileType(FileType.PRG);
		cbmFile.setName("ABCDEFG;:/");
		Assert.assertEquals("abcdefg___.prg", Utility.pcFilename(cbmFile));
		Assert.assertEquals("bcdC", Utility.getCpmString("abcd\u00ecXYZ".getBytes(), 1, 4));
	}

	@Test
	public void testWrite() throws CbmException, IOException {
		File tmpFile = getTempFile("tmp");

		Utility.writeFile(tmpFile, "Hello world!");
		Assert.assertEquals("Hello world!\n", new String(Utility.readFile(tmpFile), "UTF-8"));
		Assert.assertFalse(Utility.isGZipped(new File(tmpFile.getAbsolutePath())));

		Utility.writeFile(tmpFile, getTempFile("tmp"));
		Assert.assertTrue("", Utility.writeFileSafe(tmpFile, getTempFile("tmp")));
		Assert.assertTrue("", Utility.writeFileSafe(getTempFile("tmp"), "Hello world!".getBytes()));
		Assert.assertTrue("", Utility.writeFileSafe(getTempFile("tmp"), "Hello world!".getBytes()));
		Assert.assertFalse("", Utility.writeFileSafe(getTempFile("tmp"), (File)null));
		Assert.assertFalse("", Utility.writeFileSafe((File)null, getTempFile("tmp")));
		Assert.assertFalse("", Utility.writeFileSafe((File)null, (File)null));
		Assert.assertFalse("", Utility.writeFileSafe((File)null, (byte[])null));
		Assert.assertFalse("", Utility.writeFileSafe(getTempFile("tmp"), (File)null));
		Assert.assertFalse("", Utility.writeFileSafe(getTempFile("tmp"), (byte[])null));

		File f1 = getTempFile("tmp");
		Assert.assertTrue(f1.delete());
		Assert.assertFalse(Utility.writeFileSafe(f1, getTempFile("tmp")));

		try {
			Utility.writeFile(new File("/No/Such/File"), getTempFile("tmp"));
			Assert.fail("Expected a CbmException");
		} catch (CbmException e) {}
		try {
			Utility.writeFile(null, getTempFile("tmp"));
			Assert.fail("Expected a CbmException");
		} catch (CbmException e) {}
		try {
			Utility.writeFile(getTempFile("tmp"), (File) null);
			Assert.fail("Expected a CbmException");
		} catch (CbmException e) {}
	}

	@Test(expected=CbmException.class)
	public void testGzip() throws CbmException {
		Utility.readGZippedFile(new File("/tmp/File/UnitTest_GzipDoesNotExist"));
	}

	@Test
	public void testTrim() throws CbmException {
		Assert.assertNull(Utility.safeTrim(null));
		Assert.assertNull(Utility.safeTrim(""));
		Assert.assertNull(Utility.safeTrim("  "));
		Assert.assertEquals("x", Utility.safeTrim(" x "));
		Assert.assertEquals("234", Utility.getTrimmedString("0123456789".getBytes(), 2, 3));
		Assert.assertEquals("1 23", Utility.getTrimmedString(new byte[] {48, 49, -96, 50, 51, 52, 53}, 1, 4));
	}

	@Test
	public void testBytes() throws CbmException {
		Assert.assertEquals(0x03, Utility.getInt8(getData(16), 3));
		Assert.assertEquals(0x00, Utility.getInt8(getData(16), 17));
		Assert.assertEquals(0, Utility.getInt8(null, 3));

		Assert.assertEquals(0x0504, Utility.getInt16(getData(16), 4));
		Assert.assertEquals(0, Utility.getInt16(null, 3));
		Assert.assertEquals(0x08070605, Utility.getInt32(getData(16), 5));

		Assert.assertEquals(42, Utility.parseInteger("", 42));
		Assert.assertEquals(42, Utility.parseInteger("42", 42));
		Assert.assertEquals(42, Utility.parseInteger("42", 11));
		Assert.assertEquals(12, Utility.parseInteger("X", 12));
		Assert.assertEquals(11, Utility.parseInteger(null, 11));

		Assert.assertEquals(0x78, Utility.trimIntByte(0x78));
		Assert.assertEquals(0, Utility.trimIntByte(-5));
		Assert.assertEquals(255, Utility.trimIntByte(256));

		byte[] strData = getData(32);
		Utility.setPaddedString(strData, 4, "ABCD", 8);
		Assert.assertTrue(Utility.getString(strData, 4, 8).startsWith("ABCD"));

		Assert.assertEquals("18c77f6046b49d927ddc3aa9c840a7f3",
				Utility.calcMd5Checksum("abcdefGHIJKL012345-.,?+()".getBytes()));
		Assert.assertEquals("f2", Utility.getByteString(0xf2));
		Assert.assertEquals("BA", Utility.getByteStringUpperCase(0xba));
		Assert.assertEquals("00cafe00", Utility.getIntHexString(0x00cafe00));
	}

	@Test
	public void testZip() throws Exception {

		File gzipTemp = getTempFile("tmp.gz");
		Utility.writeGZippedFile(gzipTemp, "Hello world!".getBytes());
		Utility.writeGZippedFile(gzipTemp, null);

		byte[] b = Utility.readGZippedFile(gzipTemp);

		Utility.hexDump(b);

		Assert.assertEquals(Utility.hexDump(b), 12, Utility.readGZippedFile(gzipTemp).length);

		File zipTemp = getTempFile("tmp.zip");
		byte[] testData = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		Utility.createNewZipFile(zipTemp, "TestEntryName", testData);

		byte[] readZipData = Utility.getDataFromZipFileEntry(zipTemp, "TestEntryName");
		Assert.assertEquals(testData.length, readZipData.length);

		List<DirEntry> entryList = Utility.getZipFileEntries(zipTemp, 1);
		Assert.assertEquals(1, entryList.size());
		Assert.assertEquals("TestEntryName", entryList.get(0).getName());
	}

	@Test
	public void testHexDump() throws Exception {
		Assert.assertEquals("null", Utility.hexDump(null));
		Assert.assertEquals("", Utility.hexDump(new byte[0]));
		Assert.assertEquals("00000000:  41 42 43 44 45 46  ABCDEF", Utility.hexDump(new byte[] {65,66,67,68,69,70}).trim());
	}

	@Test(expected=CbmException.class)
	public void testWriteFileFail() throws Exception {
		Utility.writeFile(getTempFile("tst"), (File) null);
	}

	@Test
	public void testSetString() {
		Utility.setString(new byte[10], 0, null);
		Utility.setString(new byte[10], 0, "ABCDEF");
		Assert.assertEquals("hex","F2",  Utility.getByteStringUpperCase(242));
	}

	@Test
	public void testSortFiles() {
		Assert.assertNotNull("empty", Utility.sortFiles(new File[] {}));
		Utility.sortFiles(new File[] {new File("A")});
		Utility.sortFiles(new File[] {new File("A"), new File("B"), new File(".")});
	}

	@Test
	public void testRinseCtrlChars() {
		Assert.assertNull("null", Utility.rinseCtrlChars(null));
		Assert.assertEquals("empty", "", Utility.rinseCtrlChars(""));
		Assert.assertEquals("ctrl", "ABCD", Utility.rinseCtrlChars("\tA\nB\fC\bD"));
		Assert.assertEquals("only ctrl", "", Utility.rinseCtrlChars("\t\n\f\b"));
	}

	@Test
	public void testGetPetSciiString() throws ParseException {
		Assert.assertEquals("1", "CDEF", Utility.getPetsciiString("ABCDEFG".getBytes(), 2, 6));
		Assert.assertEquals("2", "#b¤%", Utility.getPetsciiString("!\"#¤%&/()=".getBytes(), 2, 6));
	}

	private byte[] getData(int size) {
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) {
			data[i] = (byte) (i & 0xff);
		}
		return data;
	}

	private File getTempFile(String suffix) throws IOException {
		File tmpFile = File.createTempFile("UnitTest_", suffix);
		tmpFile.deleteOnExit();
		return tmpFile;
	}

	@Test
	public void testTruncate() {
		Assert.assertNull("null", Utility.truncate(null, 10));
		Assert.assertEquals("empty", "", Utility.truncate("", 10));
		Assert.assertEquals("exact", "1234", Utility.truncate("1234", 4));
		Assert.assertEquals("shorter", "1234", Utility.truncate("1234", 10));
		Assert.assertEquals("longer", "12345", Utility.truncate("1234567890", 5));
	}

	@Test
	public void testGetListItem() {
		Assert.assertNull("null", Utility.getListItem(null, 0));
		Assert.assertNull("negative", Utility.getListItem(Arrays.asList(new Integer[] {1, 2, 3}), -1));
		Assert.assertNull("after end", Utility.getListItem(Arrays.asList(new Integer[] {1, 2, 3}), 3));
		Assert.assertNull("empty", Utility.getListItem(Arrays.asList(new Integer[0]), 0));
		Assert.assertEquals("first", Integer.valueOf(10), Utility.getListItem(Arrays.asList(new Integer[] {10, 20, 30}), 0));
		Assert.assertEquals("first", Integer.valueOf(30), Utility.getListItem(Arrays.asList(new Integer[] {10, 20, 30}), 2));
	}

	@Test
	public void testStringStartsWith() {
		Assert.assertFalse("empty string",	Utility.stringStartsWith(""));
		Assert.assertFalse("null string",	Utility.stringStartsWith(null));
		Assert.assertFalse("null array",	Utility.stringStartsWith("abcdefgh", (String[]) null));
		Assert.assertFalse("null starter",	Utility.stringStartsWith("abcdefgh", (String) null));
		Assert.assertFalse("empty starters",	Utility.stringStartsWith("abcdefgh", new String[0]));
		Assert.assertTrue("first",	Utility.stringStartsWith("abcdefgh", "abc"));
		Assert.assertFalse("miss 1",	Utility.stringStartsWith("abcdefgh", "bcd"));
		Assert.assertFalse("miss empties",	Utility.stringStartsWith("abcdefgh", "", "123", "456", "789", ""));
		Assert.assertTrue("first match",	Utility.stringStartsWith("abcdefgh", "abc", "123", "456", "789", ""));
		Assert.assertTrue("last match",	Utility.stringStartsWith("abcdefgh", "123", "456", "789", "abc"));
		Assert.assertTrue("full match",	Utility.stringStartsWith("abcdefgh", "123", "456", "789", "abcdefgh"));
		Assert.assertFalse("miss longer",	Utility.stringStartsWith("abcdefgh", "123", "456", "789", "abcdefghij"));
	}

	@Test
	public void testSetListItem() {
		Assert.assertNull("null list", Utility.setListItem(null, 0, "a"));
		Assert.assertEquals("negative index", 0, Utility.setListItem(new ArrayList<String>(), -1, "a").size());

		Assert.assertEquals("index 0", 1, Utility.setListItem(new ArrayList<String>(), 0, "a").size());
		Assert.assertEquals("index 1", 2, Utility.setListItem(new ArrayList<String>(), 1, "a").size());
		Assert.assertEquals("index 2", 3, Utility.setListItem(new ArrayList<String>(), 2, "a").size());

		ArrayList<String> list = new ArrayList<>();

		Assert.assertEquals("index -1", 0, Utility.setListItem(list, -1, "a").size());
		Assert.assertEquals("index 3", 4, Utility.setListItem(list, 3, "c").size());
		Assert.assertEquals("index 0", 4, Utility.setListItem(list, 0, "a").size());
		Assert.assertEquals("index 10", 11, Utility.setListItem(list, 10, "a").size());
	}

	@Test
	public void testMakeList() {
		Assert.assertEquals("null", 0, Utility.makeList((String[])null).size());
		Assert.assertEquals("empty", 0, Utility.makeList().size());
		Assert.assertEquals("1", 1, Utility.makeList("a").size());
		Assert.assertEquals("2", 2, Utility.makeList("a", "b").size());
		Assert.assertEquals("3", 3, Utility.makeList("a", "b", "c").size());
	}

	@Test
	public void testCloneList() {
		Assert.assertEquals("null", 0, Utility.cloneList((List<String>)null).size());
		Assert.assertEquals("1", 1, Utility.cloneList(Arrays.asList("a")).size());
		Assert.assertEquals("2", 2, Utility.cloneList(Arrays.asList("a","b")).size());

		List<String> list1 = new ArrayList<>();
		list1.add("a");
		list1.add("b");
		list1.add("c");

		List<String> list2 = Utility.cloneList(list1);
		Assert.assertEquals("size", list1.size(), list2.size());
		Assert.assertEquals("size", list1, list2);
		Assert.assertFalse("equivalence test", list1 == list2);
	}

	@Test
	public void testIsOneOf() {
		Assert.assertFalse("both null", Utility.isOneOf((String) null, (String[]) null));
		Assert.assertFalse("straw null", Utility.isOneOf(null, new String[] {"1"}));
		Assert.assertFalse("haystack null", Utility.isOneOf("", (String[]) null));
		Assert.assertFalse("absent", Utility.isOneOf("2", new String[] {"1", "3", "4"}));
		Assert.assertTrue("present", Utility.isOneOf("2", new String[] {"1", "2", "3", "4"}));
	}

	@Test
	public void testRepeat() {
		Assert.assertNull("null", Utility.repeat((String) null, 5));
		Assert.assertEquals("empty", "", Utility.repeat("", 5));
		Assert.assertEquals("one", "a", Utility.repeat("a", 1));
		Assert.assertEquals("5x1", "aaaaa", Utility.repeat("a", 5));
		Assert.assertEquals("5x2", "1212121212", Utility.repeat("12", 5));
	}
}
