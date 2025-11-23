package me.whereareiam.intercept.adapter.database.statement;

import me.whereareiam.intercept.adapter.database.dialect.DialectLocator;
import me.whereareiam.intercept.adapter.database.dialect.StatementProvider;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.AttachedExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Handler for {@link DialectUpdate} annotation.
 * <p>
 * This handler delegates to Jdbi's internal {@code SqlUpdateHandler}.
 * SQL resolution is handled by {@link DialectLocator}, which intercepts
 * the SQL location and resolves it from {@link StatementProvider}.
 */
public class UpdateHandler implements ExtensionHandler {
	private final ExtensionHandler delegateHandler;

	public UpdateHandler(Class<?> sqlObjectType, Method method) {
		// Delegate to Jdbi's internal SqlUpdateHandler
		// The SQL will be resolved by DialectLocator before it reaches here
		this.delegateHandler = createDelegateHandler(sqlObjectType, method);
	}

	private ExtensionHandler createDelegateHandler(Class<?> sqlObjectType, Method method) {
		try {
			// Use reflection to access Jdbi's internal SqlUpdateHandler
			Class<?> sqlUpdateHandlerClass = Class.forName("org.jdbi.v3.sqlobject.statement.internal.SqlUpdateHandler");
			Constructor<?> constructor = sqlUpdateHandlerClass.getConstructor(Class.class, Method.class);
			return (ExtensionHandler) constructor.newInstance(sqlObjectType, method);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create delegate SqlUpdateHandler", e);
		}
	}

	@Override
	public AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
		return delegateHandler.attachTo(config, target);
	}
}

