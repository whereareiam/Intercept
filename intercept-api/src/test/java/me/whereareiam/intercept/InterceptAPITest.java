package me.whereareiam.intercept;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("Intercept API")
class InterceptAPITest {
	private final InterceptAPI intercept = mock(InterceptAPI.class);

	@AfterEach
	void unregister() {
		InterceptAPI.unregister();
	}

	@DisplayName("Without a running instance, get throws and find is empty")
	@Test
	void notRunning() {
		assertThrows(IllegalStateException.class, InterceptAPI::get);
		assertEquals(Optional.empty(), InterceptAPI.find());
	}

	@DisplayName("Waiting actions run once Intercept is ready, not when it registers; later ones run immediately")
	@Test
	void whenReadyWaitsForTheReadiness() {
		List<String> ran = new ArrayList<>();
		InterceptAPI.whenReady(instance -> ran.add("before"));

		InterceptAPI.register(intercept);
		InterceptAPI.whenReady(instance -> ran.add("registered"));
		assertEquals(List.of(), ran);
		assertSame(intercept, InterceptAPI.get());

		InterceptAPI.markReady();
		InterceptAPI.whenReady(instance -> ran.add("after"));

		assertEquals(List.of("before", "registered", "after"), ran);
	}

	@DisplayName("A restarted Intercept waits for its readiness again")
	@Test
	void restartWaitsAgain() {
		InterceptAPI.register(intercept);
		InterceptAPI.markReady();
		InterceptAPI.unregister();

		List<String> ran = new ArrayList<>();
		InterceptAPI.register(intercept);
		InterceptAPI.whenReady(instance -> ran.add("restarted"));
		assertEquals(List.of(), ran);

		InterceptAPI.markReady();
		assertEquals(List.of("restarted"), ran);
	}

	@DisplayName("A failing action does not keep the others or the readiness from completing")
	@Test
	void failingActionDoesNotStopOthers() {
		List<String> ran = new ArrayList<>();
		InterceptAPI.whenReady(instance -> {
			throw new IllegalStateException("plugin failed");
		});
		InterceptAPI.whenReady(instance -> ran.add("second"));

		InterceptAPI.register(intercept);
		InterceptAPI.markReady();

		assertEquals(List.of("second"), ran);
		assertSame(intercept, InterceptAPI.get());
	}

	@DisplayName("A second registration is refused until the first instance leaves, and readiness needs one")
	@Test
	void oneInstanceAtATime() {
		assertThrows(IllegalStateException.class, InterceptAPI::markReady);
		InterceptAPI.register(intercept);

		assertThrows(IllegalStateException.class, () -> InterceptAPI.register(mock(InterceptAPI.class)));

		InterceptAPI.unregister();
		assertEquals(Optional.empty(), InterceptAPI.find());
	}
}
