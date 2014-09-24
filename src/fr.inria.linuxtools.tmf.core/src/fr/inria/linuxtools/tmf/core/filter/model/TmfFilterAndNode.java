/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.tmf.core.filter.model;

import fr.inria.linuxtools.tmf.core.event.ITmfEvent;


/**
 * Filter node for the 'and' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterAndNode extends TmfFilterTreeNode {

    public static final String NODE_NAME = "AND"; //$NON-NLS-1$
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$

    private boolean fNot = false;

    /**
     * @param parent the parent node
     */
    public TmfFilterAndNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * @return the NOT state
     */
    public boolean isNot() {
        return fNot;
    }

    /**
     * @param not the NOT state
     */
    public void setNot(boolean not) {
        this.fNot = not;
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        for (ITmfFilterTreeNode node : getChildren()) {
            if (! node.matches(event)) {
                return false ^ fNot;
            }
        }
        return true ^ fNot;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (fNot) {
            buf.append("not "); //$NON-NLS-1$
        }
        if (getParent() != null && !(getParent() instanceof TmfFilterRootNode) && !(getParent() instanceof TmfFilterNode)) {
            buf.append("( "); //$NON-NLS-1$
        }
        for (int i = 0; i < getChildrenCount(); i++) {
            ITmfFilterTreeNode node = getChildren()[i];
            buf.append(node.toString());
            if (i < getChildrenCount() - 1) {
                buf.append(" and "); //$NON-NLS-1$
            }
        }
        if (getParent() != null && !(getParent() instanceof TmfFilterRootNode) && !(getParent() instanceof TmfFilterNode)) {
            buf.append(" )"); //$NON-NLS-1$
        }
        return buf.toString();
    }

}
