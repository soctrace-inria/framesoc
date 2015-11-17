package fr.inria.soctrace.framesoc.headless;

import java.io.Console;
import java.util.Scanner;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.DBMS;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

public class HeadlessSetup {

	// Configuration options
	private static final int SQLITE_DB = 1;
	private static final int MYSQL_DB = 2;
	
	private static final int SQLITE_PATH_TO_EXISTING_DB_OPTION = 1;
	private static final int SQLITE_CREATE_NEW_SYSTEM_DB_OPTION = 2;
	private static final int SQLITE_CREATE_NEW_SYSTEM_DB_AT_OPTION = 3;
	
	Scanner scanner;
	
	public HeadlessSetup(Scanner scanner) {
		this.scanner = scanner;
	}

	/**
	 * Setup Framesoc database.
	 */
	public void launchSetup() {
		System.out
				.println("Choose the type of Database management system you want:\n"
						+ "1. SQLite\n"
						+ "2. MySQL");
		int choice = scanner.nextInt();
		// Remove the new line from the scanner stream
		scanner.nextLine();
		
		switch (choice) {
		case SQLITE_DB:
			Configuration.getInstance().set(SoCTraceProperty.soctrace_dbms,
					DBMS.SQLITE.toString());
			setupSQLite();
			break;

		case MYSQL_DB:
			Configuration.getInstance().set(SoCTraceProperty.soctrace_dbms,
					DBMS.MYSQL.toString());
			setupMySQL();
			break;

		default:
			System.out.println("Invalid option selected: " + choice);
			launchSetup();
		}
	}

	/**
	 * Setup database for SQLite
	 */
	private void setupSQLite(){
		String currentDbPath = Configuration.getInstance().get(
				SoCTraceProperty.sqlite_db_directory);
		String path;

		System.out.println("Getting parameters for SQLite.");
		System.out.println("SQLite current path for the database is " + currentDbPath);
		System.out.println("Your choices are:\n"
				+ " 1. Provide a path to an existing database.\n"
				+ " 2. Create a new system database at the current path.\n"
				+ " 3. Provide a new path and create a system database.");

		int choice = scanner.nextInt();

		switch (choice) {
		case SQLITE_PATH_TO_EXISTING_DB_OPTION:
			System.out
					.println("Please provide a path to the directory of an existing database:");
			path = scanner.next();
			setDatabasePath(path);
			break;

		case SQLITE_CREATE_NEW_SYSTEM_DB_OPTION:
			createDatabase();
			break;

		case SQLITE_CREATE_NEW_SYSTEM_DB_AT_OPTION:
			System.out
					.println("Please provide a path to an existing directory:");
			path = scanner.next();
			setDatabasePath(path);
			createDatabase();
			break;

		default:
			System.out.println("Invalid option selected: " + choice);
			setupSQLite();
		}
	}

	private void setDatabasePath(String path) {
		Configuration.getInstance().set(SoCTraceProperty.sqlite_db_directory,
				path);
		Configuration.getInstance().saveOnFile();
	}

	private void createDatabase() {
		try {
			FramesocManager.getInstance().createSystemDB();
		} catch (SoCTraceException e) {
			System.out
					.println("Error: Failed to create a new system database at "
							+ Configuration.getInstance().get(
									SoCTraceProperty.sqlite_db_directory));
			e.printStackTrace();
		}
	}
	
	/**
	 * Setup database for MySQL
	 */
	private void setupMySQL() {
		System.out.println("Getting parameters for mySQL. Current values (in parentheses) will be used if no input is provided.");
		
		// User name
		String username = Configuration.getInstance().get(
				SoCTraceProperty.mysql_db_user);
		System.out.println("Please provide a user name (" + username + "):");
		String providedUser = scanner.nextLine();
		if (!providedUser.isEmpty())
			username = providedUser;
		Configuration.getInstance().set(SoCTraceProperty.mysql_db_user,
				username);

		// Password
		Console console = System.console();
		String pass = Configuration.getInstance().get(
				SoCTraceProperty.mysql_db_password);
		String providedPass = new String(console.readPassword("Please provide a password:"));
		if (!providedPass.isEmpty())
			pass = providedPass;
		Configuration.getInstance().set(SoCTraceProperty.mysql_db_password,
				pass);

		// URL
		String url = Configuration.getInstance().get(
				SoCTraceProperty.mysql_base_db_jdbc_url);
		System.out.println("Please provide the URL (" + url + "):");
		String providedUrl = scanner.nextLine();
		if (!providedUrl.isEmpty())
			url = providedUrl;
		Configuration.getInstance().set(
				SoCTraceProperty.mysql_base_db_jdbc_url, url);

		// Save
		Configuration.getInstance().saveOnFile();
	}
}
