package me.whereareiam.intercept.persistence.database;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import org.jetbrains.annotations.NotNull;

/**
 * The contents of {@code persistence.yml}.
 */
@Getter
@Setter
@ToString
public class Persistence {
	@Comment("SQLITE or H2 for a server alone; MYSQL or POSTGRES for servers that share messages and languages.")
	private @NotNull DatabaseType type = DatabaseType.SQLITE;
	@Comment("The file of SQLITE or H2, relative to Intercept's folder.")
	private @NotNull String file = "intercept.db";
	private @NotNull String host = "localhost";
	private int port = 5432;
	private @NotNull String database = "intercept";
	private @NotNull String username = "intercept";
	@ToString.Exclude
	private @NotNull String password = "";
	private @NotNull Pool pool = new Pool();

	/**
	 * The connection pool.
	 */
	@Getter
	@Setter
	@ToString
	public static class Pool {
		private int maximumSize = 4;
		private int minimumIdle = 1;
		@Comment("Milliseconds to wait for a connection.")
		private long connectionTimeout = 10_000;
	}
}
