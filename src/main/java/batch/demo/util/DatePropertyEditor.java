package batch.demo.util;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePropertyEditor extends PropertyEditorSupport {

	private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");;

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			Date date = format.parse(text);
			this.setValue(date);
		} 
		catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public String getAsText() {
		Date date = (Date) this.getValue();
		return format.format(date);
	}
}
