package com.sprint.mission.monew.batch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class LogBackupMetrics {

  private static final String BACKUP = "monew.log.backup";
  private static final String BYTES = "monew.log.backup.bytes";
  private static final String DURATION = "monew.log.backup.duration";
  private static final String RESULT = "result";
  private static final String RESULT_UPLOADED = "uploaded";
  private static final String RESULT_SKIPPED = "skipped";
  private static final String RESULT_FAILED = "failed";

  private final MeterRegistry registry;
  private final Counter bytesCounter;
  private final Timer durationTimer;

  public LogBackupMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.bytesCounter = Counter.builder(BYTES)
        .baseUnit("bytes")
        .description("S3에 업로드된 압축 로그 총 바이트")
        .register(registry);
    this.durationTimer = Timer.builder(DURATION)
        .description("로그 백업 1회 소요 시간")
        .register(registry);
  }

  public void countUploaded() {
    backup(RESULT_UPLOADED).increment();
  }

  public void countSkipped() {
    backup(RESULT_SKIPPED).increment();
  }

  public void countFailed() {
    backup(RESULT_FAILED).increment();
  }

  public void recordBytes(long bytes) {
    bytesCounter.increment(bytes);
  }

  public void recordDuration(Duration duration) {
    durationTimer.record(duration);
  }

  private Counter backup(String result) {
    return registry.counter(BACKUP, RESULT, result);
  }
}
