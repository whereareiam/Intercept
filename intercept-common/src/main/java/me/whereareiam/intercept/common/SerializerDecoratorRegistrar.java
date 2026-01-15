package me.whereareiam.intercept.common;

import me.whereareiam.intercept.util.Serializer;
import me.whereareiam.keystone.Serializers;
import me.whereareiam.keystone.decorator.PrefixDecorator;
import me.whereareiam.keystone.serializer.MessageDecorator;
import me.whereareiam.keystone.serializer.SerializerEngine;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class SerializerDecoratorRegistrar {
	public static void registerDefaults(
			@NotNull SerializerEngine engine,
			@NotNull Supplier<String> prefixSupplier,
			@NotNull MessageDecorator... decorators
	) {
		Serializers.registerScopedDecorator(engine, Serializer.SCOPE, new PrefixDecorator(prefixSupplier));

		for (MessageDecorator decorator : decorators) {
			Serializers.registerDecorator(engine, decorator);
		}
	}
}
