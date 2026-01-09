package me.whereareiam.intercept.model.dependency;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Getter
@Builder
public class LibraryDescriptor {
	private String groupId;
	private String artifactId;
	private String version;
	@Builder.Default
	private boolean resolveTransitive = false;
	@Singular("relocation")
	private List<RelocationRule> relocations;
	private String classifier;
}
