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
package fr.inria.soctrace.framesoc.core.tools.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * Arguments manager able to parse a command line with the
 * following expressions:
 * <ol>
 * <li> -opt=value
 * <li> -flag
 * <li> token
 * </ol>
 * <p>
 * E.g. <pre>-log="logfile" -v -t "val1" "val2"</pre> 
 * 
 * <p>
 * Note that any quotation mark (") is removed.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ArgumentsManager implements IArgumentsManager {

	Map<String, String> options = new HashMap<String, String>();
	Set<String> flags = new HashSet<String>();
	List<String> tokens = new LinkedList<String>();

	@Override
	public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++)
            switch (args[i].charAt(0)) {
		        case '-':
		            if (args[i].length() < 2)
		                throw new IllegalArgumentException("Not a valid argument: " + args[i]);
		            if (args[i].charAt(1) == '-')
		                throw new IllegalArgumentException("Not a valid argument: " + args[i]);
		            if (args[i].contains("=")) {
		            	// option
		            	String v[] = args[i].split("=");
		            	if (v.length<2)
			                throw new IllegalArgumentException("Not a valid argument: " + args[i]);
		            	options.put(v[0].substring(1), v[1]);
		            } else {
		            	// flag
		            	flags.add(args[i].substring(1));
		            }
		            break;
		        default:
		        	tokens.add(args[i]);
		            break;
            }
	}

	@Override
	public void processArgs() {
		// the base class does nothing
	}

	@Override
	public void printArgs() {
		System.out.println("*** Arguments ***");
		System.out.println("[Options]");
		Iterator<Entry<String,String>> iterator = options.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> pair = iterator.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
		System.out.println("[Flags]");
		for (String s: flags) {
			System.out.println(s);
		}
		System.out.println("[Tokens]");
		for (String s: tokens) {
			System.out.println(s);
		}
		System.out.println("***************** ");
	}

	@Override
	public void clean() {
		options.clear();
		flags.clear();
		tokens.clear();
	}

	/**
	 * Get the options: -name=value
	 * @return the options map
	 */
	public Map<String, String> getOptions() {
		return options;
	}

	/**
	 * Get the flags: -flag
	 * @return the flags set
	 */
	public Set<String> getFlags() {
		return flags;
	}

	/**
	 * Get the tokens: token
	 * @return the tokens list
	 */
	public List<String> getTokens() {
		return tokens;
	}

	/**
	 * Static method to tokenize a single string containing a
	 * command line compliant with the arguments manager.
	 * 
	 * @param args string containing the command line
	 * @return a vector of tokens as accepted by the argument manager
	 */
	public static String[] tokenize(String args) {
		List<String> list = new ArrayList<String>();
		
		// pattern: 
		// -\\S+=\".+?\" matches stuff like: -opt="a b c"
		// [^\"]\\S* matches something non starting by "
		// \".+?\" matches text between quotes
		Matcher m = Pattern.compile("(-\\S+=\".+?\"|[^\"]\\S*|\".+?\")\\s*").matcher(args);
		
		while (m.find())
			list.add(m.group(1).replace("\"", ""));
		return list.toArray(new String[0]);
	}

	
}
