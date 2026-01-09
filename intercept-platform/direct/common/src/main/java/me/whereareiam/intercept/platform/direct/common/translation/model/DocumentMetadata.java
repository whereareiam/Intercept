package me.whereareiam.intercept.platform.direct.common.translation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Metadata about a parsed translation document.
 */
@Getter
@AllArgsConstructor
public class DocumentMetadata {
    private final Path sourcePath;
    private final String formatType;
    private final Instant loadedAt;

    public static DocumentMetadata of(Path sourcePath, String formatType) {
        return new DocumentMetadata(sourcePath, formatType, Instant.now());
    }
}
