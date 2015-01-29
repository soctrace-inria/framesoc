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
package fr.inria.soctrace.lib.model;

import java.sql.Timestamp;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing an analysis result.
 * It contains the information of the ANALYSIS_RESULT entity of the
 * data model and the analysis result corresponding data.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnalysisResult implements IModelElement {

	private long id;
	private Tool tool;
	private String type;
	private Timestamp date;
	private String description;
	private AnalysisResultData data;
	
	/**
	 * Constructor to be used when a tool create a result data,
	 * and want to save the analysis result in the DB.
	 * 
	 * @param data the analysis result data
	 * @param tool the tool
	 */
	public AnalysisResult(AnalysisResultData data, Tool tool) {
		this.data = data;
		this.data.setAnalysisResult(this);
		this.type = data.getType().toString();
		this.tool = tool;
		this.description = "";
		// unique id given at save time by the visitor
	}

	/**
	 * Constructor to be used when we retrieve an analysis result from the DB.
	 * 
	 * @param id the result unique id
	 */
	public AnalysisResult(long id) {
		super();
		this.id = id;
		this.tool = null;
		this.type = null;
		this.description = null;
		this.data = null;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the tool
	 */
	public Tool getTool() {
		return tool;
	}
	
	/**
	 * @param tool the tool to set
	 */
	public void setTool(Tool tool) {
		this.tool = tool;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the date
	 */
	public Timestamp getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Timestamp date) {
		this.date = date;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the data
	 */
	public AnalysisResultData getData() {
		return data;
	}
	
	/**
	 * @param data the data to set
	 */
	public void setData(AnalysisResultData data) {
		this.data = data;
		this.data.setAnalysisResult(this);
	}
	
	@Override
	public String toString() {
		return "AnalysisResult[(id:" + id + "),(tool.name:'" + tool.getName() + "')," +
				"(type:'" + type + "'),(date:'" + date + "')(description:'"+description +"')]";
	}
		
	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}
	
	/**
	 * Debug method
	 * @throws SoCTraceException 
	 */
	public void print() throws SoCTraceException {
		System.out.println(toString());
		if (data!=null)
			data.print();
		else 
			System.out.println("Result Data not set");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((tool == null) ? 0 : tool.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisResult other = (AnalysisResult) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (tool == null) {
			if (other.tool != null)
				return false;
		} else if (!tool.equals(other.tool))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
