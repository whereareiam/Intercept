package me.whereareiam.intercept.adapter.database.convention;

import io.ebean.config.TableName;
import io.ebean.config.UnderscoreNamingConvention;

/**
 * Custom naming convention that adds a table prefix to all table names.
 */
public class PrefixedUnderscoreNamingConvention extends UnderscoreNamingConvention {
	private final String tablePrefix;

	public PrefixedUnderscoreNamingConvention(String tablePrefix) {
		this.tablePrefix = tablePrefix != null ? tablePrefix : "";
	}

	@Override
	public TableName getTableName(Class<?> beanClass) {
		TableName tableName = super.getTableName(beanClass);
		String prefixedName = tablePrefix + tableName.getName();
		
		return new TableName(tableName.getCatalog(), tableName.getSchema(), prefixedName);
	}
}

