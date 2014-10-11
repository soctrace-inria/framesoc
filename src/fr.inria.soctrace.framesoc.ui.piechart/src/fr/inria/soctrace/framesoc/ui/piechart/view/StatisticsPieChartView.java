/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.piechart.view;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.PieDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO create a fragment plugin for jfreechart
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.piechart.loaders.EventProducerStatisticsLoader;
import fr.inria.soctrace.framesoc.ui.piechart.loaders.EventTypeStatisticsLoader;
import fr.inria.soctrace.framesoc.ui.piechart.loaders.PieChartStatisticsLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableColumn;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.framesoc.ui.piechart.providers.StatisticsTableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class StatisticsPieChartView extends FramesocPart {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(StatisticsPieChartView.class);

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.STATISTICS_PIE_CHART_VIEW_ID;

	/**
	 * Constants
	 */
	public static final boolean HAS_LEGEND = false;
	public static final boolean HAS_TOOLTIPS = true;
	public static final boolean HAS_URLS = true;	
	public static final boolean USE_BUFFER = true;

	/**
	 * Loader data
	 */
	private class LoaderData {
		public PieChartStatisticsLoader loader = null;
		public PieDataset dataset = null;
		public Map<String, FramesocColor> colors = null;

		public LoaderData(PieChartStatisticsLoader loader) {
			this.loader = loader;
		}
		
		public boolean dataReady() {
			return (dataset!=null && colors!=null);
		}

		public void dispose() {
			loader = null;
			if (dataset!=null)
				dataset = null;
			if (colors!=null) {
				for (FramesocColor c: colors.values())
					c.dispose();
				colors.clear();
				colors = null;
			}
		}
	}

	/**
	 * Available loaders. Add new loaders here.
	 */
	private LoaderData loaders [] = {
			new LoaderData(new EventTypeStatisticsLoader()),
			new LoaderData(new EventProducerStatisticsLoader())
	};

	/**
	 * Current loader index
	 */
	private int currentLoaderIndex = -1;
	
	/**
	 * Statistic loader combo
	 */
	private Combo combo;

	/**
	 * Pie load button
	 */
	private Button load;

	/**
	 * Description text
	 */
	private Text txtDescription;

	/**
	 * The chart parent composite 
	 */
	private Group compositePie;

	/**
	 * The table viewer
	 */
	private TreeViewer tableTreeViewer;

	/**
	 * Status text
	 */
	private Text statusText;

	/**
	 * Images
	 */
	private Map<String, Image> images = new HashMap<String, Image>();

	/**
	 * Column comparator
	 */
	private StatisticsColumnComparator comparator;
	
	/**
	 * Filter text for table
	 */
	private Text textFilter;

	/**
	 * Filter for table
	 */
	private RowFilter nameFilter;

	/**
	 * Constructor
	 */
	public StatisticsPieChartView() {
		super();
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
	}

//	// Uncomment this to use the window builder
//	@Override
//	public void createPartControl(Composite parent) {
//		createFramesocPartControl(parent);
//	}

	@Override
	public void createFramesocPartControl(Composite parent) {

		setContentDescription("Trace: <no trace displayed>");	

		// base GUI:
		SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.SMOOTH);

		// Pie
		Composite compositeLeft = new Composite(sashForm, SWT.BORDER);
		compositeLeft.setLayout(new GridLayout(1, false));

		Composite compositeCombo = new Composite(compositeLeft, SWT.NONE);
		compositeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeCombo.setLayout(new GridLayout(3, false));

		combo = new Combo(compositeCombo, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (int i=0; i<loaders.length; i++) {
			combo.add(loaders[i].loader.getStatName(), i);
		}
		combo.setEnabled(false);

		load = new Button(compositeCombo, SWT.NONE);
		load.setToolTipText("Load metric");
		load.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/play.png"));
		load.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadPieChart(currentShownTrace, combo.getSelectionIndex());
			}
		});
		load.setEnabled(false);
		new Label(compositeCombo, SWT.NONE);

		//compositePie = new Composite(compositeLeft, SWT.NONE);
		compositePie = new Group(compositeLeft, SWT.NONE);
		compositePie.setLayout(new FillLayout()); // Fill layout with Grid Data (FILL) to allow correct resize
		compositePie.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		txtDescription = new Text(compositePie, SWT.READ_ONLY | SWT.WRAP | SWT.CENTER | SWT.MULTI);
		txtDescription.setEnabled(false);
		txtDescription.setEditable(false);
		txtDescription.setText("Select one of the above metrics, then press the Load button.");
		txtDescription.setVisible(false);

		// Table
		Composite compositeTable = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeTable = new GridLayout(1, false);
		gl_compositeTable.marginWidth = 2;
		gl_compositeTable.marginHeight = 2;
		gl_compositeTable.horizontalSpacing = 2;
		gl_compositeTable.verticalSpacing = 2;
		compositeTable.setLayout(gl_compositeTable);

		Composite composite = new Composite(compositeTable, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		textFilter = new Text(compositeTable, SWT.BORDER);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textFilter.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					if (nameFilter == null || tableTreeViewer == null || statusText == null)
						return;
					if (currentLoaderIndex == -1 || loaders[currentLoaderIndex].dataset == null)
						return;
					nameFilter.setSearchText(textFilter.getText());
					tableTreeViewer.refresh();
					tableTreeViewer.expandAll();
					logger.debug("items: " +getTreeLeafs(tableTreeViewer.getTree().getItems(), 0));
					statusText.setText(getStatus(loaders[currentLoaderIndex].loader.getNumberOfValues(), 
							getTreeLeafs(tableTreeViewer.getTree().getItems(), 0)));
				}
			}
		});
		tableTreeViewer = new TreeViewer(compositeTable, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		tableTreeViewer.setContentProvider(new TreeContentProvider());
		comparator = new StatisticsColumnComparator();
		tableTreeViewer.setComparator(comparator);
		Tree table = tableTreeViewer.getTree();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createColumns();

		// status bar
		Composite statusBar = new Composite(compositeTable, SWT.BORDER);
		GridLayout statusBarLayout = new GridLayout();
		GridData statusBarGridData = new GridData();
		statusBarGridData.horizontalAlignment = SWT.FILL;
		statusBarGridData.grabExcessHorizontalSpace = true;
		statusBar.setLayoutData(statusBarGridData);
		statusBarLayout.numColumns = 1;
		statusBar.setLayout(statusBarLayout);
		// text
		statusText = new Text(statusBar,SWT.NONE);
		statusText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		statusText.setText(getStatus(0, 0));

	}

	private int getTreeLeafs(TreeItem[] items, int v) {
		for (TreeItem ti : items) {
			if (ti.getItems().length > 0) {
				v+=getTreeLeafs(ti.getItems(), 0); // add only leafs
			} else {
				v+=1;
			}
		}
		return v;
	}
	
	// GUI creation

	private String getStatus(int events, int matched) {
		StringBuilder sb = new StringBuilder();
		sb.append("Filter matched ");
		sb.append(matched);
		sb.append(" of ");
		sb.append(events);
		sb.append(" items");
		return sb.toString();
	}

	private void createColumns() {
		for (final StatisticsTableColumn col: StatisticsTableColumn.values()) {
			TreeViewerColumn elemsViewerCol = new TreeViewerColumn(tableTreeViewer, SWT.NONE);

			if (col.equals(StatisticsTableColumn.NAME)) {

				// add a filter for this column
				nameFilter = new RowFilter(col);
				tableTreeViewer.addFilter(nameFilter);

				// the label provider puts also the image
				elemsViewerCol.setLabelProvider(new StatisticsTableRowLabelProvider(col, images));
			}
			else
				elemsViewerCol.setLabelProvider(new TableRowLabelProvider(col));


			final TreeColumn elemsTableCol = elemsViewerCol.getColumn();
			elemsTableCol.setWidth(col.getWidth());		
			elemsTableCol.setText(col.getHeader());
			elemsTableCol.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					comparator.setColumn(col);
					tableTreeViewer.getTree().setSortDirection(comparator.getDirection());
					tableTreeViewer.getTree().setSortColumn(elemsTableCol);
					tableTreeViewer.refresh();
				}
			});
		}	
	}

	@Override
	public void setFocus() {
		super.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		for (LoaderData data: loaders) {
			data.dispose();
		}
		loaders = null;
		disposeImages();
		images = null;
	}
	
	private void disposeImages() {
		Iterator<Image> it = images.values().iterator();
		while (it.hasNext()) {
			it.next().dispose();
		}
		images.clear();		
	}

	/**
	 * Load a pie chart using the loader whose index is specified.
	 * @param trace 
	 * @param loaderIndex loader index in loaders array
	 */
	private void loadPieChart(final Trace trace, final int loaderIndex) {

		// Clean parent
		for (Control c: compositePie.getChildren()) {
			c.dispose();
		};

		// dispose images
		disposeImages();
		
		Job job = new Job("Loading Statistics Pie Chart...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Loading Statistics Pie Chart", IProgressMonitor.UNKNOWN);
				try {	
					// update trace selection
					if (trace==null)
						return Status.CANCEL_STATUS;
					currentShownTrace = trace;
					currentLoaderIndex = loaderIndex;

					// prepare dataset and chart (if necessary)
					final PieChartStatisticsLoader loader = loaders[loaderIndex].loader;
					if (!loaders[loaderIndex].dataReady()) {
						loader.load(currentShownTrace);
						loaders[loaderIndex].dataset = loader.getPieDataset();
						loaders[loaderIndex].colors = loader.getColors();
					}
					final PieDataset dataset = loaders[loaderIndex].dataset;
					final String title = loader.getStatName();
					final Map<String, FramesocColor> colors = loaders[loaderIndex].colors;
					final StatisticsTableFolderRow root = loader.getTableRows(colors);
					final int valuesCount = loader.getNumberOfValues();
					
					// prepare the new UI
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							final JFreeChart chart = createChart(dataset, "", colors);
							setContentDescription("Trace: " + currentShownTrace.getAlias());
							compositePie.setText(title);
							ChartComposite chartFrame = new ChartComposite(compositePie, SWT.NONE, chart, USE_BUFFER);
							
							Point size = compositePie.getSize();
							size.x -= 5; // consider the group border
							size.y -= 25; // consider the group border and text
							chartFrame.setSize(size);
							
							Point location = chartFrame.getLocation();
							location.x+=1; // consider the group border
							location.y+=20; // consider the group border and text
							chartFrame.setLocation(location);
							
							tableTreeViewer.setInput(root);
							tableTreeViewer.expandAll();					
							statusText.setText(getStatus(valuesCount, valuesCount));
							
							logger.debug("group location: " + compositePie.getLocation() );
							logger.debug("group size: " + compositePie.getSize() );
							logger.debug("frame location: " + chartFrame.getLocation() );
							logger.debug("frame size: " + chartFrame.getSize() );
						}
					});
					monitor.done();
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule(); 
	}

	/**
	 * Creates the chart.
	 * @param dataset the dataset.
	 * @param colors 
	 * @return the pie chart
	 */
	private static JFreeChart createChart(PieDataset dataset, String title, Map<String, FramesocColor> colors) {

		JFreeChart chart = ChartFactory.createPieChart(title, dataset, HAS_LEGEND, HAS_TOOLTIPS, HAS_URLS);

		// legend
		if (HAS_LEGEND) {
			LegendTitle legend = chart.getLegend();
			legend.setPosition(RectangleEdge.LEFT);
		}

		// plot
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setSectionOutlinesVisible(false);
		plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		plot.setNoDataMessage("No data available");
		plot.setCircular(true);
		plot.setLabelGenerator(null); // hide labels
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setShadowPaint(Color.WHITE);
	    plot.setBaseSectionPaint(Color.WHITE);	    

		for (Object o: dataset.getKeys()) {
			String key = (String)o;
			plot.setSectionPaint(key, colors.get(key).getAwtColor());
		}                
		return chart;
	}

	public class StatisticsColumnComparator extends ViewerComparator {
		private StatisticsTableColumn col = StatisticsTableColumn.OCCURRENCES;
		private int direction = SWT.DOWN;

		public int getDirection() {
			return direction;
		}

		public void setColumn(StatisticsTableColumn col) {
			if (this.col.equals(col)) {
				// Same column as last sort: toggle the direction
				direction = (direction == SWT.UP) ? SWT.DOWN : SWT.UP;
			} else {
				// New column: do an ascending sort
				this.col = col;
				direction = SWT.UP;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			
			StatisticsTableRow r1 = (StatisticsTableRow) e1;
			StatisticsTableRow r2 = (StatisticsTableRow) e2;
			
			// Aggregated rows at the end
			if (r1 instanceof StatisticsTableFolderRow)
				return +1;
			if (r2 instanceof StatisticsTableFolderRow)
				return -1;
			
			int rc = 0;
			try {
				if (this.col.equals(StatisticsTableColumn.OCCURRENCES)) {
					// number comparison
					Double v1 = Double.valueOf(r1.get(this.col));
					Double v2 = Double.valueOf(r2.get(this.col));
					rc = v1.compareTo(v2);
				} else if (this.col.equals(StatisticsTableColumn.PERCENTAGE)) {
					// percentage comparison 'xx.xx %'
					Double v1 = Double.valueOf(r1.get(this.col).split(" ")[0]);
					Double v2 = Double.valueOf(r2.get(this.col).split(" ")[0]);
					rc = v1.compareTo(v2);				
				} else {
					// string comparison
					String v1 = r1.get(this.col);
					String v2 = r2.get(this.col);
					rc = v1.compareTo(v2);
				}
			} catch (SoCTraceException e) {
				e.printStackTrace();
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == SWT.DOWN) {
				rc = -rc;
			}
			return rc;
		}
	}

	@Override
	public void partHandle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED)) {
			if (currentShownTrace==null)
				return;
			ColorsChangeDescriptor des = (ColorsChangeDescriptor) data;
			logger.debug("Colors changed: {}", des);
			try {
				loaders[currentLoaderIndex].colors.clear();
				loaders[currentLoaderIndex].colors = loaders[currentLoaderIndex].loader.getColors();
				loadPieChart(currentShownTrace, currentLoaderIndex);
			} catch (SoCTraceException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(Trace trace, Object data) {
		load.setEnabled(true);
		combo.setEnabled(true);
		combo.select(0);
		txtDescription.setVisible(true);
		currentShownTrace = trace;
		setContentDescription("Trace: " + trace.getAlias());
	}
}
