/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.Scheduler.Client;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles regular resource allocations.
 */
class AllocatorTask
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AllocatorTask.class);
  /**
   * The kernel.
   */
  private final LocalKernel kernel;
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool;
  /**
   * Takes care of (sub)modules.
   */
  private final Scheduler.Module allocationAdvisor;
  /**
   * Commands to be processed.
   */
  private final BlockingQueue<AllocatorCommand> commands = new PriorityBlockingQueue<>();
  /**
   * Allocations deferred because they couldn't be granted, yet.
   */
  private final Queue<AllocatorCommand.Allocate> deferredAllocations = new LinkedList<>();
  /**
   * This tasks termination flag.
   */
  private boolean terminated;

  /**
   * Creates a new instance.
   */
  public AllocatorTask(@Nonnull LocalKernel kernel,
                       @Nonnull ReservationPool reservationPool,
                       @Nonnull Scheduler.Module allocationAdvisor) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.allocationAdvisor = requireNonNull(allocationAdvisor, "allocationAdvisor");
  }

  @Override
  public void run() {
    while (!terminated) {
      try {
        consume(commands.take());
      }
      catch (InterruptedException exc) {
        LOG.warn("Unexpectedly interrupted, ignored.", exc);
      }
    }
  }

  private void consume(AllocatorCommand command) {
    if (command instanceof AllocatorCommand.PoisonPill) {
      commands.clear();
      terminated = true;
    }
    else if (command instanceof AllocatorCommand.Allocate) {
      processAllocate((AllocatorCommand.Allocate) command);
    }
    else if (command instanceof AllocatorCommand.RetryAllocates) {
      retryWaitingAllocations();
    }
    else if (command instanceof AllocatorCommand.CheckAllocationsPrepared) {
      checkAllocationsPrepared((AllocatorCommand.CheckAllocationsPrepared) command);
    }
    else if (command instanceof AllocatorCommand.AllocationsReleased) {
      allocationsReleased((AllocatorCommand.AllocationsReleased) command);
    }
    else {
      LOG.warn("Unhandled AllocatorCommand implementation {}, ignored.", command.getClass());
    }
  }

  public void enqueue(AllocatorCommand command) {
    commands.offer(command);
  }

  public void terminate() {
    enqueue(new AllocatorCommand.PoisonPill());
  }

  private void processAllocate(AllocatorCommand.Allocate command) {
    if (!tryAllocate(command)) {
      LOG.debug("{}: Resources unavailable, deferring allocation...", command.getClient().getId());
      deferredAllocations.add(command);
      return;
    }

    checkAllocationsPrepared(command.getClient(), command.getResources());
  }

  private void checkAllocationsPrepared(AllocatorCommand.CheckAllocationsPrepared command) {
    checkAllocationsPrepared(command.getClient(), command.getResources());
  }

  private void checkAllocationsPrepared(Client client, Set<TCSResource<?>> resources) {
    if (!allocationAdvisor.hasPreparedAllocation(client, resources)) {
      LOG.debug("{}: Preparation of resources not yet done.",
                client.getId());
      // XXX remember the resources a client is waiting for preparation done?
      return;
    }

    LOG.debug("Preparation of resources '{}' successful, calling back client '{}'...",
              resources,
              client.getId());
    if (!client.allocationSuccessful(resources)) {
      LOG.warn("{}: Client didn't want allocated resources ({}), unallocating them...",
               client.getId(),
               resources);
      undoAllocate(client, resources);
      enqueue(new AllocatorCommand.RetryAllocates(client));
    }
  }

  /**
   * Allocates the given set of resources, if possible.
   *
   * @param command Describes the requested allocation.
   * @return <code>true</code> if, and only if, the given resources were allocated.
   */
  private boolean tryAllocate(AllocatorCommand.Allocate command) {
    Set<TCSResource<?>> resourcesExpanded = expandResources(command.getResources());
    synchronized (reservationPool) {
      LOG.debug("{}: Checking if all resources are available...", command.getClient().getId());
      if (!reservationPool.resourcesAvailableForUser(resourcesExpanded, command.getClient())) {
        LOG.debug("{}: Resources unavailable.", command.getClient().getId());
        return false;
      }

      LOG.debug("{}: Checking if resources may be allocated...", command.getClient().getId());
      if (!allocationAdvisor.mayAllocate(command.getClient(), command.getResources())) {
        LOG.debug("{}: Resource allocation restricted by some modules.", command.getClient());
        return false;
      }

      LOG.debug("{}: Some resources need to be prepared for allocation.", command.getClient().getId());
      allocationAdvisor.prepareAllocation(command.getClient(), command.getResources());

      LOG.debug("{}: All resources available, allocating...", command.getClient().getId());
      // Allocate resources.
      for (TCSResource<?> curRes : command.getResources()) {
        reservationPool.getReservationEntry(curRes).allocate(command.getClient());
      }

      return true;
    }
  }

  private void allocationsReleased(AllocatorCommand.AllocationsReleased command) {
    allocationAdvisor.allocationReleased(command.getClient(), command.getResources());
  }

  /**
   * Unallocates the given set of resources.
   *
   * @param command Describes the allocated resources.
   */
  private void undoAllocate(Client client, Set<TCSResource<?>> resources) {
    synchronized (reservationPool) {
      reservationPool.free(client, resources);
    }
  }

  /**
   * Moves all waiting allocations back into the incoming queue so they can be rechecked.
   */
  private void retryWaitingAllocations() {
    commands.addAll(deferredAllocations);
    deferredAllocations.clear();
  }

  /**
   * Returns the given set of resources after expansion (by resolution of blocks, for instance) by
   * the kernel.
   *
   * @param resources The set of resources to be expanded.
   * @return The given set of resources after expansion (by resolution of
   * blocks, for instance) by the kernel.
   */
  private Set<TCSResource<?>> expandResources(Set<TCSResource<?>> resources) {
    requireNonNull(resources, "resources");
    // Build a set of references
    Set<TCSResourceReference<?>> refs = resources.stream()
        .map((resource) -> resource.getReference())
        .collect(Collectors.toSet());
    // Let the kernel expand the resources for us.
    Set<TCSResource<?>> result = kernel.expandResources(refs);
    LOG.debug("Set {} expanded to {}", resources, result);
    return result;
  }

}
