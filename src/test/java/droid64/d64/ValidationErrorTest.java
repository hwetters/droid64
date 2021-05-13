package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class ValidationErrorTest {

	@Test
	public void test() {
		ValidationError.Error.ERROR_DIR_SECTOR_ALREADY_SEEN.getError(1,2);
		ValidationError.Error.ERROR_BAM_FREE_SECTOR_MISMATCH.getError(1,2).toString();
		ValidationError ve = ValidationError.Error.ERROR_FILE_SECTOR_ALREADY_SEEN.getError(1, 2, "file");
		ve.getTrack();
		ve.getSector();
		ve.getFileName();

		Assert.assertFalse(ValidationError.Error.ERROR_FILE_SECTOR_ALREADY_FREE.getError(1,2).toString().isEmpty());
	}

}
