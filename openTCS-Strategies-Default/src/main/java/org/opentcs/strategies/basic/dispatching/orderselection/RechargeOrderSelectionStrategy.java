/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class RechargeOrderSelectionStrategy
    implements VehicleOrderSelectionStrategy,
               Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ParkingOrderSelectionStrategy.class);
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;
  /**
   * The strategy used for finding suitable recharge locations.
   */
  private final org.opentcs.components.kernel.RechargePositionSupplier rechargePosSupplier;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public RechargeOrderSelectionStrategy(
      LocalKernel kernel,
      Router router,
      ProcessabilityChecker processabilityChecker,
      org.opentcs.components.kernel.RechargePositionSupplier rechargePosSupplier,
      DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.kernel = requireNonNull(kernel, "kernel");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.rechargePosSupplier = requireNonNull(rechargePosSupplier, "rechargePosSupplier");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }
    rechargePosSupplier.initialize();

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      return;
    }
    rechargePosSupplier.terminate();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Nullable
  @Override
  public VehicleOrderSelection selectOrder(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle,"vehicle");
    if (!configuration.rechargeIdleVehicles()) {
      return null;
    }
    if (!vehicle.isEnergyLevelDegraded()) {
      return null;
    }
    if (vehicle.hasState(Vehicle.State.CHARGING) && vehicle.getEnergyLevel() < 100) {
      LOG.debug("{}: Charging and energy < 100% - leaving it alone.", vehicle.getName());
      return new VehicleOrderSelection(null, vehicle, null);
    }
    LOG.debug("{}: Looking for recharge location...", vehicle.getName());
    List<DriveOrder.Destination> rechargeDests = rechargePosSupplier.findRechargeSequence(vehicle);
    if (rechargeDests.isEmpty()) {
      LOG.warn("{}: Did not find a suitable recharge sequence for vehicle", vehicle.getName());
      return null;
    }
    List<DestinationCreationTO> chargeDests = new LinkedList<>();
    for (DriveOrder.Destination dest : rechargeDests) {
      chargeDests.add(
          new DestinationCreationTO(dest.getDestination().getName(), dest.getOperation())
              .setProperties(dest.getProperties())
      );
    }
    // Create a transport order for recharging and verify its processability.
    // The recharge order may be withdrawn unless its energy level is critical.
    TransportOrder rechargeOrder = kernel.createTransportOrder(
        new TransportOrderCreationTO("Recharge-" + UUID.randomUUID(), chargeDests)
            .setIntendedVehicleName(vehicle.getName())
            .setDispensable(!vehicle.isEnergyLevelCritical())
    );

    Point vehiclePosition = kernel.getTCSObject(Point.class, vehicle.getCurrentPosition());
    Optional<List<DriveOrder>> driveOrders
        = router.getRoute(vehicle, vehiclePosition, rechargeOrder);
    if (processabilityChecker.checkProcessability(vehicle, rechargeOrder)
        && driveOrders.isPresent()) {
      return new VehicleOrderSelection(rechargeOrder, vehicle, driveOrders.get());
    }
    else {
      // Mark the order as failed, since the vehicle does not want to execute it.
      kernel.setTransportOrderState(rechargeOrder.getReference(), TransportOrder.State.FAILED);
      return null;
    }
  }

}
