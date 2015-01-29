/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.lib.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Group;
import fr.inria.soctrace.lib.model.IGroupable;
import fr.inria.soctrace.lib.model.OrderedGroup;
import fr.inria.soctrace.lib.model.UnorderedGroup;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for Group analysis results.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class AnalysisResultGroupDataQuery extends AnalysisResultDataQuery {

	/**
	 * Utility class to store Target Entity ID and Sequence number.
	 */
	private class Entity {

		public long mapping; // leaf mapping id
		public long id; // entity id
		public int seq; // sequence number

		public Entity(long mapping, long id, int seq) {
			this.mapping = mapping;
			this.id = id;
			this.seq = seq;
		}
	}

	/* --------------------------- */

	/* Three hierarchy management */

	/**
	 * Map: GID - Group
	 */
	Map<Long, Group> gidGroupMap = new HashMap<>();

	/**
	 * Map: Parent GID - List of son GID
	 */
	Map<Long, List<Long>> pidGidMap = new HashMap<>();

	/* Leaf entity management */

	/**
	 * Map: GID - List of Entity son id with sequence number
	 */
	Map<Integer, List<Entity>> gidEntityListMap = new HashMap<Integer, List<Entity>>();

	/**
	 * Map: Entity Class - Entity ID - Entity object
	 */
	Map<Class<? extends IGroupable>, Map<Long, IGroupable>> eidEntityMap = new HashMap<>();

	/* --------------------------- */

	/**
	 * The constructor.
	 * 
	 * @param traceDB
	 *            trace DB containing the result
	 */
	public AnalysisResultGroupDataQuery(TraceDBObject traceDB) {
		super(traceDB);
	}

	@Override
	public AnalysisResultData getAnalysisResultData(long analysisResultId) throws SoCTraceException {

		try {

			loadGroupMaps(analysisResultId);
			loadEntityMaps(analysisResultId);

			Long rootId = pidGidMap.get(-1).iterator().next();
			Group root = gidGroupMap.get(rootId);
			getSons(root);
			buildMapping();
			clear();

			return new AnalysisResultGroupData(root);

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Load maps used to rebuild the tree structure.
	 * 
	 * @param analysisResultId
	 *            analysis result ID
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private void loadGroupMaps(long analysisResultId) throws SQLException, SoCTraceException {

		String query = "SELECT * FROM " + FramesocTable.ENTITY_GROUP
				+ " WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
		debug(query);
		Statement stm = traceDB.getConnection().createStatement();
		ResultSet rs = stm.executeQuery(query);
		while (rs.next()) {
			Group g = buildGroup(rs);
			gidGroupMap.put(g.getId(), g);
			if (!pidGidMap.containsKey(g.getParentId()))
				pidGidMap.put(g.getParentId(), new LinkedList<Long>());
			pidGidMap.get(g.getParentId()).add(g.getId());
		}
		stm.close();
	}

	/**
	 * Load all the maps used for Entity management.
	 * 
	 * @param analysisResultId
	 *            analysis result ID
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private void loadEntityMaps(long analysisResultId) throws SQLException, SoCTraceException {

		// Entity Class - Value List String for Entity IDs
		Map<Class<? extends IGroupable>, ValueListString> classVlsMap = new HashMap<Class<? extends IGroupable>, ValueListString>();

		String query = "SELECT * FROM " + FramesocTable.GROUP_MAPPING
				+ " WHERE ANALYSIS_RESULT_ID = " + analysisResultId;

		debug(query);
		Statement stm = traceDB.getConnection().createStatement();
		ResultSet rs = stm.executeQuery(query);

		while (rs.next()) {
			int mid = rs.getInt(2); // mapping id
			int gid = rs.getInt(3);
			int eid = rs.getInt(4);
			int seq = rs.getInt(5);
			if (!gidEntityListMap.containsKey(gid))
				gidEntityListMap.put(gid, new LinkedList<Entity>());
			gidEntityListMap.get(gid).add(new Entity(mid, eid, seq));
			Group g = gidGroupMap.get(gid);
			Class<? extends IGroupable> c = g.getTargetClass();
			if (!classVlsMap.containsKey(c))
				classVlsMap.put(c, new ValueListString());
			classVlsMap.get(c).addValue(String.valueOf(eid));
		}

		if (classVlsMap.containsKey(Event.class)) {
			EventQuery eventQuery = new EventQuery(traceDB);
			ValueListString vls = classVlsMap.get(Event.class);
			eventQuery.setElementWhere(new SimpleCondition("ID", ComparisonOperation.IN, vls
					.getValueString()));
			List<Event> elist = eventQuery.getList();
			eidEntityMap.put(Event.class, new HashMap<Long, IGroupable>());
			Map<Long, IGroupable> tmp = eidEntityMap.get(Event.class);
			for (Event e : elist) {
				tmp.put(e.getId(), e);
			}
		}

		if (classVlsMap.containsKey(EventType.class)) {
			EventTypeQuery eventTypeQuery = new EventTypeQuery(traceDB);
			ValueListString vls = classVlsMap.get(EventType.class);
			eventTypeQuery.setElementWhere(new SimpleCondition("ID", ComparisonOperation.IN, vls
					.getValueString()));
			List<EventType> tlist = eventTypeQuery.getList();
			eidEntityMap.put(EventType.class, new HashMap<Long, IGroupable>());
			Map<Long, IGroupable> tmp = eidEntityMap.get(EventType.class);
			for (EventType t : tlist) {
				tmp.put(t.getId(), t);
			}
		}

		if (classVlsMap.containsKey(EventParamType.class)) {
			EventParamTypeQuery eventParamTypeQuery = new EventParamTypeQuery(traceDB);
			ValueListString vls = classVlsMap.get(EventParamType.class);
			eventParamTypeQuery.setElementWhere(new SimpleCondition("ID", ComparisonOperation.IN,
					vls.getValueString()));
			List<EventParamType> tlist = eventParamTypeQuery.getList();
			eidEntityMap.put(EventParamType.class, new HashMap<Long, IGroupable>());
			Map<Long, IGroupable> tmp = eidEntityMap.get(EventParamType.class);
			for (EventParamType t : tlist) {
				tmp.put(t.getId(), t);
			}
		}

	}

	/**
	 * Build the tree hierarchy that follows a given node. This is a recursive function.
	 * 
	 * @param g
	 *            base of the three
	 * @throws SoCTraceException
	 */
	private void getSons(Group g) throws SoCTraceException {

		// base case: group without son groups
		if (!pidGidMap.containsKey(g.getId()))
			return;

		// general case: group with son groups
		boolean ordered = (g instanceof OrderedGroup);
		List<Long> sons = pidGidMap.get(g.getId());
		for (Long sonId : sons) {
			Group son = gidGroupMap.get(sonId);
			if (ordered)
				((OrderedGroup) g).addSon(son, son.getSequenceNumber());
			else
				((UnorderedGroup) g).addSon(son);
			getSons(son);
		}
	}

	/**
	 * Put the leaf entities in the respective parent groups.
	 * 
	 * @throws SoCTraceException
	 */
	private void buildMapping() throws SoCTraceException {

		Set<Integer> gidSet = gidEntityListMap.keySet();
		for (Integer gid : gidSet) {
			Group g = gidGroupMap.get(gid);
			boolean ordered = g.isOrdered();
			Map<Long, IGroupable> tmp = eidEntityMap.get(g.getTargetClass());
			List<Entity> elist = gidEntityListMap.get(gid);
			for (Entity entity : elist) {
				if (ordered)
					((OrderedGroup) g).addSon(tmp.get(entity.id), entity.seq, entity.mapping);
				else
					((UnorderedGroup) g).addSon(tmp.get(entity.id), entity.mapping);
			}
		}
	}

	/**
	 * Build a group object, given the ResultSet row.
	 * 
	 * @param rs
	 *            table row
	 * @return the Group object
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private Group buildGroup(ResultSet rs) throws SQLException, SoCTraceException {
		Group group = null;
		int id = rs.getInt(2);
		String targetEntity = rs.getString(5);
		boolean ordered = rs.getBoolean(7);

		if (ordered)
			group = new OrderedGroup(id, getTargetClass(targetEntity));
		else
			group = new UnorderedGroup(id, getTargetClass(targetEntity));

		group.setParentId(rs.getInt(3));
		group.setName(rs.getString(4));
		group.setGroupingOperator(rs.getString(6));
		group.setSequenceNumber(rs.getInt(8));

		return group;
	}

	/**
	 * Get Target Entity class from its string label.
	 * 
	 * @param targetEntity
	 *            string containing the class label
	 * @return the class object
	 */
	private Class<? extends IGroupable> getTargetClass(String targetEntity) {
		if (targetEntity.equals(ModelEntity.EVENT.name()))
			return Event.class;
		else if (targetEntity.equals(ModelEntity.EVENT_TYPE.name()))
			return EventType.class;
		else if (targetEntity.equals(ModelEntity.EVENT_PARAM_TYPE.name()))
			return EventParamType.class;
		return null;
	}

	/**
	 * Clear maps
	 */
	private void clear() {
		gidGroupMap.clear();
		pidGidMap.clear();
		gidEntityListMap.clear();
		eidEntityMap.clear();
	}

}
