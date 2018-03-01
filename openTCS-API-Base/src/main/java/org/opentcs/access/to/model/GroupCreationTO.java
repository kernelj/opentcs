/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a group in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class GroupCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * This group's member names.
   */
  @Nonnull
  private Set<String> memberNames = new HashSet<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this group.
   */
  public GroupCreationTO(String name) {
    super(name);
  }

  /**
   * Sets the name of this group.
   *
   * @param name The new name.
   * @return The modified group.
   */
  @Nonnull
  @Override
  public GroupCreationTO setName(@Nonnull String name) {
    return (GroupCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this group.
   *
   * @param properties The new properties.
   * @return The modified group.
   */
  @Nonnull
  @Override
  public GroupCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (GroupCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this group.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified group.
   */
  @Nonnull
  @Override
  public GroupCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (GroupCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the names of this group's members.
   *
   * @return The names of this group's members.
   */
  @Nonnull
  public Set<String> getMemberNames() {
    return memberNames;
  }

  /**
   * Sets the names of this group's members.
   *
   * @param memberNames The names of this group's members.
   * @return The modified group.
   */
  @Nonnull
  public GroupCreationTO setMemberNames(@Nonnull Set<String> memberNames) {
    this.memberNames = requireNonNull(memberNames, "memberNames");
    return this;
  }
}
