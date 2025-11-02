package me.whereareiam.intercept.common;

import com.google.inject.Injector;
import lombok.Getter;
import lombok.Setter;

public class CommonInjector {
	@Getter
	@Setter
	private static Injector injector;
}
