package fr.inria.soctrace.framesoc.ui.filter;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;

/**
 * Base abstract class for ITableRow filters.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class TableRowFilter {

	/**
	 * Map between each row column and its filter string. It must be filled with the columns and
	 * empty strings by the {@link #clean()} method.
	 */
	protected Map<ITableColumn, String> searchStrings;

	/**
	 * Initialize the filter calling the {@link #clean()} method.
	 */
	public TableRowFilter() {
		clean();
	}

	/**
	 * Set the filter string for a given column
	 * 
	 * @param col
	 *            column to set
	 * @param s
	 *            filter string to set
	 */
	public void setSearchText(ITableColumn col, String s) {
		if (!searchStrings.containsKey(col))
			throw new IllegalArgumentException("Column " + col + " unknown by the filter.");
		searchStrings.put(col, s);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[Filters] ");
		Iterator<Entry<ITableColumn, String>> it = searchStrings.entrySet().iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			Entry<ITableColumn, String> e = it.next();
			sb.append(e.getKey().getHeader());
			sb.append(": ");
			sb.append(e.getValue());
		}
		return sb.toString();
	}

	/**
	 * Checks if the filter matches the passed row
	 * 
	 * @param row
	 *            the row to check
	 * @return true if the row matches, false otherwise
	 */
	public abstract boolean matches(ITableRow row);

	/**
	 * Clean the filter, setting an empty string for each row column.
	 */
	public abstract void clean();

}
