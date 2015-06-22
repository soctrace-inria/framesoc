package fr.inria.soctrace.lib.storage.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public class DBModelChecker {

	private final static FramesocTable[] checkedTableModel = { FramesocTable.TRACE};
		//	FramesocTable.TRACE_TYPE, FramesocTable.TRACE_PARAM_TYPE,
		//	FramesocTable.TRACE_PARAM, FramesocTable.TOOL };
	
	// Iterate on Framesoc table
	public static final int NAME_COLUMN_INDEX = 2;

	public boolean checkDB(SystemDBObject systemDB) throws SoCTraceException {

		Statement stm = null;
		ResultSet rs;
		
		int cpt = 1;
		for (FramesocTable framesocTable : checkedTableModel) {
			try {
				stm = systemDB.getConnection().createStatement();
				String query = systemDB.getTraceInfoQuery(framesocTable);
				
				// Get the class method corresponding to the current model
				@SuppressWarnings("unchecked")
				Method m = DBModelConstants.TableModelDictionary.get(framesocTable).getMethod("getValueAt", Integer.class);
				
				// Get info
				rs = stm.executeQuery(query);
				while (rs.next()) {
					// Check that each column is similar
					String columnName = rs.getString(NAME_COLUMN_INDEX);
					if (!columnName.equals(((TableModel)m.invoke(null, cpt))
							.getDbColumnName())) {
						stm.close();
						return false;
					}

					cpt++;
				}

				// If size is different return false (missing field)
				if (cpt - 1 != TraceTableModel.values().length) {
					stm.close();
					return false;
				}

				stm.close();
			} catch (SQLException | SoCTraceException | NoSuchMethodException
					| SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new SoCTraceException(e);
			}
		}

		return true;
	}

}
