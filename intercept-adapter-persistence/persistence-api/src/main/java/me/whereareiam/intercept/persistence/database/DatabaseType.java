package me.whereareiam.intercept.persistence.database;

/**
 * The databases Intercept stores in.
 */
public enum DatabaseType {
	/**
	 * A file beside the server; fine for one server, not for a network.
	 */
	SQLITE,
	/**
	 * An H2 file beside the server.
	 */
	H2,
	/**
	 * A MySQL or MariaDB server.
	 */
	MYSQL,
	/**
	 * A PostgreSQL server.
	 */
	POSTGRES
}
