package me.whereareiam.intercept.adapter.database.statement;

import me.whereareiam.intercept.adapter.database.dialect.StatementProvider;
import org.jdbi.v3.core.extension.annotation.UseExtensionHandler;
import org.jdbi.v3.sqlobject.SqlObjectFactory;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for database-specific SQL queries (SELECT statements).
 * <p>
 * This annotation works similarly to {@link SqlQuery},
 * but allows specifying a {@link StatementProvider} class that provides database-specific SQL.
 * <p>
 * The SQL will be resolved at runtime based on the current database type.
 * <p>
 * Example:
 * <pre>{@code
 * @DialectQuery(provider = MessageFileAdapter.FindById.class)
 * Optional<MessageFileEntity> findById(@Bind("id") long id);
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@UseExtensionHandler(id = SqlObjectFactory.EXTENSION_ID, value = QueryHandler.class)
public @interface DialectQuery {
	/**
	 * The {@link StatementProvider} class that provides the SQL query for each database type.
	 *
	 * @return the StatementProvider class
	 */
	Class<? extends StatementProvider> provider();
}

