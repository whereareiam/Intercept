package me.whereareiam.intercept.platform.direct.common.translation.source;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class DirectTranslationSourceEntry {
	private final String path;
	private final String formatId;
	private final String fileType;
	private final boolean multiLocale;
	private final boolean optional;
	private final DirectDefaultsProvider defaultsProvider;
}
