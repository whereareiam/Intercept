package me.whereareiam.intercept.common;

import me.whereareiam.attache.LibraryManager;
import me.whereareiam.attache.model.LibraryRequest;
import me.whereareiam.attache.type.VerbosityMode;
import me.whereareiam.intercept.dependency.DependencyLoader;
import me.whereareiam.intercept.model.dependency.LibraryDescriptor;
import me.whereareiam.intercept.model.dependency.RelocationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InterceptDependencyLoader implements DependencyLoader {
	private final LibraryManager libraryManager;
	private final boolean applyRelocations;
	private final List<LibraryDescriptor> libraries = new ArrayList<>();

	public InterceptDependencyLoader(LibraryManager libraryManager, boolean applyRelocations) {
		this.libraryManager = Objects.requireNonNull(libraryManager, "libraryManager");
		this.applyRelocations = applyRelocations;

		libraryManager.setVerbosityMode(VerbosityMode.SUMMARY);
		if (!libraryManager.hasLibraryAdapter(LibraryDescriptor.class)) {
			libraryManager.registerLibraryAdapter(LibraryDescriptor.class, this::toRequest);
		}
	}

	@Override
	public void addRepository(String repository) {
		libraryManager.addRepository(repository);
	}

	@Override
	public void addLibrary(LibraryDescriptor library) {
		libraries.add(library);
	}

	@Override
	public void loadLibraries() {
		if (libraries.isEmpty())
			return;

		libraryManager.loadLibraries(libraries);
		libraries.clear();
	}

	private LibraryRequest toRequest(LibraryDescriptor spec) {
		LibraryRequest.LibraryRequestBuilder builder = LibraryRequest.builder()
				.groupId(spec.getGroupId())
				.artifactId(spec.getArtifactId())
				.version(spec.getVersion())
				.resolveTransitiveDependencies(spec.isResolveTransitive());

		if (spec.getClassifier() != null)
			builder.classifier(spec.getClassifier());

		if (applyRelocations && spec.getRelocations() != null)
			for (RelocationRule relocation : spec.getRelocations())
				builder.relocation(me.whereareiam.attache.model.RelocationRule.builder()
						.pattern(relocation.getFrom())
						.relocatedPattern(relocation.getTo())
						.build());

		return builder.build();
	}
}
