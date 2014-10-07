/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ColorUtil;

import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.TmfVirtualTable;
import fr.inria.soctrace.framesoc.ui.model.TraceTableColumn;
import fr.inria.soctrace.framesoc.ui.model.TraceTableRow;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * TODO:
 * 
 * - allow to hide columns, and store in local settings the hidden columns -
 * allow to restore hidden columns - checkall / uncheckall when using filter
 * checkbox - filtering / reindexing - sorting / reindexing
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceFilterDialog extends Dialog {

	private static final String TRACE_FILTER_DIALOG_TITLE = "Trace Search";
	private static final String FILTER_HINT = "<filter>";
	private static final String MESSAGE = "Check the traces to highlight them in the Traces view.";

	private static final int DEFAULT_WIDTH = 1200;
	private static final int DEFAULT_HEIGHT = 300;

	private TmfVirtualTable fTable;
	private List<Trace> fTraces;
	private Set<Trace> fChecked;
	private Map<Trace, TraceTableRow> fCache;
	private Map<Integer, Trace> fIndex;

	// SWT resources
	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	private Color grayColor;
	private Color blackColor;
	private Font boldFont;

	/**
	 * Keys for table data
	 */
	public interface Key {
		/** Column object, set on a column */
		String COLUMN_OBJ = "$field_id"; //$NON-NLS-1$

		/** Filter text, set on a column */
		String FILTER_TXT = "$fltr_txt"; //$NON-NLS-1$

		/** Filter object, set on the table */
		String FILTER_OBJ = "$fltr_obj"; //$NON-NLS-1$
	}

	public TraceFilterDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setTraces(List<Trace> traces) {
		if (traces == null)
			throw new NullPointerException();
		fTraces = traces;
	}

	public void setChecked(Set<Trace> checked) {
		if (checked == null)
			throw new NullPointerException();
		fChecked = checked;
	}

	public Set<Trace> getChecked() {
		return fChecked;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (fTraces == null || fChecked == null)
			throw new NullPointerException();

		getShell().setText(TRACE_FILTER_DIALOG_TITLE);
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) composite.getLayout();
		gridLayout.verticalSpacing = 2;
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 2;

		Label messageLabel = createMessage(composite);
		TmfVirtualTable table = createTable(composite);

		if (fTraces.size() == 0) {
			messageLabel.setEnabled(false);
			table.setEnabled(false);
		}

		return composite;

	}

	private TmfVirtualTable createTable(Composite composite) {

		// Create the virtual table
		final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.CHECK;
		fTable = new TmfVirtualTable(composite, style);

		// Set the table layout
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = DEFAULT_WIDTH;
		layoutData.heightHint = DEFAULT_HEIGHT;
		fTable.setLayoutData(layoutData);

		// Create resources
		createResources();

		// Some cosmetic enhancements
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);

		// Set the columns
		setColumnHeaders();

		// Set the frozen row for header row
		fTable.setFrozenRowCount(1);

		// Create the header row cell editor
		createHeaderEditor();

		int index = 0;
		fCache = new HashMap<>();
		fIndex = new HashMap<>();
		for (Trace trace : fTraces) {
			fCache.put(trace, new TraceTableRow(trace));
			fIndex.put(index++, trace);
		}

		// Handle the table item requests
		fTable.addListener(SWT.SetData, new Listener() {

			@Override
			public void handleEvent(final org.eclipse.swt.widgets.Event event) {

				final TableItem item = (TableItem) event.item;
				int index = event.index - 1; // -1 for the header row

				if (event.index == 0) {
					setHeaderRowItemData(item);
					return;
				}

				TraceTableRow row = fCache.get(fIndex.get(index));
				item.setText(getItemStrings(row));

			}
		});

		fTable.setItemCount(1 + fTraces.size()); // +1 for the header

		fTable.setSelection(0);

		return fTable;

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// overridden to remove focus from OK button (last parameter false)
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
	}

	private String[] getItemStrings(TraceTableRow row) {
		String values[] = new String[TraceTableColumn.values().length];
		int i = 0;
		for (TraceTableColumn col : TraceTableColumn.values()) {
			try {
				values[i] = row.get(col);
			} catch (SoCTraceException e) {
				e.printStackTrace();
			} finally {
				i++;
			}
		}
		return values;
	}

	private void setHeaderRowItemData(final TableItem item) {
		item.setForeground(grayColor);
		for (int i = 0; i < fTable.getColumns().length; i++) {
			final TableColumn column = fTable.getColumns()[i];
			final String filter = (String) column.getData(Key.FILTER_TXT);
			if (filter == null) {
				item.setText(i, FILTER_HINT);
				item.setForeground(i, grayColor);
				item.setFont(i, fTable.getFont());
			} else {
				item.setText(i, filter);
				item.setForeground(i, blackColor);
				item.setFont(i, boldFont);
			}
		}
	}

	private void createResources() {
		grayColor = resourceManager.createColor(ColorUtil.blend(fTable.getBackground().getRGB(),
				fTable.getForeground().getRGB()));
		blackColor = fTable.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		boldFont = resourceManager.createFont(FontDescriptor.createFrom(fTable.getFont()).setStyle(
				SWT.BOLD));
	}

	private void setColumnHeaders() {
		ColumnData columnData[] = new ColumnData[TraceTableColumn.values().length];
		int i = 0;
		for (TraceTableColumn col : TraceTableColumn.values()) {
			columnData[i++] = new ColumnData(col.getShortName(), col.getWidth(), SWT.LEFT);
		}
		fTable.setColumnHeaders(columnData);
		i = 0;
		for (TraceTableColumn col : TraceTableColumn.values()) {
			fTable.getColumns()[i++].setData(Key.COLUMN_OBJ, col);
		}
	}

	private void createHeaderEditor() {
		final TableEditor tableEditor = fTable.createTableEditor();
		tableEditor.horizontalAlignment = SWT.LEFT;
		tableEditor.verticalAlignment = SWT.CENTER;
		tableEditor.grabHorizontal = true;
		tableEditor.minimumWidth = 50;

		// Handle the header row selection
		fTable.addMouseListener(new MouseAdapter() {
			int columnIndex;
			TableColumn column;
			TableItem item;

			@Override
			public void mouseDown(final MouseEvent event) {
				if (event.button != 1) {
					return;
				}
				// Identify the selected row
				final Point point = new Point(event.x, event.y);
				item = fTable.getItem(point);

				// Header row selected
				if ((item != null) && (fTable.indexOf(item) == 0)) {

					// Identify the selected column
					columnIndex = -1;
					for (int i = 0; i < fTable.getColumns().length; i++) {
						final Rectangle rect = item.getBounds(i);
						if (rect.contains(point)) {
							columnIndex = i;
							break;
						}
					}

					if (columnIndex == -1) {
						return;
					}

					column = fTable.getColumns()[columnIndex];

					// The control that will be the editor must be a child of
					// the Table
					final Text newEditor = (Text) fTable.createTableEditorControl(Text.class);
					final String headerString = (String) column.getData(Key.FILTER_TXT);
					if (headerString != null) {
						newEditor.setText(headerString);
					}
					newEditor.addFocusListener(new FocusAdapter() {
						@Override
						public void focusLost(final FocusEvent e) {
							final boolean changed = updateHeader(newEditor.getText());
							if (changed) {
								applyHeader();
							}
						}
					});
					newEditor.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(final KeyEvent e) {
							if (e.character == SWT.CR) {
								updateHeader(newEditor.getText());
								applyHeader();

								// Set focus on the table so that the next
								// carriage return goes to the next result
								// EventTableView.this.table.setFocus();
							} else if (e.character == SWT.ESC) {
								tableEditor.getEditor().dispose();
							}
						}
					});
					newEditor.selectAll();
					newEditor.setFocus();
					tableEditor.setEditor(newEditor, item, columnIndex);
				}
			}

			/*
			 * returns true if the value was changed
			 */
			private boolean updateHeader(final String text) {
				if (text.trim().length() > 0) {
					try {
						final String regex = regexFix(text);
						Pattern.compile(regex);
						if (regex.equals(column.getData(Key.FILTER_TXT))) {
							tableEditor.getEditor().dispose();
							return false;
						}
						TraceTableColumn col = (TraceTableColumn) column.getData(Key.COLUMN_OBJ);
						// TODO
						// rowFilter.setSearchText(col, regex);
						column.setData(Key.FILTER_TXT, regex);
					} catch (final PatternSyntaxException ex) {
						tableEditor.getEditor().dispose();
						MessageDialog.openError(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(), ex.getDescription(),
								ex.getMessage());
						return false;
					}
				} else {
					if (column.getData(Key.FILTER_TXT) == null) {
						tableEditor.getEditor().dispose();
						return false;
					}
					TraceTableColumn col = (TraceTableColumn) column.getData(Key.COLUMN_OBJ);
					// TODO
					// rowFilter.setSearchText(col, "");
					column.setData(Key.FILTER_TXT, null);
				}
				return true;
			}

			public String regexFix(String pattern) {
				String ret = pattern;
				// if the pattern does not contain one of the expressions .* !^
				// (at the beginning) $ (at the end), then a .* is added at the
				// beginning and at the end of the pattern
				if (!(ret.indexOf(".*") >= 0 || ret.charAt(0) == '^' || ret.charAt(ret.length() - 1) == '$')) { //$NON-NLS-1$
					ret = ".*" + ret + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ret;
			}

			private void applyHeader() {

				// TODO

				tableEditor.getEditor().dispose();
				fTable.refresh();
			}
		});
	}

	private Label createMessage(Composite composite) {
		Label messageLabel = new Label(composite, SWT.NONE);
		messageLabel.setText(MESSAGE);
		messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		return messageLabel;
	}

}
