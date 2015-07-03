package fr.inria.soctrace.lib.storage.updater;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.ToolTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public class ToolModelRebuilder extends DBModelRebuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(ToolModelRebuilder.class);

	public ToolModelRebuilder() {
		table = FramesocTable.TOOL;
		getTableQuery = SQLConstants.PREPARED_STATEMENT_TOOL_INSERT;
	}
	
	@Override
	public void copyValues(PreparedStatement statement, ResultSet rs)
			throws SoCTraceException {
		for (ToolTableModel toolTableModel : ToolTableModel.values()) {
			// If it is one of the difference
			if (oldModelDiff.containsKey(toolTableModel.toString())) {
				// If missing parameter
				if (oldModelDiff.get(toolTableModel.toString()) == MISSING_PARAMETER_VALUE) {
					// Put default value
					addToStatement(statement,
							toolTableModel.getDefaultValue(),
							toolTableModel.getPosition(),
							toolTableModel.getType());
				} else {
					// Put the value at the correct place
					addToStatement(statement, rs,
							toolTableModel.getPosition(),
							oldModelDiff.get(toolTableModel.toString()),
							toolTableModel.getType());
				}
				// Remove parameter from table
				oldModelDiff.remove(toolTableModel.toString());
			} else {
				// Just copy it
				addToStatement(statement, rs, toolTableModel.getPosition(),
						toolTableModel.getPosition(),
						toolTableModel.getType());
			}
		}

		if (!oldModelDiff.keySet().isEmpty()) {
			logger.info("Tools: the following parameters were not imported in the new model:");
			for (String columnName : oldModelDiff.keySet())
				logger.info("  *" + columnName);
		}
	}
	
	@Override
	public String getValueAt(int pos) {
		return ToolTableModel.getValueAt(pos).getDbColumnName();
	}
	
	@Override
	public int getColumnNumber() {
		return ToolTableModel.numberOfColumns();
	}

}
