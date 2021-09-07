// Copyright 2021 The Bazel Authors. All rights reserved.
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
//

package com.google.devtools.build.lib.bazel.bzlmod;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.build.lib.skyframe.SkyFunctions;
import com.google.devtools.build.lib.skyframe.serialization.autocodec.AutoCodec;
import com.google.devtools.build.skyframe.SkyKey;
import com.google.devtools.build.skyframe.SkyValue;

/** The result of discovery, containing the initial dependency graph. */
@AutoValue
public abstract class DiscoveryValue implements SkyValue {

  @AutoCodec public static final SkyKey KEY = () -> SkyFunctions.DISCOVERY;

  public static DiscoveryValue create(ImmutableMap<ModuleKey, Module> depGraph) {
    return new AutoValue_DiscoveryValue(depGraph);
  }

  /**
   * The initial dependency graph. For any KEY in the returned map, it's guaranteed that {@code
   * depGraph[KEY].getKey() == KEY}.
   */
  public abstract ImmutableMap<ModuleKey, Module> getDepGraph();
}
