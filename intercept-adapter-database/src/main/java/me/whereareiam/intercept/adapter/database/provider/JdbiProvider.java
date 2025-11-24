package me.whereareiam.intercept.adapter.database.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.database.DefaultDatabaseService;
import org.jdbi.v3.core.Jdbi;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class JdbiProvider implements Provider<Jdbi> {
	private final DefaultDatabaseService databaseService;

	@Override
	public Jdbi get() {
		if (!databaseService.isInitialized())
			throw new IllegalStateException("Database is not initialized yet");

		return databaseService.getJdbi();
	}
}

