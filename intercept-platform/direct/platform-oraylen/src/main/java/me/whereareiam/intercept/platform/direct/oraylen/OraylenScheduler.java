package me.whereareiam.intercept.platform.direct.oraylen;

import com.google.inject.Singleton;
import me.whereareiam.intercept.Scheduler;
import me.whereareiam.intercept.model.scheduler.DelayedRunnableTask;
import me.whereareiam.intercept.model.scheduler.PeriodicalRunnableTask;
import me.whereareiam.intercept.model.scheduler.RunnableTask;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public final class OraylenScheduler implements Scheduler {
	private static final String DEFAULT_MODULE = "intercept";
	private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

	private final Map<String, Map<Integer, Task>> tasks = new ConcurrentHashMap<>();
	private final net.minestom.server.timer.Scheduler scheduler = MinecraftServer.getSchedulerManager();

	private void ensureId(RunnableTask task) {
		int existing = task.getId();
		if (existing <= 0) {
			int generated = ID_GENERATOR.getAndIncrement();
			task.setId(generated);
		}
	}

	private String moduleKey(RunnableTask task) {
		String module = task.getModule();
		return module == null || module.isBlank() ? DEFAULT_MODULE : module;
	}

	private Runnable wrapRunnable(Runnable runnable, boolean async) {
		if (!async) return runnable;
		return () -> CompletableFuture.runAsync(runnable);
	}

	private TaskSchedule toSchedule(long millis) {
		if (millis <= 0) return TaskSchedule.immediate();
		return TaskSchedule.millis(millis);
	}

	private void storeTask(RunnableTask runnableTask, Task task) {
		tasks.computeIfAbsent(moduleKey(runnableTask), _ -> new ConcurrentHashMap<>())
				.put(runnableTask.getId(), task);
	}

	@Override
	public void schedule(RunnableTask runnableTask) {
		schedule(runnableTask, true);
	}

	@Override
	public void schedule(DelayedRunnableTask runnableTask) {
		schedule(runnableTask, true);
	}

	@Override
	public void schedule(PeriodicalRunnableTask runnableTask) {
		schedule(runnableTask, true);
	}

	@Override
	public void schedule(RunnableTask runnableTask, boolean async) {
		ensureId(runnableTask);

		Runnable runnable = wrapRunnable(runnableTask.getRunnable(), async);
		Task task = scheduler.buildTask(runnable)
				.executionType(ExecutionType.TICK_START)
				.schedule();

		storeTask(runnableTask, task);
	}

	@Override
	public void schedule(DelayedRunnableTask runnableTask, boolean async) {
		ensureId(runnableTask);

		Runnable runnable = wrapRunnable(runnableTask.getRunnable(), async);
		Task task = scheduler.buildTask(runnable)
				.delay(toSchedule(runnableTask.getDelay()))
				.executionType(ExecutionType.TICK_START)
				.schedule();

		storeTask(runnableTask, task);
	}

	@Override
	public void schedule(PeriodicalRunnableTask runnableTask, boolean async) {
		ensureId(runnableTask);

		Runnable runnable = wrapRunnable(runnableTask.getRunnable(), async);
		Task task = scheduler.buildTask(runnable)
				.delay(toSchedule(runnableTask.getDelay()))
				.repeat(toSchedule(runnableTask.getPeriod()))
				.executionType(ExecutionType.TICK_START)
				.schedule();

		storeTask(runnableTask, task);
	}

	@Override
	public void cancel(RunnableTask runnableTask) {
		Map<Integer, Task> moduleTasks = tasks.get(moduleKey(runnableTask));
		if (moduleTasks == null) return;

		Task task = moduleTasks.remove(runnableTask.getId());
		if (task != null) task.cancel();

		if (moduleTasks.isEmpty())
			tasks.remove(moduleKey(runnableTask));
	}

	@Override
	public void cancel(DelayedRunnableTask runnableTask) {
		cancel((RunnableTask) runnableTask);
	}

	@Override
	public void cancelByModule(String module) {
		String key = module == null || module.isBlank() ? DEFAULT_MODULE : module;
		Map<Integer, Task> moduleTasks = tasks.remove(key);
		if (moduleTasks == null) return;

		moduleTasks.values().forEach(Task::cancel);
	}
}
