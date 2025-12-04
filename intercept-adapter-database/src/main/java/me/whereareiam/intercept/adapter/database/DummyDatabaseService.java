package me.whereareiam.intercept.adapter.database;

import com.google.inject.Singleton;
import me.whereareiam.intercept.database.DatabaseService;

/**
 * Dummy implementation of DatabaseService used when persistence is disabled.
 * Always reports database as not initialized.
 */
@Singleton
public class DummyDatabaseService implements DatabaseService {
	@Override
	public boolean isInitialized() {
		return false;
	}
}