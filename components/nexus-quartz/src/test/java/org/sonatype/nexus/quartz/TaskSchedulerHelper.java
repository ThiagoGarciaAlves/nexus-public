/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.quartz;

import java.io.File;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.sonatype.goodies.testsupport.TestUtil;
import org.sonatype.nexus.common.app.ApplicationDirectories;
import org.sonatype.nexus.common.app.BaseUrlManager;
import org.sonatype.nexus.common.event.EventBus;
import org.sonatype.nexus.common.node.LocalNodeAccess;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.scheduling.spi.SchedulerSPI;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
import org.quartz.spi.JobFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * IT tool: this beast brings up real SISU container and complete Quartz environment.
 */
public class TaskSchedulerHelper
{
  private final TestUtil util = new TestUtil(TaskSchedulerHelper.class);

  @Inject
  private Injector injector;

  @Inject
  private MutableBeanLocator locator;

  @Inject
  protected TaskScheduler taskScheduler;

  @Inject
  private SchedulerSPI scheduler;

  @Inject
  private EventBus eventBus;

  private ApplicationDirectories applicationDirectories;

  private BaseUrlManager baseUrlManager;

  private LocalNodeAccess localNodeAccess;

  private final DatabaseInstance databaseInstance;

  public TaskSchedulerHelper(final DatabaseInstance databaseInstance) {
    this.databaseInstance = checkNotNull(databaseInstance);
  }

  public void init(@Nullable final Integer poolSize, @Nullable final JobFactory factory) throws Exception {
    applicationDirectories = mock(ApplicationDirectories.class);
    baseUrlManager = mock(BaseUrlManager.class);
    localNodeAccess = mock(LocalNodeAccess.class);

    Module module = binder -> {
      Properties properties = new Properties();
      properties.put("basedir", util.getBaseDir());
      if (poolSize != null) {
        properties.put("nexus.quartz.poolSize", poolSize);
      }
      binder.bind(ParameterKeys.PROPERTIES)
          .toInstance(properties);

      File workDir = util.createTempDir(util.getTargetDir(), "workdir");
      when(applicationDirectories.getWorkDirectory(anyString())).thenReturn(workDir);
      binder.bind(ApplicationDirectories.class)
          .toInstance(applicationDirectories);

      binder.bind(BaseUrlManager.class)
          .toInstance(baseUrlManager);

      binder.bind(DatabaseInstance.class)
          .annotatedWith(Names.named("config"))
          .toInstance(databaseInstance);

      when(localNodeAccess.getId()).thenReturn("test-12345");
      binder.bind(LocalNodeAccess.class)
          .toInstance(localNodeAccess);
      if (factory != null) {
        binder.bind(JobFactory.class).toInstance(factory);
      }
    };

    this.injector = Guice.createInjector(new WireModule(
        module,
        new SpaceModule(new URLClassSpace(TaskSchedulerHelper.class.getClassLoader()), BeanScanning.INDEX)
    ));
    injector.injectMembers(this);
  }

  public void start() throws Exception {
    scheduler.start();
    scheduler.resume();
  }

  public void stop() throws Exception {
    scheduler.pause();
    scheduler.stop();
    locator.clear();
  }

  public TaskScheduler getTaskScheduler() {
    return taskScheduler;
  }

  public SchedulerSPI getScheduler() {
    return scheduler;
  }

  public EventBus getEventBus() {
    return eventBus;
  }
}
