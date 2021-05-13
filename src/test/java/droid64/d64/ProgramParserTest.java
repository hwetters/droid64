package droid64.d64;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class ProgramParserTest {

	@Test
	public void test() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(600);
		buf.write(0x00);
		buf.write(0x10);
		for (int i=0; i<256; i++) {
			buf.write((byte)i);
			for (int j=1; j<ProgramParser.getinstructionLength((byte)i); j++) {
				buf.write((byte)j);
			}
		}
		byte[] bytes = buf.toByteArray();
		buf.close();
		Assert.assertEquals("Program byte size", 548, bytes.length);
		String asm = ProgramParser.parse(bytes, bytes.length, true);
		Assert.assertNotNull("Parsed program ", asm);
		Assert.assertFalse("Parsed program ", asm.isEmpty());


		ProgramParser.parse(new byte[2], 2, true);


	}

}
