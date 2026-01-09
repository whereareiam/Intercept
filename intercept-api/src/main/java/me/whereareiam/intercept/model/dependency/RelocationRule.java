package me.whereareiam.intercept.model.dependency;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelocationRule {
	private String from;
	private String to;
}
