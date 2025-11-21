package me.whereareiam.intercept.adapter.database.connection.type;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.jdbc.db.PostgresDatabaseType;

import java.util.List;

/**
 * Custom Postgres database type that guards CREATE SEQUENCE statements with IF NOT EXISTS.
 * <p>
 * The default ORMLite implementation issues unconditional CREATE SEQUENCE statements during table creation.
 * When {@code TableUtils.createTableIfNotExists} is invoked on an already-initialized schema, PostgreSQL raises
 * an error because the sequence already exists. This implementation adds {@code IF NOT EXISTS} for PostgreSQL 9.5+
 * drivers (the first version that supports it) to make schema initialization idempotent.
 */
public final class SafePostgresDatabaseType extends PostgresDatabaseType {
	@Override
	protected void configureGeneratedIdSequence(
			StringBuilder sb,
			FieldType fieldType,
			List<String> statementsBefore,
			List<String> additionalArgs,
			List<String> queriesAfter
	) {
		String sequenceName = fieldType.getGeneratedIdSequence();

		StringBuilder seqSb = new StringBuilder(64);
		seqSb.append("CREATE SEQUENCE ");
		if (supportsIfNotExists()) seqSb.append("IF NOT EXISTS ");

		appendEscapedEntityName(seqSb, sequenceName);
		statementsBefore.add(seqSb.toString());

		sb.append("DEFAULT NEXTVAL(");
		sb.append('\'').append('\"').append(sequenceName).append('\"').append('\'');
		sb.append(") ");

		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	private boolean supportsIfNotExists() {
		if (driver == null) return true;

		try {
			int major = driver.getMajorVersion();
			int minor = driver.getMinorVersion();

			return major > 9 || (major == 9 && minor >= 5);
		} catch (Exception ignored) {
			return true;
		}
	}
}

