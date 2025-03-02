// Copyright 2020 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.analysis;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.lib.analysis.platform.PlatformInfo;
import com.google.devtools.build.lib.cmdline.Label;
import com.google.devtools.build.lib.packages.ExecGroup;
import com.google.devtools.build.lib.skyframe.serialization.autocodec.AutoCodec;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.InlineMe;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class for a map of exec_group names to their relevant ToolchainContext.
 *
 * @param <T> any class that extends ToolchainContext. This generic allows ToolchainCollection to be
 *     used, e.g., both before and after toolchain resolution.
 * @param contextMap A map of execution group names to toolchain contexts.
 */
@AutoCodec
public record ToolchainCollection<T extends ToolchainContext>(ImmutableMap<String, T> contextMap) {
  public ToolchainCollection {
    requireNonNull(contextMap, "contextMap");
  }

  @InlineMe(replacement = "this.contextMap()")
  public ImmutableMap<String, T> getContextMap() {
    return contextMap();
  }

  public T getDefaultToolchainContext() {
    return getContextMap().get(ExecGroup.DEFAULT_EXEC_GROUP_NAME);
  }

  public boolean hasToolchainContext(String execGroup) {
    return getContextMap().containsKey(execGroup);
  }

  public T getToolchainContext(String execGroup) {
    return getContextMap().get(execGroup);
  }

  public ImmutableSet<Label> getResolvedToolchains() {
    return getContextMap().values().stream()
        .flatMap(c -> c.resolvedToolchainLabels().stream())
        .collect(toImmutableSet());
  }

  public ImmutableSet<String> getExecGroupNames() {
    return getContextMap().keySet();
  }

  /**
   * This is safe because all toolchain context in a toolchain collection should have the same
   * target platform
   */
  public PlatformInfo getTargetPlatform() {
    return getDefaultToolchainContext().targetPlatform();
  }

  @SuppressWarnings("unchecked")
  public ToolchainCollection<ToolchainContext> asToolchainContexts() {
    return (ToolchainCollection<ToolchainContext>) this;
  }

  /** Returns a new builder for {@link ToolchainCollection} instances. */
  public static <T extends ToolchainContext> Builder<T> builder() {
    return new Builder<T>();
  }

  public static <T extends ToolchainContext> Builder<T> builderWithExpectedSize(int expectedSize) {
    return new Builder<T>(expectedSize);
  }

  /** Builder for ToolchainCollection. */
  public static final class Builder<T extends ToolchainContext> {
    // This is not immutable so that we can check for duplicate keys easily.
    private final Map<String, T> toolchainContexts;

    private Builder() {
      this.toolchainContexts = new HashMap<>();
    }

    private Builder(int expectedSize) {
      this.toolchainContexts = newHashMapWithExpectedSize(expectedSize);
    }

    public ToolchainCollection<T> build() {
      Preconditions.checkArgument(toolchainContexts.containsKey(ExecGroup.DEFAULT_EXEC_GROUP_NAME));
      return new ToolchainCollection<T>(ImmutableMap.copyOf(toolchainContexts));
    }

    public void addContext(String execGroup, T context) {
      Preconditions.checkArgument(
          !toolchainContexts.containsKey(execGroup),
          "Duplicate add of '%s' exec group to toolchain collection.",
          execGroup);
      toolchainContexts.put(execGroup, context);
    }

    @CanIgnoreReturnValue
    public Builder<T> addDefaultContext(T context) {
      addContext(ExecGroup.DEFAULT_EXEC_GROUP_NAME, context);
      return this;
    }
  }
}
