/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.junit.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.RecoveryEvaluator;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.kernel.xmlhost.orders.ScriptFileManager;

/**
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class KernelStateOperatingTest {

  private final Set<Vehicle> vehicles = new HashSet<>();

  private int objectID = 0;

  private KernelState operating;

  private KernelApplicationConfiguration configuration;

  private TCSObjectPool objectPool;

  private RecoveryEvaluator recoveryEvaluator;

  private Router router;

  private Scheduler scheduler;

  private Dispatcher dispatcher;

  private LocalVehicleControllerPool controllerPool;

  private AttachmentManager attachmentManager;

  @Before
  public void setUp() {
    objectID = 0;
    objectPool = mock(TCSObjectPool.class);
    configuration = mock(KernelApplicationConfiguration.class);
    recoveryEvaluator = mock(RecoveryEvaluator.class);
    router = mock(Router.class);
    scheduler = mock(Scheduler.class);
    dispatcher = mock(Dispatcher.class);
    controllerPool = mock(LocalVehicleControllerPool.class);
    attachmentManager = mock(AttachmentManager.class);
    when(objectPool.getObjects(Vehicle.class)).thenReturn(vehicles);
  }

  @After
  public void tearDown() {
    operating = null;
    configuration = null;
    objectPool = null;
  }

  @Test
  public void shouldInitializeExtensionsAndComponents() {
    KernelExtension extension = mock(KernelExtension.class);
    operating = createKernel(Collections.singleton(extension));
    operating.initialize();
    verify(recoveryEvaluator, times(1)).initialize();
    verify(router, times(1)).initialize();
    verify(scheduler, times(1)).initialize();
    verify(dispatcher, times(1)).initialize();
    verify(controllerPool, times(1)).initialize();
    verify(extension, times(1)).initialize();
  }

  @Test
  public void shouldTerminateExtensionsAndComponents() {
    KernelExtension extension = mock(KernelExtension.class);
    operating = createKernel(Collections.singleton(extension));
    operating.initialize();
    operating.terminate();
    verify(recoveryEvaluator, times(1)).terminate();
    verify(router, times(1)).terminate();
    verify(scheduler, times(1)).terminate();
    verify(dispatcher, times(1)).terminate();
    verify(controllerPool, times(1)).terminate();
    verify(extension, times(1)).terminate();
  }

  @Test
  public void initializeKernelWithVehiclesAsUnavailable() {
    Vehicle vehicle = new Vehicle("Vehicle-" + objectID++);
    vehicles.add(vehicle);
    operating = createKernel(new HashSet<>());
    operating.initialize();
    verify(operating, times(1)).setVehicleProcState(vehicle.getReference(),
                                                    Vehicle.ProcState.UNAVAILABLE);
    verify(operating, times(1)).setVehicleState(vehicle.getReference(),
                                                Vehicle.State.UNKNOWN);
    verify(operating, times(1)).setVehicleTransportOrder(vehicle.getReference(),
                                                         null);
    verify(operating, times(1)).setVehicleOrderSequence(vehicle.getReference(),
                                                        null);
  }

  @Test
  public void terminateKernelWithVehiclesAsUnavailable() {
    Vehicle vehicle = new Vehicle("Vehicle-" + objectID++);
    vehicles.add(vehicle);
    operating = createKernel(new HashSet<>());
    operating.initialize();
    operating.terminate();
    verify(operating, times(2)).setVehicleProcState(vehicle.getReference(),
                                                    Vehicle.ProcState.UNAVAILABLE);
    verify(operating, times(2)).setVehicleState(vehicle.getReference(),
                                                Vehicle.State.UNKNOWN);
    verify(operating, times(2)).setVehicleTransportOrder(vehicle.getReference(),
                                                         null);
    verify(operating, times(2)).setVehicleOrderSequence(vehicle.getReference(),
                                                        null);
  }

  /**
   * Creates the kernel to test.
   *
   * @param extensions The kernel extensions
   * @return The kernel to test
   */
  private KernelStateOperating createKernel(@Nonnull Set<KernelExtension> extensions) {
    return spy(new KernelStateOperating(new Object(),
                                        objectPool,
                                        mock(Model.class),
                                        new TransportOrderPool(objectPool),
                                        new NotificationBuffer(),
                                        mock(ModelPersister.class),
                                        configuration,
                                        recoveryEvaluator,
                                        router,
                                        scheduler,
                                        dispatcher,
                                        controllerPool,
                                        mock(ScriptFileManager.class),
                                        mock(OrderCleanerTask.class),
                                        extensions,
                                        attachmentManager));
  }
}
