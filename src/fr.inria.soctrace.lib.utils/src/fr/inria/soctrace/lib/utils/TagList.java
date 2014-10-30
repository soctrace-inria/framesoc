/**
 * 
 */
package fr.inria.soctrace.lib.utils;


/**
 * Utility class to generate lists of separated elements, between some brackets (e.g. [a, b, c]).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TagList {

	private int size = 0;
	private boolean first = true;
	private boolean quotes = false;
	private StringBuilder buffer = null;

	private static final String START = "[";
	private static final String END = "]";
	private static final String SEPARATOR = ",";

	public TagList() {
		clear();
	}

	public void setQuotes(boolean quotes) {
		this.quotes = quotes;
	}

	public void addValue(String value) {
		size++;
		if (!first) {
			buffer.append(getSeparator());
		} else {
			first = false;
		}
		if (quotes) {
			buffer.append("'");
		}
		buffer.append(value);
		if (quotes) {
			buffer.append("'");
		}
	}

	public String getValueString() {
		if (size == 0)
			throw new IllegalStateException("No element in this list");
		return getStart() + buffer.toString() + getEnd();
	}

	public void clear() {
		buffer = null;
		buffer = new StringBuilder();
		size = 0;
		first = true;
	}

	public int size() {
		return size;
	}
	
	protected String getSeparator() {
		return SEPARATOR;
	}
	
	protected String getStart() {
		return START;
	}
	
	protected String getEnd() {
		return END;
	}

}
