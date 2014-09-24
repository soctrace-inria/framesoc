/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Moved SelectTraceTypeContributionItem to this class
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.ui.project.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import fr.inria.linuxtools.tmf.core.project.model.TmfTraceType;
import fr.inria.linuxtools.tmf.core.project.model.TmfTraceType.TraceElementType;
import fr.inria.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import fr.inria.linuxtools.tmf.ui.project.model.TmfTraceElement;
import fr.inria.linuxtools.tmf.ui.project.model.TmfTraceTypeUIUtils;

/**
 * ContributionItem for the element type selection.
 *
 * @author Patrick Tassé
 */
public class SelectElementTypeContributionItem extends CompoundContributionItem {

    private static final ImageDescriptor SELECTED_ICON = Activator.getDefault().getImageDescripterFromPath("icons/elcl16/bullet.gif"); //$NON-NLS-1$
    private static final String BUNDLE_PARAMETER = "fr.inria.linuxtools.tmf.ui.commandparameter.select_trace_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "fr.inria.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "fr.inria.linuxtools.tmf.ui.commandparameter.select_trace_type.icon"; //$NON-NLS-1$
    private static final String SELECT_TRACE_TYPE_COMMAND_ID = "fr.inria.linuxtools.tmf.ui.command.select_trace_type"; //$NON-NLS-1$
    private static final String CUSTOM_TXT_CATEGORY = "Custom Text"; //$NON-NLS-1$
    private static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {

        /*
         * Fill the selected trace types and verify if selection applies only to
         * either traces or experiments
         */
        Set<String> selectedTraceTypes = new HashSet<>();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ISelection selection = page.getSelection();
        boolean forTraces = false, forExperiments = false;
        if (selection instanceof StructuredSelection) {
            for (Object element : ((StructuredSelection) selection).toList()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    selectedTraceTypes.add(trace.getTraceType());
                    forTraces = true;
                } else if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement exp = (TmfExperimentElement) element;
                    selectedTraceTypes.add(exp.getTraceType());
                    forExperiments = true;
                }
            }
        }

        if (forTraces && forExperiments) {
            /* This should never happen anyways */
            throw new RuntimeException("You must select only experiments or only traces to set the element type"); //$NON-NLS-1$
        }

        return getContributionItems(selectedTraceTypes, forExperiments);
    }

    /**
     * Get the contribution items for traces
     *
     * @param selectedTraceTypes
     *            The set of selected trace types
     * @param forExperiments
     *            <code>true</code> if the contribution items are requested for
     *            experiments, <code>false</code> for traces
     *
     * @return The list of contribution items
     */
    protected IContributionItem[] getContributionItems(Set<String> selectedTraceTypes, boolean forExperiments) {

        String ceType = forExperiments ? TmfTraceType.EXPERIMENT_ELEM : TmfTraceType.TYPE_ELEM;
        TraceElementType elementType = forExperiments ? TraceElementType.EXPERIMENT : TraceElementType.TRACE;

        List<IContributionItem> list = new LinkedList<>();

        Map<String, MenuManager> categoriesMap = new HashMap<>();
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
                TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TmfTraceType.CATEGORY_ELEM)) {
                String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                ImageDescriptor icon = isSelectedCategory(categoryId, config, selectedTraceTypes) ? SELECTED_ICON : null;
                MenuManager subMenu = new MenuManager(ce.getAttribute(TmfTraceType.NAME_ATTR), icon, null);
                categoriesMap.put(categoryId, subMenu);
                list.add(subMenu);
            }
        }

        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(ceType)) {
                String traceBundle = ce.getContributor().getName();
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                String label = ce.getAttribute(TmfTraceType.NAME_ATTR).replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
                boolean selected = selectedTraceTypes.contains(traceTypeId);
                MenuManager subMenu = categoriesMap.get(ce.getAttribute(TmfTraceType.CATEGORY_ATTR));

                /* Get the icon from the tmftracetypeui extension, if it exists */
                String traceIcon = null;
                IConfigurationElement uiCE = TmfTraceTypeUIUtils.getTraceUIAttributes(traceTypeId, elementType);
                if (uiCE != null) {
                    traceIcon = uiCE.getAttribute(TmfTraceTypeUIUtils.ICON_ATTR);
                }

                addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
            }
        }

        if (forExperiments) {
            return list.toArray(new IContributionItem[list.size()]);
        }

        /*
         * Add the custom txt and xml trace type to the contribution items for
         * traces
         */
        CustomTxtTraceDefinition[] customTxtTraceDefinitions = CustomTxtTraceDefinition.loadAll();
        if (customTxtTraceDefinitions.length > 0) {
            ImageDescriptor icon = isSelectedCategory(customTxtTraceDefinitions, selectedTraceTypes) ? SELECTED_ICON : null;
            MenuManager subMenu = new MenuManager(CUSTOM_TXT_CATEGORY, icon, null);
            categoriesMap.put(CUSTOM_TXT_CATEGORY, subMenu);
            list.add(subMenu);
        }
        CustomXmlTraceDefinition[] customXmlTraceDefinitions = CustomXmlTraceDefinition.loadAll();
        if (customXmlTraceDefinitions.length > 0) {
            ImageDescriptor icon = isSelectedCategory(customXmlTraceDefinitions, selectedTraceTypes) ? SELECTED_ICON : null;
            MenuManager subMenu = new MenuManager(CUSTOM_XML_CATEGORY, icon, null);
            categoriesMap.put(CUSTOM_XML_CATEGORY, subMenu);
            list.add(subMenu);
        }

        // add the custom trace types
        for (CustomTxtTraceDefinition def : customTxtTraceDefinitions) {
            String traceBundle = Activator.getDefault().getBundle().getSymbolicName();
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + ':' + def.definitionName;
            String traceIcon = DEFAULT_TRACE_ICON_PATH;
            String label = def.definitionName;
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            MenuManager subMenu = categoriesMap.get(CUSTOM_TXT_CATEGORY);

            addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
        }
        for (CustomXmlTraceDefinition def : customXmlTraceDefinitions) {
            String traceBundle = Activator.getDefault().getBundle().getSymbolicName();
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + ':' + def.definitionName;
            String traceIcon = DEFAULT_TRACE_ICON_PATH;
            String label = def.definitionName;
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            MenuManager subMenu = categoriesMap.get(CUSTOM_XML_CATEGORY);

            addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
        }

        return list.toArray(new IContributionItem[list.size()]);
    }

    private static void addContributionItem(List<IContributionItem> list,
            String traceBundle, String traceTypeId, String traceIcon,
            String label, boolean selected,
            MenuManager subMenu) {
        Map<String, String> params;

        params = new HashMap<>();
        params.put(BUNDLE_PARAMETER, traceBundle);
        params.put(TYPE_PARAMETER, traceTypeId);
        params.put(ICON_PARAMETER, traceIcon);

        ImageDescriptor icon = null;
        if (selected) {
            icon = SELECTED_ICON;
        }

        CommandContributionItemParameter param = new CommandContributionItemParameter(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow(), // serviceLocator
                "my.parameterid", // id //$NON-NLS-1$
                SELECT_TRACE_TYPE_COMMAND_ID, // commandId
                CommandContributionItem.STYLE_PUSH // style
        );
        param.parameters = params;
        param.icon = icon;
        param.disabledIcon = icon;
        param.hoverIcon = icon;
        param.label = label;
        param.visibleEnabled = true;

        if (subMenu != null) {
            subMenu.add(new CommandContributionItem(param));
        } else {
            list.add(new CommandContributionItem(param));
        }
    }

    private static boolean isSelectedCategory(String categoryId, IConfigurationElement[] config, Set<String> selectedTraceTypes) {
        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TmfTraceType.TYPE_ELEM)) {
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                if (selectedTraceTypes.contains(traceTypeId)) {
                    if (categoryId.equals(ce.getAttribute(TmfTraceType.CATEGORY_ATTR))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSelectedCategory(CustomTxtTraceDefinition[] customTxtTraceDefinitions, Set<String> selectedTraceTypes) {
        for (CustomTxtTraceDefinition def : customTxtTraceDefinitions) {
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + ':' + def.definitionName;
            if (selectedTraceTypes.contains(traceTypeId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSelectedCategory(CustomXmlTraceDefinition[] customXmlTraceDefinitions, Set<String> selectedTraceTypes) {
        for (CustomXmlTraceDefinition def : customXmlTraceDefinitions) {
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + ':' + def.definitionName;
            if (selectedTraceTypes.contains(traceTypeId)) {
                return true;
            }
        }
        return false;
    }
}
