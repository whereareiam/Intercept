package me.whereareiam.intercept.common.updater;

import com.google.inject.Provider;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.Scheduler;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.scheduler.PeriodicalRunnableTask;
import me.whereareiam.intercept.registry.Registry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UpdateSchedulerReloadTest {

	private Provider<Settings> settingsProvider;
	private Scheduler scheduler;
	private Registry<Reloadable> reloadableRegistry;
	private UpdateScheduler updateScheduler;

	@BeforeAll
	static void initLogger() {
		// Initialize Logger with a mock to prevent NPEs
		LoggingHelper mockLogger = mock(LoggingHelper.class);
		Logger.init(mockLogger);
	}

	@BeforeEach
	void setUp() {
		settingsProvider = mock(Provider.class);
		scheduler = mock(Scheduler.class);
		UpdateProviderRegistry providerRegistry = mock(UpdateProviderRegistry.class);
		reloadableRegistry = mock(Registry.class);

		updateScheduler = new UpdateScheduler(
				settingsProvider,
				scheduler,
				providerRegistry,
				reloadableRegistry
		);
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(updateScheduler);
	}

	@Test
	void shouldCancelExistingTasksOnReload() {
		// Setup settings with updates enabled
		createMockSettings(true, 24);

		// Reload
		updateScheduler.reload();

		// Should cancel existing tasks
		verify(scheduler).cancelByModule("main");
	}

	@Test
	void shouldRestartSchedulerOnReloadWithUpdatesEnabled() {
		// Setup settings with updates enabled
		createMockSettings(true, 12);

		// Reload
		updateScheduler.reload();

		// Should cancel old tasks and schedule new ones
		verify(scheduler).cancelByModule("main");
		verify(scheduler).schedule(any(PeriodicalRunnableTask.class), eq(true));
	}

	@Test
	void shouldNotScheduleOnReloadWhenUpdatesDisabled() {
		// Setup settings with updates disabled
		createMockSettings(false, 24);

		// Reload
		updateScheduler.reload();

		// Should cancel old tasks but not schedule new ones
		verify(scheduler).cancelByModule("main");
		verify(scheduler, never()).schedule(any(PeriodicalRunnableTask.class), anyBoolean());
	}

	@Test
	void shouldNotScheduleOnReloadWhenIntervalIsZero() {
		// Setup settings with zero interval
		createMockSettings(true, 0);

		// Reload
		updateScheduler.reload();

		// Should cancel old tasks but not schedule new ones
		verify(scheduler).cancelByModule("main");
		verify(scheduler, never()).schedule(any(PeriodicalRunnableTask.class), anyBoolean());
	}

	@Test
	void shouldNotScheduleOnReloadWhenIntervalIsNegative() {
		// Setup settings with negative interval
		createMockSettings(true, -1);

		// Reload
		updateScheduler.reload();

		// Should cancel old tasks but not schedule new ones
		verify(scheduler).cancelByModule("main");
		verify(scheduler, never()).schedule(any(PeriodicalRunnableTask.class), anyBoolean());
	}

	@Test
	void shouldHandleMultipleReloads() {
		createMockSettings(true, 24);

		// Multiple reloads
		updateScheduler.reload();
		updateScheduler.reload();
		updateScheduler.reload();

		// Should cancel 3 times
		verify(scheduler, times(3)).cancelByModule("main");
		// Should schedule 3 times
		verify(scheduler, times(3)).schedule(any(PeriodicalRunnableTask.class), eq(true));
	}

	@Test
	void shouldRespectNewIntervalOnReload() {
		// First reload with 24 hour interval
		createMockSettings(true, 24);
		updateScheduler.reload();

		// Clear invocations
		clearInvocations(scheduler);

		// Second reload with 12 hour interval
		createMockSettings(true, 12);
		updateScheduler.reload();

		// Should cancel and reschedule with new interval
		verify(scheduler).cancelByModule("main");
		verify(scheduler).schedule(any(PeriodicalRunnableTask.class), eq(true));
	}

	@Test
	void shouldHandleReloadFromEnabledToDisabled() {
		// Start with enabled
		createMockSettings(true, 24);
		updateScheduler.reload();

		clearInvocations(scheduler);

		// Reload with disabled
		createMockSettings(false, 24);
		updateScheduler.reload();

		// Should cancel but not reschedule
		verify(scheduler).cancelByModule("main");
		verify(scheduler, never()).schedule(any(PeriodicalRunnableTask.class), anyBoolean());
	}

	@Test
	void shouldHandleReloadFromDisabledToEnabled() {
		// Start with disabled
		createMockSettings(false, 24);
		updateScheduler.reload();

		clearInvocations(scheduler);

		// Reload with enabled
		createMockSettings(true, 24);
		updateScheduler.reload();

		// Should cancel and schedule
		verify(scheduler).cancelByModule("main");
		verify(scheduler).schedule(any(PeriodicalRunnableTask.class), eq(true));
	}

	private void createMockSettings(boolean checkForUpdates, int interval) {
		Settings settings = mock(Settings.class);
		Settings.Updater updater = mock(Settings.Updater.class);

		when(settings.getUpdater()).thenReturn(updater);
		when(updater.isCheckForUpdates()).thenReturn(checkForUpdates);
		when(updater.getInterval()).thenReturn(interval);
		when(updater.isWarnAboutLocalBuilds()).thenReturn(false);
		when(updater.isWarnAboutDevBuilds()).thenReturn(false);
		when(updater.isWarnAboutUpdates()).thenReturn(true);

		// Ensure settingsProvider returns this settings object
		when(settingsProvider.get()).thenReturn(settings);
	}
}

