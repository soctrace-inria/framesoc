package fr.inria.soctrace.framesoc.ui.treefilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.model.IModelElementNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;

/**
 * Manager class for filter data.
 * 
 * This class stores the data needed to display and process filters on a dimension like event
 * producers or event types. It provides the methods for creating the corresponding actions and
 * dialogs.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class FilterDataManager {

	/**
	 * Filter status
	 */
	private static enum FilterStatus {
		UNSET, SET, APPLIED;
	}

	private FilterDimensionData dimensionData;
	private IAction filterAction;
	private TreeFilterDialog filterDialog;
	private ITreeNode[] roots;
	private List<Object> checked;
	private List<Object> allElements;

	/**
	 * Constructor
	 * 
	 * @param dimensionData
	 *            dimension managed by the filter
	 */
	public FilterDataManager(FilterDimensionData dimensionData) {
		this.dimensionData = dimensionData;
	}

	/**
	 * @return the configuration dimension
	 */
	public FilterDimension getDimension() {
		return dimensionData.getFilterDimension();
	}

	/**
	 * Initialize the filter action
	 * 
	 * @return the action
	 */
	public IAction initFilterAction() {
		filterAction = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				showFilterAction();
			}
		};
		filterAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/" + dimensionData.getIconName()));
		filterAction.setToolTipText(dimensionData.getActionToolTipMessage());
		return filterAction;
	}

	/**
	 * Initialize the filter dialog
	 * 
	 * @param shell
	 *            the shell
	 */
	public void initFilterDialog(Shell shell) {
		filterDialog = new TreeFilterDialog(shell);
		filterDialog.setColumnNames(new String[] { dimensionData.getName() });
		filterDialog.setContentProvider(new TreeContentProvider());
		filterDialog.setLabelProvider(dimensionData.getLabelProvider());
	}

	/**
	 * Set the input for the filter tree viewer.
	 * 
	 * @param rootNodes
	 *            the array of tree roots
	 */
	public void setFilterRoots(ITreeNode[] rootNodes) {
		roots = rootNodes;
		checked = TreeFilterDialog.listAllInputs(Arrays.asList(roots));
		allElements = new ArrayList<>(checked);
	}

	/**
	 * Get the id of the configuration dimension entity corresponding to checked items (e.g.,
	 * checked event producers ids).
	 * 
	 * @return
	 */
	public List<Integer> getCheckedId() {
		List<Integer> toLoad = new ArrayList<Integer>(checked.size());
		for (Object o : checked) {
			if (!(o instanceof IModelElementNode)) {
				continue;
			}
			toLoad.add(((IModelElementNode) o).getId());
		}
		return toLoad;
	}

	/**
	 * Handler called after the filtering configuration has been changed.
	 */
	public abstract void reloadAfterChange();

	/*
	 * Utilities
	 */

	private void showFilterAction() {

		if (roots.length > 0) {
			filterDialog.setInput(roots);
			filterDialog.setTitle(dimensionData.getName() + " Filter");
			filterDialog.setMessage(dimensionData.getDialogMessage());
			filterDialog.setExpandedElements(allElements.toArray());
			filterDialog.setInitialElementSelections(checked);
			filterDialog.create();

			// reset checked status, managed manually
			filterAction.setChecked(!filterAction.isChecked());

			// open the dialog
			if (filterDialog.open() != Window.OK) {
				return;
			}

			// Process selected elements
			if (filterDialog.getResult() != null) {
				List<Object> currentChecked = Arrays.asList(filterDialog.getResult());
				if (areListsEqual(allElements, currentChecked)) {
					// all checked
					if (areListsEqual(allElements, checked)) {
						updateFilter(FilterStatus.UNSET);
					} else {
						// the loaded data is with unchecked elements
						updateFilter(FilterStatus.SET);
						checked = currentChecked;
						reloadAfterChange();
						updateFilter(FilterStatus.UNSET);
					}
				} else if (areListsEqual(checked, currentChecked)) {
					updateFilter(FilterStatus.APPLIED);
				} else {
					updateFilter(FilterStatus.SET);
					checked = currentChecked;
					reloadAfterChange();
					updateFilter(FilterStatus.APPLIED);
				}
			}
		}
	}

	private void updateFilter(FilterStatus status) {
		StringBuilder icon = new StringBuilder("icons/");
		StringBuilder tooltip = new StringBuilder("Show " + dimensionData.getName() + " Filter");

		switch (status) {
		case APPLIED:
			filterAction.setChecked(true);
			icon.append(dimensionData.getAppliedIconName());
			tooltip.append(" (filter applied)");
			break;
		case SET:
			filterAction.setChecked(false);
			icon.append(dimensionData.getSetIconName());
			tooltip.append(" (filter set but not applied)");
			break;
		case UNSET:
			filterAction.setChecked(false);
			icon.append(dimensionData.getIconName());
			break;
		}

		filterAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, icon.toString()));
		filterAction.setToolTipText(tooltip.toString());
	}

	private boolean areListsEqual(List<Object> l1, List<Object> l2) {
		if (l1 == null) {
			return l2 == null;
		}
		if (l2 == null) {
			return l1 == null;
		}
		Set<Object> s1 = new HashSet<>(l1);
		Set<Object> s2 = new HashSet<>(l2);
		return s1.equals(s2);
	}

}
