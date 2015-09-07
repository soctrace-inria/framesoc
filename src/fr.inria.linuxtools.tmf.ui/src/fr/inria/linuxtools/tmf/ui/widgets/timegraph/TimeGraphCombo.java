/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   François Rajotte - Filter implementation
 *   Geneviève Bastien - Add event links between entries
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.internal.tmf.ui.ITmfImageConstants;
import fr.inria.linuxtools.internal.tmf.ui.Messages;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Time graph "combo" view (with the list/tree on the left and the gantt chart
 * on the right)
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphCombo extends FXCanvas {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final TimeGraphEntry FILLER = new TimeGraphEntry("FILLER", -1, -1); //$NON-NLS-1$

  //  private static final String ITEM_HEIGHT = "$height$"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The tree viewer */
    private TreeView<ITimeGraphEntry> fTreeViewer;
    private TreeItem<ITimeGraphEntry> fTreeRoot;

    /** The time viewer */
    private TimeGraphViewer fTimeGraphViewer;

    /** The selection listener map */
    private final Map<ITimeGraphSelectionListener, SelectionListenerWrapper> fSelectionListenerMap = new HashMap<>();

    /** The map of viewer filters */
    private final Map<ViewerFilter, ViewerFilter> fViewerFilterMap = new HashMap<>();

    /**
     * Flag to block the tree selection changed listener when triggered by the
     * time graph combo
     */
    private boolean fInhibitTreeSelection = false;

    /** Number of filler rows used by the tree content provider */
    private int fNumFillerRows;

    /** Calculated item height for Linux workaround */
    //private int fLinuxItemHeight = 0;

    /** The button that opens the filter dialog */
    private Action showFilterAction;

    /** The filter dialog */
    private TimeGraphFilterDialog fFilterDialog;

    /** The filter generated from the filter dialog */
    private RawViewerFilter fFilter;

    /** Default weight of each part of the sash */
    private static final double[] DEFAULT_WEIGHTS = { 0.5, 0.5 };

    /** Default height value in pixels for the objects */
    public static final int DEFAULT_TREE_ITEM_HEIGHT = 25;
    private int fItemHeight = DEFAULT_TREE_ITEM_HEIGHT;

    /** Minimal height for an item of the graph view */
    public static final int MINIMAL_ITEM_HEIGHT = 7;

    /** List of all expanded items whose parents are also expanded */
    private List<TreeItem<ITimeGraphEntry>> fVisibleExpandedItems = null;

    private Group root;

    private Scene scene;

    private Map<ITimeGraphEntry, TreeItem<ITimeGraphEntry>>  treeItems = new HashMap<>();

    private TextField treeHeader;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    /**
     * The TreeContentProviderWrapper is used to insert filler items after the
     * elements of the tree's real content provider.
     */
   /* private class TreeContentProviderWrapper implements ITreeContentProvider {
        private final ITreeContentProvider contentProvider;

        public TreeContentProviderWrapper(ITreeContentProvider contentProvider) {
            this.contentProvider = contentProvider;
        }

        @Override
        public void dispose() {
            contentProvider.dispose();
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            contentProvider.inputChanged(viewer, oldInput, newInput);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            Object[] elements = contentProvider.getElements(inputElement);
            // add filler elements to ensure alignment with time analysis viewer
            Object[] oElements = Arrays.copyOf(elements, elements.length + fNumFillerRows, Object[].class);
            for (int i = 0; i < fNumFillerRows; i++) {
                oElements[elements.length + i] = FILLER;
            }
            return oElements;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ITimeGraphEntry) {
                return contentProvider.getChildren(parentElement);
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.getParent(element);
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.hasChildren(element);
            }
            return false;
        }
    }*/

    /**
     * The TreeLabelProviderWrapper is used to intercept the filler items from
     * the calls to the tree's real label provider.
     */
  /*  private class TreeLabelProviderWrapper implements ITableLabelProvider {
        private final ITableLabelProvider labelProvider;

        public TreeLabelProviderWrapper(ITableLabelProvider labelProvider) {
            this.labelProvider = labelProvider;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            labelProvider.addListener(listener);
        }

        @Override
        public void dispose() {
            labelProvider.dispose();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.isLabelProperty(element, property);
            }
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            labelProvider.removeListener(listener);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnImage(element, columnIndex);
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnText(element, columnIndex);
            }
            return null;
        }

    }*/

    /**
     * The SelectionListenerWrapper is used to intercept the filler items from
     * the time graph combo's real selection listener, and to prevent double
     * notifications from being sent when selection changes in both tree and
     * time graph at the same time.
     */
    private class SelectionListenerWrapper implements ISelectionChangedListener, ITimeGraphSelectionListener {
        private final ITimeGraphSelectionListener listener;
        private ITimeGraphEntry selection = null;

        public SelectionListenerWrapper(ITimeGraphSelectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (fInhibitTreeSelection) {
                return;
            }
            Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (element instanceof ITimeGraphEntry) {
                ITimeGraphEntry entry = (ITimeGraphEntry) element;
                if (entry != selection) {
                    selection = entry;
                    listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
                }
            }
        }

        @Override
        public void selectionChanged(TimeGraphSelectionEvent event) {
            ITimeGraphEntry entry = event.getSelection();
            if (entry != selection) {
                selection = entry;
                listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
            }
        }
    }

    /**
     * The ViewerFilterWrapper is used to intercept the filler items from the
     * time graph combo's real ViewerFilters. These filler items should always
     * be visible.
     */
    private class ViewerFilterWrapper extends ViewerFilter {

        private ViewerFilter fWrappedFilter;

        ViewerFilterWrapper(ViewerFilter filter) {
            super();
            this.fWrappedFilter = filter;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof ITimeGraphEntry) {
                return fWrappedFilter.select(viewer, parentElement, element);
            }
            return true;
        }

    }

    /**
     * This filter simply keeps a list of elements that should be filtered out.
     * All the other elements will be shown. By default and when the list is set
     * to null, all elements are shown.
     */
    private class RawViewerFilter extends ViewerFilter {

        private List<Object> fFiltered = null;

        public void setFiltered(List<Object> objects) {
            fFiltered = objects;
        }

        public List<Object> getFiltered() {
            return fFiltered;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (fFiltered == null) {
                return true;
            }
            return !fFiltered.contains(element);
        }
    }

    /**
     * Sets the tree columns for this time graph combo's filter dialog.
     *
     * @param columnNames
     *            the tree column names
     * @since 2.0
     */
    public void setFilterColumns(String[] columnNames) {
        fFilterDialog.setColumnNames(columnNames);
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     *
     * @param parent
     *            a widget which will be the parent of the new instance (cannot
     *            be null)
     * @param style
     *            the style of widget to construct
     */
    public TimeGraphCombo(Composite parent, int style) {
        this(parent, style, DEFAULT_WEIGHTS);
    }

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     *
     * @param parent
     *            a widget which will be the parent of the new instance (cannot
     *            be null)
     * @param style
     *            the style of widget to construct
     * @param weights
     *            The relative weights of each side of the sash form
     * @since 2.1
     */
    public TimeGraphCombo(Composite parent, int style, double[] weights) {
        super(parent, style);
        setLayout(new FillLayout());

        final SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        root = new Group();

        // Set background color
        org.eclipse.swt.graphics.Color col = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        Color bkColor = Color.rgb(col.getRed(), col.getGreen(), col.getBlue());
        scene = new Scene(root, bkColor);

        setScene(scene);
        splitPane.prefWidthProperty().bind(scene.widthProperty());
        splitPane.prefHeightProperty().bind(scene.heightProperty());

        VBox vBoxTree = new VBox();

        treeHeader = new TextField("Event Producers"); //$NON-NLS-1$
        treeHeader.setEditable(false);
        vBoxTree.getChildren().add(treeHeader);

        fTreeViewer = new TreeView<>();
        fTreeViewer.setShowRoot(false);
        fTreeViewer.maxWidth(Double.MAX_VALUE);
        fTreeViewer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        fTreeViewer.setCellFactory(new Callback<TreeView<ITimeGraphEntry>, TreeCell<ITimeGraphEntry>>() {
            @Override
            public TreeCell<ITimeGraphEntry> call(TreeView<ITimeGraphEntry> p) {
                return new TreeCell<ITimeGraphEntry>() {
                    @Override
                    public void updateItem(ITimeGraphEntry item, boolean empty) {
                        if(item != null && (item == FILLER || item.equals(FILLER))) {
                            setText(null);
                            return;
                        }

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        //final Node scrollBar = fTreeViewer.lookup(".scroll-bar:vertical");
        //scrollBar.setDisable(true);

        fTreeRoot = new TreeItem<>(new TimeGraphEntry("Root", 0 , 0));//$NON-NLS-1$

        vBoxTree.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        vBoxTree.setMinWidth(200);
        VBox.setVgrow(fTreeViewer, Priority.ALWAYS);
        fTreeViewer.setMaxHeight(Double.MAX_VALUE);

        vBoxTree.getChildren().add(fTreeViewer);
        splitPane.getItems().add(vBoxTree);

        //tree.setLinesVisible(true);
        fTimeGraphViewer = new TimeGraphViewer(splitPane);
        fTimeGraphViewer.setItemHeight(fItemHeight);
        //fTimeGraphViewer.setBorderWidth(fTreeViewer.getBorderWidth());
        fTimeGraphViewer.setNameWidthPref(0);

        fFilter = new RawViewerFilter();
        addFilter(fFilter);

        fFilterDialog = new TimeGraphFilterDialog(getShell());

        // ensure synchronization of expanded items between tree and time graph
        fTimeGraphViewer.addTreeListener(new ITimeGraphTreeListener() {
            @Override
            public void treeCollapsed(TimeGraphTreeExpansionEvent event) {
                setTreeExpandedState(event.getEntry(), false);
                alignTreeItems(true);
            }

            @Override
            public void treeExpanded(TimeGraphTreeExpansionEvent event) {
                ITimeGraphEntry entry = event.getEntry();
                setTreeExpandedState(entry, true);
                Set<Object> expandedElements = new HashSet<>(Arrays.asList(getExpandedElements(new ArrayList(treeItems.keySet()))));
                for (ITimeGraphEntry child : entry.getChildren()) {
                    if (child.hasChildren()) {
                        boolean expanded = expandedElements.contains(child);
                        fTimeGraphViewer.setExpandedState(child, expanded);
                    }
                }
                alignTreeItems(true);
            }
        });

        // prevent mouse button from selecting a filler tree item
        fTreeViewer.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                TreeItem<ITimeGraphEntry> treeItem = fTreeViewer.getSelectionModel().getSelectedItem();
                        //fTreeRoot.getItem(new Point(event.x, event.y));
                if (treeItem == null || treeItem.getValue() == FILLER) {
                    e.consume();
                    List<TreeItem<ITimeGraphEntry>> expandedTreeItems = getVisibleExpandedItems(false);
                    if (expandedTreeItems.size() == 0) {
                        fTreeViewer.getSelectionModel().clearSelection();
                        fTimeGraphViewer.setSelection(null);
                        return;
                    }
                    // this prevents from scrolling up when selecting
                    // the partially visible tree item at the bottom
                    //tree.select(expandedTreeItems.get(expandedTreeItems.size() - 1));
                    fTreeViewer.getSelectionModel().clearSelection();
                    fTimeGraphViewer.setSelection(null);
                }
            }
        });

        // prevent mouse wheel from scrolling down into filler tree items
        fTreeViewer.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                scrollEvent.consume();
                double pixelScrolled = scrollEvent.getDeltaY();
                int lineScrolled = (int) (pixelScrolled / fItemHeight);
                ScrollBar scrollBar = fTimeGraphViewer.getVerticalBar();
                fTimeGraphViewer.setTopIndex((int)scrollBar.getValue() - lineScrolled);
                alignTreeItems(false);
            }
        });

        // prevent key stroke from selecting a filler tree item
        fTreeViewer.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                List<TreeItem<ITimeGraphEntry>> expandedTreeItems = getVisibleExpandedItems(false);
                if (treeItems.size() == 0) {
                    fTreeViewer.getSelectionModel().clearSelection();
                    event.consume();
                    return;
                }
                if (event.getCode() == KeyCode.DOWN) {
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + 1, expandedTreeItems.size() - 1);
                    fTimeGraphViewer.setSelection(expandedTreeItems.get(index).getValue());
                    event.consume();
                } else if (event.getCode() == KeyCode.PAGE_DOWN) {
                    int height = (int)fTreeViewer.getHeight();//tree.getSize().y - tree.getHeaderHeight() - tree.getHorizontalBar().getSize().y;
                    int countPerPage = height / getItemHeight();
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + countPerPage - 1, expandedTreeItems.size() - 1);
                    fTimeGraphViewer.setSelection(expandedTreeItems.get(index).getValue());
                    event.consume();
                } else if (event.getCode() == KeyCode.END) {
                    fTimeGraphViewer.setSelection(expandedTreeItems.get(expandedTreeItems.size() - 1).getValue());
                    event.consume();
                }
                if (fTimeGraphViewer.getSelectionIndex() >= 0) {
                    fTreeViewer.getSelectionModel().select(treeItems.get(fTimeGraphViewer.getSelection()));
                } else {
                    fTreeViewer.getSelectionModel().selectFirst();
                }
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        /*fTimeGraphViewer.getTimeGraphControl().setOnaddControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                alignTreeItems(false);
            }
        });*/

        // ensure synchronization of selected item between tree and time graph
        fTreeViewer.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ITimeGraphEntry>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<ITimeGraphEntry>> arg0, TreeItem<ITimeGraphEntry> arg1, TreeItem<ITimeGraphEntry> arg2) {
                if (fInhibitTreeSelection) {
                    return;
                }

                if (fTreeViewer.getSelectionModel().getSelectedItem() != null) {
                    Object selection = fTreeViewer.getSelectionModel().getSelectedItem().getValue();
                    if (selection instanceof ITimeGraphEntry && selection != FILLER) {
                        fTimeGraphViewer.setSelection((ITimeGraphEntry) selection);
                    }
                }
                alignTreeItems(false);
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTimeGraphViewer.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                ITimeGraphEntry entry = fTimeGraphViewer.getSelection();
                fInhibitTreeSelection = true; // block the tree selection
                                              // changed listener
                if (entry != null) {
                    fTreeViewer.getSelectionModel().select(treeItems.get(entry));
                } else {
                    fTreeViewer.getSelectionModel().clearSelection();
                }
                fInhibitTreeSelection = false;
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getVerticalBar().setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent arg0) {
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) {
                if(fTimeGraphViewer.getTimeGraphControl().scrolled(e))
                {
                    fTimeGraphViewer.adjustVerticalScrollBar();
                    alignTreeItems(false);
                } else {
                    fTimeGraphViewer.adjustVerticalScrollBar();
                }
            }
        });

        // ensure the tree has focus control when mouse is over it if the time
        // graph had control
       /* fTreeViewer.getControl().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                if (fTimeGraphViewer.getTimeGraphControl().isFocusControl()) {
                    fTreeViewer.setFocus();
                }
            }
        });*/

        // ensure the time graph has focus control when mouse is over it if the
        // tree had control
       /* fTimeGraphViewer.getTimeGraphControl().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                if (fTreeViewer.isFocused()) {
                    fTimeGraphViewer.getTimeGraphControl().setFocus();
                }
            }
        });
        fTimeGraphViewer.getTimeGraphScale().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                if (fTreeViewer.isFocused()) {
                    fTimeGraphViewer.getTimeGraphControl().setFocus();
                }
            }
        });*/

        // The filler rows are required to ensure alignment when the tree does
        // not have a visible horizontal scroll bar. The tree does not allow its
        // top item to be set to a value that would cause blank space to be
        // drawn at the bottom of the tree.
        fNumFillerRows = Display.getDefault().getBounds().height / getItemHeight();

        SplitPane.setResizableWithParent(vBoxTree, Boolean.FALSE);

        splitPane.setMinHeight(100);
        splitPane.setDividerPositions(weights[0]);//, weights[1]);
        root.getChildren().add(splitPane);

       /* addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (getClientArea().width > 0 && getClientArea().height > 0) {
                    //splitPane.setPrefSize(getClientArea().width, getClientArea().height);
                    splitPane.setDividerPositions(weights[0]);
                    //vBoxTree.setPrefSize(getClientArea().width / 3.0, getClientArea().height);
                    fTreeViewer.setPrefHeight(splitPane.getHeight());
                    fTreeViewer.setPrefWidth(vBoxTree.getWidth());
                    //if (treeHeader.getHeight() > 0) {
                   //     fTimeGraphViewer.setHeaderHeight((int) treeHeader.getHeight());
                   // }
                    //vBoxGraph.setPrefSize(2.0 * (getClientArea().width / 3.0), getClientArea().height);
                }
            }
        });*/
    }

    // @Framesoc
 /*  private void collapseTimeGraphTree(ITimeGraphEntry entry) {
        fTimeGraphViewer.setExpandedState(entry, false);
        // queue the alignment update because the tree items may only be
        // actually collapsed after the listeners have been notified
        fVisibleExpandedItems = null; // invalidate the cache
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                alignTreeItems(true);
            }
        });
    }

    // @Framesoc
    private void expandTimeGraphTree(ITimeGraphEntry entry) {
        fTimeGraphViewer.setExpandedState(entry, true);
        List<ITimeGraphEntry> initList = fTreeRoot.getChildren().stream().map(treeItem -> treeItem.getValue()).collect(Collectors.toList());
        Set<Object> expandedElements = new HashSet<>(getExpandedElements(initList));
        for (ITimeGraphEntry child : entry.getChildren()) {
            if (child.hasChildren()) {
                boolean expanded = expandedElements.contains(child);
                fTimeGraphViewer.setExpandedState(child, expanded);
            }
        }
        // queue the alignment update because the tree items may only be
        // actually expanded after the listeners have been notified
        fVisibleExpandedItems = null; // invalidate the cache
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                alignTreeItems(true);
            }
        });
    }
*/
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns this time graph combo's tree viewer.
     *
     * @return the tree viewer
     */
    public TreeView<ITimeGraphEntry> getTreeViewer() {
        return fTreeViewer;
    }

    /**
     * Returns this time graph combo's time graph viewer.
     *
     * @return the time graph viewer
     */
    public TimeGraphViewer getTimeGraphViewer() {
        return fTimeGraphViewer;
    }

    /**
     * @Framesoc Returns this time graph producer filter dialog
     * @return the time graph producer filter dialog
     */
    public TimeGraphFilterDialog getFilterDialog() {
        return fFilterDialog;
    }

    /**
     * @return the hashmap of the items in the trees
     */
    public Map<ITimeGraphEntry, TreeItem<ITimeGraphEntry>> getTreeItems() {
        return treeItems;
    }

    /**
     * Callback for the show filter action
     *
     * @since 2.0
     */
    public void showFilterDialog() {
        ITimeGraphEntry[] topInput = fTimeGraphViewer.getTimeGraphContentProvider().getElements(fTimeGraphViewer.getInput());
        if (topInput != null) {
            List<? extends ITimeGraphEntry> allElements = listAllInputs(Arrays.asList(topInput));
            fFilterDialog.setInput(fTimeGraphViewer.getInput());
            fFilterDialog.setTitle(Messages.TmfTimeFilterDialog_WINDOW_TITLE);
            fFilterDialog.setMessage(Messages.TmfTimeFilterDialog_MESSAGE);
            fFilterDialog.setExpandedElements(allElements.toArray());
            if (fFilter.getFiltered() != null) {
                ArrayList<? extends ITimeGraphEntry> nonFilteredElements = new ArrayList<>(allElements);
                nonFilteredElements.removeAll(fFilter.getFiltered());
                fFilterDialog.setInitialElementSelections(nonFilteredElements);
            } else {
                fFilterDialog.setInitialElementSelections(allElements);
            }
            fFilterDialog.create();

            // reset checked status, managed manually @Framesoc
            showFilterAction.setChecked(!showFilterAction.isChecked());

            fFilterDialog.open();
            // Process selected elements
            if (fFilterDialog.getResult() != null) {
                fInhibitTreeSelection = true;
                if (fFilterDialog.getResult().length != allElements.size()) {
                    // @Framesoc
                    checkProducerFilter(true);
                    ArrayList<Object> filteredElements = new ArrayList<>(allElements);
                    filteredElements.removeAll(Arrays.asList(fFilterDialog.getResult()));
                    fFilter.setFiltered(filteredElements);
                } else {
                    // @Framesoc
                    checkProducerFilter(false);
                    fFilter.setFiltered(null);
                }
                setTreeExpandedState(true);
                fTimeGraphViewer.refresh();
                fInhibitTreeSelection = false;
                alignTreeItems(true);
                // Reset selection
                if (fFilterDialog.getResult().length > 0) {
                    setSelection(null);
                }
            }
        }
    }

    // @Framesoc
    private void checkProducerFilter(boolean check) {
        if (check) {
            showFilterAction.setChecked(true);
            showFilterAction.setToolTipText(Messages.TmfTimeGraphCombo_FilterActionToolTipText
                    + " (filter applied)"); //$NON-NLS-1$
        } else {
            showFilterAction.setChecked(false);
        }
    }

    /**
     * Get the filtered entries
     *
     * @return the filtered entries
     * @Framesoc
     */
    public List<Object> getFilteredEntries() {
        return (fFilter.getFiltered() != null) ? fFilter.getFiltered() : new ArrayList<>();
    }

    /**
     * Set the filtered entries
     *
     * @param filtered
     *            the filtered entries to set
     * @Framesoc
     */
    public void setFilteredEntries(List<Object> filtered) {
        fFilter.setFiltered(filtered);
    }

    // /**
    // * Filter the entry and its children
    // *
    // * @param entry
    // * entry to filter
    // * @Framesoc
    // */
    // public void filterEntry(ITimeGraphEntry entry) {
    // fInhibitTreeSelection = true;
    // addFiltered(entry);
    // fTreeViewer.refresh();
    // fTreeViewer.expandAll();
    // fTimeGraphViewer.refresh();
    // fInhibitTreeSelection = false;
    // alignTreeItems(true);
    // setSelection(null);
    // }

    /**
     * Add the entry and all its children to the filtered entries.
     *
     * @param entry
     *            entry to filter
     * @Framesoc
     */
    public void addFiltered(ITimeGraphEntry entry) {
        if (fFilter.getFiltered() == null) {
            fFilter.setFiltered(new ArrayList<>());
        }
        fFilter.getFiltered().add(entry);
        for (ITimeGraphEntry e : entry.getChildren()) {
            addFiltered(e);
        }
    }

    /**
     * Get the show filter action.
     *
     * @return The Action object
     * @since 2.0
     */
    public Action getShowFilterAction() {
        if (showFilterAction == null) {
            // showFilter
            showFilterAction = new Action("", IAction.AS_CHECK_BOX) { // @Framesoc //$NON-NLS-1$
                @Override
                public void run() {
                    showFilterDialog();
                }
            };
            showFilterAction.setText(Messages.TmfTimeGraphCombo_FilterActionNameText);
            showFilterAction.setToolTipText(Messages.TmfTimeGraphCombo_FilterActionToolTipText);
            // TODO find a nice, distinctive icon
            showFilterAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
        }

        return showFilterAction;
    }

    // ------------------------------------------------------------------------
    // Control
    // ------------------------------------------------------------------------

    @Override
    public void redraw() {
        if(fTimeGraphViewer != null) {
            fTimeGraphViewer.redraw();
        }

        super.redraw();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the tree content provider used by this time graph combo.
     *
     * @param contentProvider
     *            the tree content provider
     */
    public void setTreeContentProvider(ITreeContentProvider contentProvider) {
      //  fTreeViewer.setContentProvider(new TreeContentProviderWrapper(contentProvider));
    }

    /**
     * Sets the tree label provider used by this time graph combo.
     *
     * @param labelProvider
     *            the tree label provider
     */
    public void setTreeLabelProvider(ITableLabelProvider labelProvider) {
     //   fTreeViewer.setLabelProvider(new TreeLabelProviderWrapper(labelProvider));
    }

    /**
     * Sets the tree content provider used by the filter dialog
     *
     * @param contentProvider
     *            the tree content provider
     * @since 2.0
     */
    public void setFilterContentProvider(ITreeContentProvider contentProvider) {
        fFilterDialog.setContentProvider(contentProvider);
    }

    /**
     * Sets the tree label provider used by the filter dialog
     *
     * @param labelProvider
     *            the tree label provider
     * @since 2.0
     */
    public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
        fFilterDialog.setLabelProvider(labelProvider);
    }


    /**
     * Sets the time graph content provider used by this time graph combo.
     *
     * @param timeGraphContentProvider
     *            the time graph content provider
     *
     * @since 3.0
     */
    public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
        fTimeGraphViewer.setTimeGraphContentProvider(timeGraphContentProvider);
    }

    /**
     * Sets the time graph presentation provider used by this time graph combo.
     *
     * @param timeGraphProvider
     *            the time graph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphViewer.setTimeGraphProvider(timeGraphProvider);
    }

    /**
     * Sets or clears the input for this time graph combo.
     *
     * @param input
     *            the input of this time graph combo, or <code>null</code> if
     *            none
     *
     * @since 3.0
     */
    public void setInput(Object input) {
        fFilter.setFiltered(null);
        fInhibitTreeSelection = true;
        setTreeInput(input);
        fTreeViewer.setRoot(fTreeRoot);
        for (SelectionListenerWrapper listenerWrapper : fSelectionListenerMap.values()) {
            listenerWrapper.selection = null;
        }
        fInhibitTreeSelection = false;
        //fTreeViewer.getTree().getVerticalBar().setEnabled(false);
        //fTreeViewer.getTree().getVerticalBar().setVisible(false);
        fTimeGraphViewer.setItemHeight(fItemHeight);
        fTimeGraphViewer.setInput(input);
        // queue the alignment update because in Linux the item bounds are not
        // set properly until the tree has been painted at least once
        fVisibleExpandedItems = null; // invalidate the cache
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                alignTreeItems(true);
            }
        });
    }

    private void setTreeInput(Object input) {
        fTreeRoot.getChildren().clear();
        List<ITimeGraphEntry> entries = (List<ITimeGraphEntry>) input;
        List<ITimeGraphEntry> topEntries = entries.stream().filter(entry -> entry.getParent() == null).collect(Collectors.toList());
        buildTreeItems(topEntries);
        // Build filler row items (use to avoid bad alignment with the graph
        // when there is a horizontal scrollbar
        for (int i = 0; i < fNumFillerRows; i++) {
            fTreeRoot.getChildren().add(new TreeItem<>(FILLER));
        }
        setTreeItemsHeight();
        setTreeExpandedState(true);
    }

    private void buildTreeItems(List<? extends ITimeGraphEntry> entries) {
        for (ITimeGraphEntry entry : entries) {
            if(fFilter.getFiltered() != null && fFilter.getFiltered().contains(entry)) {
                continue;
            }

            TreeItem<ITimeGraphEntry> item = new TreeItem<>();

            // Add listener
            item.expandedProperty().addListener(new TreeItemListener());

            item.setValue(entry);
            treeItems.put(entry, item);

            // Set parent
            if (entry.getParent() == null) {
                fTreeRoot.getChildren().add(item);
            } else {
                treeItems.get(entry.getParent()).getChildren().add(item);
            }

            // Build children
            if (!entry.getChildren().isEmpty()) {
                buildTreeItems(entry.getChildren());
            }
        }
    }

    /**
     * Gets the input for this time graph combo.
     *
     * @return The input of this time graph combo, or <code>null</code> if none
     *
     * @since 3.0
     */
    public Object getInput() {
        return treeItems.keySet();
    }

    /**
     * Sets or clears the list of links to display on this combo
     *
     * @param links
     *            the links to display in this time graph combo
     * @since 2.1
     */
    public void setLinks(List<ILinkEvent> links) {
        fTimeGraphViewer.setLinks(links);
    }

    /**
     * @param filter
     *            The filter object to be attached to the view
     * @since 2.0
     */
    public void addFilter(ViewerFilter filter) {
        ViewerFilter wrapper = new ViewerFilterWrapper(filter);
        //fTreeViewer.addFilter(wrapper);
        fTimeGraphViewer.addFilter(wrapper);
        fViewerFilterMap.put(filter, wrapper);
        alignTreeItems(true);
    }

    /**
     * @param filter
     *            The filter object to be removed from the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        ViewerFilter wrapper = fViewerFilterMap.get(filter);
       //fTreeViewer.removeFilter(wrapper);
        fTimeGraphViewer.removeFilter(wrapper);
        fViewerFilterMap.remove(filter);
        alignTreeItems(true);
    }

    /**
     * Refreshes this time graph completely with information freshly obtained
     * from its model.
     */
    public void refresh() {
        fInhibitTreeSelection = true;
        setTreeExpandedState(true);
        fTimeGraphViewer.refresh();
        alignTreeItems(true);
        fInhibitTreeSelection = false;
    }

    /**
     * Adds a listener for selection changes in this time graph combo.
     *
     * @param listener
     *            a selection listener
     */
    public void addSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = new SelectionListenerWrapper(listener);
        //fTreeViewer.addSelectionChangedListener(listenerWrapper);
        fSelectionListenerMap.put(listener, listenerWrapper);
        fTimeGraphViewer.addSelectionListener(listenerWrapper);
    }

    /**
     * Removes the given selection listener from this time graph combo.
     *
     * @param listener
     *            a selection changed listener
     */
    public void removeSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = fSelectionListenerMap.remove(listener);
       // fTreeViewer.removeSelectionChangedListener(listenerWrapper);
        fTimeGraphViewer.removeSelectionListener(listenerWrapper);
    }

    /**
     * Sets the current selection for this time graph combo.
     *
     * @param selection
     *            the new selection
     */
    public void setSelection(ITimeGraphEntry selection) {
        fTimeGraphViewer.setSelection(selection);
        fInhibitTreeSelection = true; // block the tree selection changed
                                      // listener
        if (selection != null) {
            fTreeViewer.getSelectionModel().select(treeItems.get(selection));//setSelection(structuredSelection);
        } else {
            fTreeViewer.getSelectionModel().clearSelection();
        }
        fInhibitTreeSelection = false;
        alignTreeItems(false);
    }

    /**
     * Set the expanded state of an entry
     *
     * @param entry
     *            The entry to expand/collapse
     * @param expanded
     *            True for expanded, false for collapsed
     *
     * @since 2.0
     */
    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        fTimeGraphViewer.setExpandedState(entry, expanded);
        setTreeExpandedState(entry, expanded);
        alignTreeItems(true);
    }

    /**
     * Expand or collapse the item specified by the entry
     *
     * @param entry
     *            the entry whose state is to be modified
     * @param expanded
     *            true if the item should be expanded, false otherwise
     */
    public void setTreeExpandedState(ITimeGraphEntry entry, boolean expanded) {
        if (treeItems.containsKey(entry)) {
            treeItems.get(entry).setExpanded(expanded);
        }
    }

    /**
     * Expand or collapse all the items of the tree
     *
     * @param expanded
     *            true if the items should be expanded, false otherwise
     */
    public void setTreeExpandedState(boolean expanded) {
        if (expanded) {
            for (TreeItem<ITimeGraphEntry> treeItem : treeItems.values()) {
                if (!treeItem.isLeaf()) {
                    treeItem.setExpanded(true);
                }
            }
        } else {
            // Just collapse the root
            fTreeRoot.setExpanded(false);
        }
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        fTimeGraphViewer.collapseAll();
        setTreeExpandedState(false);
        alignTreeItems(true);
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void expandAll() {
        fTimeGraphViewer.expandAll();
        setTreeExpandedState(true);
        alignTreeItems(true);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private List<TreeItem<ITimeGraphEntry>> getVisibleExpandedItems(boolean refresh) {
        if (fVisibleExpandedItems == null || refresh) {
            ArrayList<TreeItem<ITimeGraphEntry>> items = new ArrayList<>();
            for (TreeItem<ITimeGraphEntry> item : treeItems.values()) {
                if (item.getValue() == FILLER) {
                    break;
                }
                items.add(item);
                if (item.isExpanded()) {
                    addVisibleExpandedItems(items, item);
                }
            }
            fVisibleExpandedItems = items;
        }
        return fVisibleExpandedItems;
    }

    private void addVisibleExpandedItems(List<TreeItem<ITimeGraphEntry>> items, TreeItem<ITimeGraphEntry> treeItem) {
        for (TreeItem<ITimeGraphEntry> item :treeItem.getChildren()) {
            items.add(item);
            if (item.isExpanded()) {
                addVisibleExpandedItems(items, item);
            }
        }
    }

    /**
     * Explores the list of top-level inputs and returns all the inputs
     *
     * @param inputs
     *            The top-level inputs
     * @return All the inputs
     */
    private List<? extends ITimeGraphEntry> listAllInputs(List<? extends ITimeGraphEntry> inputs) {
        ArrayList<ITimeGraphEntry> items = new ArrayList<>();
        for (ITimeGraphEntry entry : inputs) {
            items.add(entry);
            if (entry.hasChildren()) {
                items.addAll(listAllInputs(entry.getChildren()));
            }
        }
        return items;
    }

    private List<? extends TreeItem<ITimeGraphEntry>> getExpandedElements(List<? extends ITimeGraphEntry> inputs) {
        ArrayList<TreeItem<ITimeGraphEntry>> items = new ArrayList<>();
        for (ITimeGraphEntry entry : inputs) {

            if(treeItems.get(entry).isExpanded() || treeItems.get(entry).getChildren().isEmpty()) {
                items.add(treeItems.get(entry));
            }

            if (treeItems.get(entry).isExpanded() && !treeItems.get(entry).getChildren().isEmpty()) {
                items.addAll(getExpandedElements(entry.getChildren()));
            }
        }
        return items;
    }

    private int getItemHeight() {
        return fItemHeight;
    }

    /**
     * Set the size of the item in the tree view
     */
    public void setTreeItemsHeight() {
        fTreeViewer.setFixedCellSize(fItemHeight);
        // Set font size
        fTreeViewer.setStyle("-fx-font-size: " + fItemHeight/2 + ";"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Decrease the size of the object in the tree view of one pixel
     */
    public void verticalZoomOut() {
        if(fItemHeight <= MINIMAL_ITEM_HEIGHT) {
            return;
        }

        fItemHeight--;
        setTreeItemsHeight();
    }

    /**
     * Increase the size of the object in the tree view of one pixel
     */
    public void verticalZoomIn() {
        fItemHeight++;
        setTreeItemsHeight();
    }

    /**
     * Reset the size of the object in the tree view to default
     */
    public void resetVerticalZoom() {
        fItemHeight = DEFAULT_TREE_ITEM_HEIGHT;
        setTreeItemsHeight();
    }

    private void alignTreeItems(boolean refreshExpandedItems) {
        // align the tree top item with the time graph top item
        List<TreeItem<ITimeGraphEntry>> expandedTreeItems = getVisibleExpandedItems(refreshExpandedItems);
        int topIndex = fTimeGraphViewer.getTopIndex();
        if (topIndex >= expandedTreeItems.size()) {
            return;
        }

        // Set the time scale to the same size as the tree header
        fTimeGraphViewer.setHeaderHeight((int) treeHeader.getHeight());
        layout();
        update();
        fTimeGraphViewer.resizeControls();
        fTimeGraphViewer.refresh();

        fTreeViewer.scrollTo(topIndex);
    }

    /**
     * Listener handling with the expansion of an item in the tree
     *
     */
    public class TreeItemListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean arg1, Boolean newValue) {
            BooleanProperty bb = (BooleanProperty) observable;
            TreeItem<ITimeGraphEntry> t = (TreeItem<ITimeGraphEntry>) bb.getBean();
            fTimeGraphViewer.setExpandedState(t.getValue(), newValue);
            // @Framesoc
          /*  if ( (Boolean) newValue) {
                collapseTimeGraphTree((ITimeGraphEntry) t.getValue());
            } else {
                expandTimeGraphTree((ITimeGraphEntry) t.getValue());
            }*/
        }
    }

}
