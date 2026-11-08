package me.whereareiam.intercept.adapter.replication;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.replication.ReplicationService;
import me.whereareiam.intercept.replication.transport.Replication;

/**
 * Binds replication through Redis in place of the default that keeps everything on one instance.
 */
public final class ReplicationConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(Replication.class).toProvider(ReplicationProvider.class);

		OptionalBinder.newOptionalBinder(binder(), ReplicationService.class)
				.setBinding()
				.to(TransportReplicationService.class);
		Multibinder.newSetBinder(binder(), Lifecycle.class).addBinding().to(TransportReplicationService.class);
	}
}
