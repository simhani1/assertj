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
package org.assertj.core.api.longarray;

import static org.assertj.core.testkit.AlwaysEqualComparator.alwaysEqual;
import static org.mockito.Mockito.verify;

import java.util.Comparator;

import org.assertj.core.api.LongArrayAssert;
import org.assertj.core.api.LongArrayAssertBaseTest;

/**
 * Tests for <code>{@link LongArrayAssert#isSortedAccordingTo(Comparator)}</code>.
 *
 * @author Joel Costigliola
 */
class LongArrayAssert_isSortedAccordingToComparator_Test extends LongArrayAssertBaseTest {

  private Comparator<Long> comparator = alwaysEqual();

  @Override
  protected LongArrayAssert invoke_api_method() {
    return assertions.isSortedAccordingTo(comparator);
  }

  @Override
  protected void verify_internal_effects() {
    verify(arrays).assertIsSortedAccordingToComparator(getInfo(assertions), getActual(assertions), comparator);
  }

}
