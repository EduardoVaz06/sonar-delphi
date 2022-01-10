package org.sonar.plugins.delphi.nunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.sonar.api.measures.CoreMetrics.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiNUnitSensorTest {

  private static final String PROJECT_DIR = "/org/sonar/plugins/delphi/nunit";

  private MapSettings settings;
  private SensorContextTester context;
  private DelphiNUnitSensor sensor;

  @BeforeEach
  void setup() {
    context = SensorContextTester.create(DelphiUtils.getResource(PROJECT_DIR));
    settings = new MapSettings();
    sensor = new DelphiNUnitSensor(settings.asConfig());
  }

  void assertAllMeasuresEmpty() {
    assertThat(context.measures(context.project().key())).isEmpty();
  }

  @Test
  void testToString() {
    assertThat(sensor).hasToString("DelphiNUnitSensor");
  }

  @Test
  void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(DelphiLanguage.KEY);
    verify(mockDescriptor).name("Delphi NUnit Sensor");
  }

  @Test
  void testExecute() {
    assertAllMeasuresEmpty();
    settings.setProperty(DelphiPlugin.NUNIT_REPORT_PATHS_PROPERTY, "./reports/normal");
    sensor.execute(context);

    assertThat(context.measure(context.project().key(), TESTS).value()).isEqualTo(8);
    assertThat(context.measure(context.project().key(), SKIPPED_TESTS).value()).isEqualTo(1);
    assertThat(context.measure(context.project().key(), TEST_FAILURES).value()).isEqualTo(3);
    assertThat(context.measure(context.project().key(), TEST_EXECUTION_TIME).value())
        .isEqualTo(1561L);
  }

  @Test
  void testExecuteWithNoReportPath() {
    sensor.execute(context);
    assertAllMeasuresEmpty();
  }

  @Test
  void testExecuteWithInvalidReportPath() {
    settings.setProperty(DelphiPlugin.NUNIT_REPORT_PATHS_PROPERTY, UUID.randomUUID().toString());
    sensor.execute(context);
    assertAllMeasuresEmpty();
  }
}
