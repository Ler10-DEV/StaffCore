package org.staffcore.discord;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Batches and queues Discord log messages to prevent Discord API rate limiting.
 */
public class RateLimitGuard {
    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<LogTask> queue = new LinkedBlockingQueue<>(500);
    private final Logger logger;
    private ScheduledFuture<?> scheduledTask;

    public record LogTask(TextChannel channel, String message) {}

    public RateLimitGuard(ScheduledExecutorService scheduler, Logger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public void start(long batchIntervalMs) {
        this.scheduledTask = scheduler.scheduleWithFixedDelay(this::processBatch,
                batchIntervalMs, batchIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        processBatch(); // Flush remainder
    }

    public void queueMessage(TextChannel channel, String message) {
        if (channel == null || message == null || message.trim().isEmpty()) return;
        queue.offer(new LogTask(channel, message));
    }

    private void processBatch() {
        if (queue.isEmpty()) return;
        LogTask task;
        while ((task = queue.poll()) != null) {
            try {
                task.channel().sendMessage(task.message()).queue(
                        null,
                        err -> logger.warning("Failed sending Discord batched message: " + err.getMessage())
                );
            } catch (Exception e) {
                logger.warning("Error processing Discord log task: " + e.getMessage());
            }
        }
    }
}
