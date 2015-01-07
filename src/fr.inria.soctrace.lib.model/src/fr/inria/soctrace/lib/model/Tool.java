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
package fr.inria.soctrace.lib.model;

import java.io.Serializable;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing the TOOL entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class Tool implements IModelElement, Serializable {
	
	/**
	 * Generated UID for serialization 
	 */
	private static final long serialVersionUID = 2230347002444798252L;
	
	private final int id;
	private String name;
	private String type;
	private String command;
	private boolean plugin;
	private String doc;
	private String extensionId; // meaningful only for plugin tools

	/**
	 * Constructor 
	 * @param id the tool unique id
	 */
	public Tool(int id) {
		this.id = id;
		this.name = "";
		this.type = "";
		this.command = "";
		this.plugin = false;
		this.doc = "";
		this.extensionId="";
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Tool [id=" + id + ", name=" + name + ", type=" + type + ", command=" + command
				+ ", plugin=" + plugin + ", doc=" + doc + ", extensionId=" + extensionId + "]";
	}

	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/**
	 * @return the plugin flag
	 */
	public boolean isPlugin() {
		return plugin;
	}

	/**
	 * @param plugin the plugin flag to set
	 */
	public void setPlugin(boolean plugin) {
		this.plugin = plugin;
	}

	/**
	 * @return the launching documentation
	 */
	public String getDoc() {
		return doc;
	}

	/**
	 * @param doc the launching documentation to set
	 */
	public void setDoc(String doc) {
		this.doc = doc;
	}

	/**
	 * 
	 * @return the extension id
	 */
	public String getExtensionId() {
		return extensionId;
	}

	/**
	 * 
	 * @param extensionId the extension id to set
	 */
	public void setExtensionId(String extensionId) {
		this.extensionId = extensionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
		result = prime * result + ((extensionId == null) ? 0 : extensionId.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (plugin ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tool other = (Tool) obj;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (doc == null) {
			if (other.doc != null)
				return false;
		} else if (!doc.equals(other.doc))
			return false;
		if (extensionId == null) {
			if (other.extensionId != null)
				return false;
		} else if (!extensionId.equals(other.extensionId))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (plugin != other.plugin)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
