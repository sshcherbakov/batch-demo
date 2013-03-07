package batch.demo.util;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.Test;

public class BigDecimalPropertyEditorTest {
	
	@Test
	public void testSetAsText() {
		BigDecimalPropertyEditor editor = new BigDecimalPropertyEditor();
		editor.setAsText(" 21.634.026,75");
		Assert.assertEquals(new BigDecimal("21634026.75"), editor.getValue());
	}

}
