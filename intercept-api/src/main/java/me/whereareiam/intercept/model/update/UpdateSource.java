package me.whereareiam.intercept.model.update;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import me.whereareiam.intercept.type.ProviderType;

@Data
@SuperBuilder
public class UpdateSource {
	private ProviderType provider;
	private String id;
}