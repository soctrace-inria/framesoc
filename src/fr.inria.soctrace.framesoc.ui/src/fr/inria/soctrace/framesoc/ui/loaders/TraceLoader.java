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
package fr.inria.soctrace.framesoc.ui.loaders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.model.FolderNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.model.TraceNode;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;

/**
 * Load the data of the model for the Traces view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceLoader {

	/**
	 * Trace category
	 */
	enum TraceCategory {

		/** Raw trace */
		RAW_TRACE,
		/** Trace that are a result of processing other traces */
		PROCESSED_TRACE;

		public String getName() {
			return (this == RAW_TRACE) ? "Raw Traces" : "Processed Traces";
		}
	}

	/**
	 * Change performed on a trace.
	 */
	public enum TraceChange {
		/** Trace being added */
		ADD,
		/** Trace being removed */
		REMOVE,
		/** Trace metadata being updated */
		UPDATE;
	}

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(TraceLoader.class);

	/**
	 * List of all system traces
	 */
	private List<Trace> traces;

	/**
	 * Map of category folder nodes (root nodes)
	 */
	private Map<TraceCategory, FolderNode> roots;

	/**
	 * The constructor
	 */
	public TraceLoader() {
		roots = new HashMap<>();
		for (TraceCategory cat : TraceCategory.values()) {
			roots.put(cat, new FolderNode(cat.getName()));
		}
	}

	/**
	 * Get an array containing the root folder nodes.
	 * 
	 * @return an array containing the root folder nodes.
	 */
	public FolderNode[] getRoots() {
		return roots.values().toArray(new FolderNode[roots.values().size()]);
	}

	/**
	 * Load traces from System DB. Note that this method loads new trace objects. Any existing root
	 * content is lost.
	 * 
	 * @return an array containing the root folder nodes.
	 */
	public FolderNode[] loadFromDB() {

		ITraceSearch traceSearch = null;
		try {

			if (!FramesocManager.getInstance().isSystemDBExisting())
				return getRoots();

			/* Get traces from DB */
			traceSearch = new TraceSearch().initialize();
			traces = traceSearch.getTraces();
			traceSearch.uninitialize();

			/* Prepare the tree */
			buildTree(buildTraceMap(traces));

		} catch (SoCTraceException e) {
			logger.error(e.getMessage());
			removeAll();
		} finally {
			TraceSearch.finalUninitialize(traceSearch);
		}
		return getRoots();
	}

	/**
	 * Synchronize current model with DB. Note that an old model input must be already present. This
	 * method keeps the existing trace objects (possibly updating them) and tree nodes, thus fixing
	 * any selection service issues with other views.
	 * 
	 * @return the model input (an array containing the root folder nodes)
	 * @throws SoCTraceException
	 */
	public FolderNode[] synchWithDB() throws SoCTraceException {

		ITraceSearch traceSearch = null;
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting())
				return getRoots();

			/* Get traces from DB */
			traceSearch = new TraceSearch().initialize();
			traces = traceSearch.getTraces();
			traceSearch.uninitialize();

			// new trace map
			Map<Integer, Trace> newTraces = new HashMap<>();
			for (Trace t : traces) {
				newTraces.put(t.getId(), t);
			}

			// linearize the old tree
			List<Trace> traces = linearizeTree();

			Map<TraceChange, List<Trace>> traceChangeMap = new HashMap<TraceChange, List<Trace>>();
			traceChangeMap.put(TraceChange.ADD, new LinkedList<Trace>());
			traceChangeMap.put(TraceChange.REMOVE, new LinkedList<Trace>());
			traceChangeMap.put(TraceChange.UPDATE, new LinkedList<Trace>());

			// synch old tree trace objects with new ones
			Iterator<Trace> oldTracesIterator = traces.iterator();
			while (oldTracesIterator.hasNext()) {
				Trace t = oldTracesIterator.next();
				if (newTraces.containsKey(t.getId())) {
					// update or not changed
					Trace newTrace = newTraces.get(t.getId());
					if (!newTrace.equals(t)) {
						t.synchWith(newTrace);
						traceChangeMap.get(TraceChange.UPDATE).add(t);
					}
					// remove from new traces
					newTraces.remove(t.getId());
				} else {
					// deleted: remove from input
					traceChangeMap.get(TraceChange.REMOVE).add(t);
					oldTracesIterator.remove();
				}
			}

			// cat new traces
			for (Trace nt : newTraces.values()) {
				traces.add(nt);
				traceChangeMap.get(TraceChange.ADD).add(nt);
			}

			// update the tree
			updateTree(buildTraceMap(traces));

			// notify the bus of changes
			FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED,
					traceChangeMap);

		} catch (SoCTraceException e) {
			logger.error(e.getMessage());
			removeAll();
			throw e;
		} finally {
			TraceSearch.finalUninitialize(traceSearch);
		}
		return getRoots();
	}

	/**
	 * Synchronize the trace nodes with the trace objects contained, in order to get nodes with
	 * updated labels. The System DB is not used.
	 * 
	 * @return the model input (an array containing the root folder nodes)
	 */
	public FolderNode[] synchWithModel() {

		// linearize the old tree
		List<Trace> traces = linearizeTree();

		// update the tree
		updateTree(buildTraceMap(traces));

		return getRoots();

	}

	/**
	 * Get the traces. You have to call the methods that actually load traces from DB before calling
	 * this method.
	 * 
	 * @return the loaded traces
	 */
	public List<Trace> getTraces() {
		return traces;
	}

	/**
	 * Get a trace node from a given trace.
	 * 
	 * @param t
	 *            trace
	 * @return the corresponding trace node, or null if not found
	 */
	public TraceNode getTraceNode(Trace t) {
		for (ITreeNode node : roots.values()) {
			TraceNode n = getTraceNode(node, t);
			if (n != null)
				return n;
		}
		return null;
	}

	/**
	 * Remove all the elements from the root folder nodes.
	 */
	private void removeAll() {
		for (FolderNode n : roots.values()) {
			n.removeAll();
		}
	}

	/**
	 * Builds the following map:
	 * 
	 * <pre>
	 * Category <-> { TraceTypeName <-> {TraceID <-> Trace} }
	 * </pre>
	 * 
	 * @param traces
	 *            list of traces
	 * @return the trace map
	 */
	private Map<TraceCategory, Map<String, Map<Integer, Trace>>> buildTraceMap(List<Trace> traces) {
		Map<TraceCategory, Map<String, Map<Integer, Trace>>> traceMap = new HashMap<TraceCategory, Map<String, Map<Integer, Trace>>>();
		traceMap.put(TraceCategory.RAW_TRACE, new HashMap<String, Map<Integer, Trace>>());
		traceMap.put(TraceCategory.PROCESSED_TRACE, new HashMap<String, Map<Integer, Trace>>());
		Map<String, Map<Integer, Trace>> currentMap;
		for (Trace trace : traces) {
			if (trace.isProcessed())
				currentMap = traceMap.get(TraceCategory.PROCESSED_TRACE);
			else
				currentMap = traceMap.get(TraceCategory.RAW_TRACE);
			if (!currentMap.containsKey(trace.getType().getName())) {
				currentMap.put(trace.getType().getName(), new HashMap<Integer, Trace>());
			}
			currentMap.get(trace.getType().getName()).put(trace.getId(), trace);
		}
		return traceMap;
	}

	private void buildTree(Map<TraceCategory, Map<String, Map<Integer, Trace>>> map) {
		// clean the tree
		removeAll();

		// category
		Iterator<Entry<TraceCategory, Map<String, Map<Integer, Trace>>>> iterator = map.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<TraceCategory, Map<String, Map<Integer, Trace>>> pair = iterator.next();
			FolderNode currentCategory = roots.get(pair.getKey());
			// trace type
			Iterator<Entry<String, Map<Integer, Trace>>> it = pair.getValue().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Map<Integer, Trace>> typePair = it.next();
				FolderNode typeNode = new FolderNode(typePair.getKey());
				// traces
				for (Trace trace : typePair.getValue().values()) {
					typeNode.addChild(new TraceNode(trace.getAlias(), trace));
				}
				currentCategory.addChild(typeNode);
			}
		}
	}

	private void updateTree(Map<TraceCategory, Map<String, Map<Integer, Trace>>> map) {
		for (TraceCategory cat : TraceCategory.values()) {
			updateCategory(roots.get(cat), map.get(cat));
		}
	}

	private void updateCategory(FolderNode categoryNode, Map<String, Map<Integer, Trace>> map) {
		// empty category
		if (!categoryNode.hasChildren()) {
			Iterator<Entry<String, Map<Integer, Trace>>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Map<Integer, Trace>> typePair = it.next();
				FolderNode typeNode = new FolderNode(typePair.getKey());
				for (Trace trace : typePair.getValue().values()) {
					typeNode.addChild(new TraceNode(trace.getAlias(), trace));
				}
				categoryNode.addChild(typeNode);
			}
			return;
		}
		// non empty category
		Iterator<ITreeNode> typeIterator = categoryNode.getChildren().iterator();
		while (typeIterator.hasNext()) {
			FolderNode typeNode = (FolderNode) typeIterator.next();
			if (!map.containsKey(typeNode.getName()))
				typeIterator.remove();
			else {
				updateType(typeNode, map.get(typeNode.getName()));
				// remove type from map
				map.remove(typeNode.getName());
			}
		}
		// new types for this category
		Iterator<Entry<String, Map<Integer, Trace>>> newTypeIterator = map.entrySet().iterator();
		while (newTypeIterator.hasNext()) {
			Entry<String, Map<Integer, Trace>> typePair = newTypeIterator.next();
			FolderNode typeNode = new FolderNode(typePair.getKey());
			for (Trace trace : typePair.getValue().values()) {
				typeNode.addChild(new TraceNode(trace.getAlias(), trace));
			}
			categoryNode.addChild(typeNode);
		}
	}

	private void updateType(FolderNode typeNode, Map<Integer, Trace> map) {
		// empty type is not possible, so manage non empty case
		Iterator<ITreeNode> traceIterator = typeNode.getChildren().iterator();
		while (traceIterator.hasNext()) {
			TraceNode traceNode = (TraceNode) traceIterator.next();
			Trace t = (traceNode).getTrace();
			if (!map.containsKey(t.getId())) {
				// deleted
				traceIterator.remove();
			} else {
				traceNode.setName(t.getAlias());
				map.remove(t.getId());
			}
		}
		// new traces for this type
		for (Trace nt : map.values()) {
			typeNode.addChild(new TraceNode(nt.getAlias(), nt));
		}
	}

	private List<Trace> linearizeTree() {
		List<Trace> traces = new LinkedList<Trace>();
		if (roots.isEmpty())
			return traces;
		for (ITreeNode categoryNode : roots.values()) {
			if (!categoryNode.hasChildren())
				continue;
			for (ITreeNode typeNode : categoryNode.getChildren()) {
				if (!typeNode.hasChildren())
					continue;
				for (ITreeNode traceNode : typeNode.getChildren())
					traces.add(((TraceNode) traceNode).getTrace());
			}
		}
		return traces;
	}

	/**
	 * Recursively looks for a trace node for the given trace.
	 * 
	 * @param n
	 *            node
	 * @param t
	 *            trace to look for
	 * @return the trace node, or null if not found
	 */
	private TraceNode getTraceNode(ITreeNode n, Trace t) {
		if (!n.hasChildren()) {
			// leaf
			if (n instanceof TraceNode) {
				// trace node
				System.out.println(n.getName());
				TraceNode tnode = (TraceNode) n;
				if (tnode.getTrace().equals(t))
					return tnode;
			}
			return null;
		}
		for (ITreeNode son : n.getChildren()) {
			TraceNode ret = getTraceNode(son, t);
			if (ret != null)
				return ret;
		}
		return null;
	}

}
