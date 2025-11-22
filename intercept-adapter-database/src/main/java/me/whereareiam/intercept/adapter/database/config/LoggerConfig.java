package me.whereareiam.intercept.adapter.database.config;

import me.whereareiam.intercept.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.SQLException;

/**
 * Configures Jdbi SQL logging.
 */
public final class LoggerConfig {
	/**
	 * Configures SQL logger for Jdbi.
	 *
	 * @param jdbi the shared Jdbi instance
	 */
	public static void configure(Jdbi jdbi) {
		if (jdbi == null) return;

		jdbi.getConfig(SqlStatements.class).setSqlLogger(new SqlLogger() {
			@Override
			public void logBeforeExecution(StatementContext context) {
				Logger.debug("Executing SQL: %s", context.getRenderedSql());
			}

			@Override
			public void logAfterExecution(StatementContext context) {
				Logger.debug("SQL completed: %s", context.getRenderedSql());
			}

			@Override
			public void logException(StatementContext context, SQLException ex) {
				Logger.warn("SQL error [%s]: %s", context.getRenderedSql(), ex.getMessage());
			}
		});
	}
}

