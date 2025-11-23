package me.whereareiam.intercept.adapter.database.statement;

import me.whereareiam.intercept.adapter.database.dialect.DialectLocator;
import me.whereareiam.intercept.adapter.database.dialect.StatementProvider;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.AttachedExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Handler for {@link DialectQuery} annotation.
 * <p>
 * This handler delegates to Jdbi's internal {@code SqlQueryHandler}.
 * SQL resolution is handled by {@link DialectLocator}, which intercepts
 * the SQL location and resolves it from {@link StatementProvider}.
 */
public class QueryHandler implements ExtensionHandler {
	private final ExtensionHandler delegateHandler;

	public QueryHandler(Class<?> sqlObjectType, Method method) {
		// Delegate to Jdbi's internal SqlQueryHandler
		// The SQL will be resolved by DialectLocator before it reaches here
		this.delegateHandler = createDelegateHandler(sqlObjectType, method);
	}

	private ExtensionHandler createDelegateHandler(Class<?> sqlObjectType, Method method) {
		try {
			// Use reflection to access Jdbi's internal SqlQueryHandler
			Class<?> sqlQueryHandlerClass = Class.forName("org.jdbi.v3.sqlobject.statement.internal.SqlQueryHandler");
			Constructor<?> constructor = sqlQueryHandlerClass.getConstructor(Class.class, Method.class);
			return (ExtensionHandler) constructor.newInstance(sqlObjectType, method);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create delegate SqlQueryHandler", e);
		}
	}

	@Override
	public AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
		return delegateHandler.attachTo(config, target);
	}
}

