package me.whereareiam.intercept.dependency;

import me.whereareiam.intercept.model.dependency.LibraryDescriptor;

public interface DependencyLoader {
	void addRepository(String repository);

	void addLibrary(LibraryDescriptor library);

	default void loadLibraries() {

	}
}
