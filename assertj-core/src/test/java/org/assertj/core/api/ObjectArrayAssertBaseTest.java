/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2025 the original author or authors.
 */
package org.assertj.core.api;

import static org.assertj.core.testkit.ObjectArrays.emptyArray;
import static org.mockito.Mockito.mock;

import org.assertj.core.internal.Iterables;
import org.assertj.core.internal.ObjectArrays;

/**
 * Base class for {@link ObjectArrayAssert} tests.
 * 
 * @author Olivier Michallat
 */
public abstract class ObjectArrayAssertBaseTest extends BaseTestTemplate<ObjectArrayAssert<Object>, Object[]> {

  protected ObjectArrays arrays;
  protected Iterables iterables;

  @Override
  protected ObjectArrayAssert<Object> create_assertions() {
    return new ObjectArrayAssert<>(emptyArray());
  }

  @Override
  protected void inject_internal_objects() {
    super.inject_internal_objects();
    arrays = mock(ObjectArrays.class);
    iterables = mock(Iterables.class);
    assertions.arrays = arrays;
    assertions.iterables = iterables;
  }

  protected ObjectArrays getArrays(ObjectArrayAssert<Object> someAssertions) {
    return someAssertions.arrays;
  }
}
