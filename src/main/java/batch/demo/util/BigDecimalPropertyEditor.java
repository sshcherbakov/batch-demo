package batch.demo.util;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;

public class BigDecimalPropertyEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		String strBd = text.trim().replace(".","").replace(',', '.');
		BigDecimal bd = new BigDecimal(strBd);
		this.setValue(bd);
	}
	
	@Override
	public String getAsText() {
		BigDecimal bd = (BigDecimal) this.getValue();
		return bd.toPlainString();
	}
}
