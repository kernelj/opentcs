/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.access.Kernel;

/**
 * A query for general information about the routing tables.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Availability(Kernel.State.OPERATING)
public class QueryRoutingInfo
    extends Query<QueryRoutingInfo>
    implements Serializable {

  /**
   * The router's info string.
   */
  private final String routingInfo;

  /**
   * Creates a new instance.
   *
   * @param info Some general information about the routing tables.
   */
  public QueryRoutingInfo(String info) {
    this.routingInfo = Objects.requireNonNull(info, "info is null");
  }

  /**
   * Returns some general information about the routing tables.
   *
   * @return Some general information about the routing tables.
   */
  public String getRoutingInfo() {
    return routingInfo;
  }
}
