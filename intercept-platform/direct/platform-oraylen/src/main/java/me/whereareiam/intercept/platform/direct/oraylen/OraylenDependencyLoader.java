package me.whereareiam.intercept.platform.direct.oraylen;

import me.whereareiam.intercept.common.Dependencies;
import me.whereareiam.intercept.dependency.DependencyLoader;
import me.whereareiam.intercept.model.dependency.LibraryDescriptor;
import me.whereareiam.intercept.model.dependency.RelocationRule;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OraylenDependencyLoader implements DependencyLoader {
	private final boolean applyRelocations;
	private final List<URI> repositories = new ArrayList<>();
	private final List<net.oraylen.api.model.library.LibraryDescriptor> libraries = new ArrayList<>();

	public OraylenDependencyLoader(boolean applyRelocations) {
		this.applyRelocations = applyRelocations;
		Dependencies.applyTo(this);
	}

	@Override
	public void addRepository(String repository) {
		repositories.add(URI.create(repository));
	}

	@Override
	public void addLibrary(LibraryDescriptor library) {
		libraries.add(toDescriptor(library));
	}

	public List<URI> repositories() {
		return Collections.unmodifiableList(repositories);
	}

	public List<net.oraylen.api.model.library.LibraryDescriptor> libraries() {
		return Collections.unmodifiableList(libraries);
	}

	private net.oraylen.api.model.library.LibraryDescriptor toDescriptor(LibraryDescriptor spec) {
		net.oraylen.api.model.library.LibraryDescriptor.LibraryDescriptorBuilder builder = net.oraylen.api.model.library.LibraryDescriptor.builder()
				.groupId(spec.getGroupId())
				.artifactId(spec.getArtifactId())
				.version(spec.getVersion())
				.resolveTransitive(spec.isResolveTransitive());

		if (spec.getClassifier() != null)
			builder.classifier(spec.getClassifier());

		if (applyRelocations && spec.getRelocations() != null) {
			for (RelocationRule relocation : spec.getRelocations()) {
				net.oraylen.api.model.library.RelocationRule rule = new net.oraylen.api.model.library.RelocationRule();
				rule.setFrom(relocation.getFrom());
				rule.setTo(relocation.getTo());
				builder.relocation(rule);
			}
		}

		return builder.build();
	}
}
