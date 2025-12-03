package me.whereareiam.intercept.common.provider;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.integration.Integration;
import me.whereareiam.intercept.registry.base.Registry;

import java.util.HashSet;
import java.util.Set;

@Singleton
public class IntegrationProvider implements Provider<Set<Integration>>, Registry<Integration> {
    private final Set<Integration> integrations = new HashSet<>();

    @Override
    public void register(Integration integration) {
        integrations.add(integration);
    }

    @Override
    public Set<Integration> get() {
        return integrations;
    }
}