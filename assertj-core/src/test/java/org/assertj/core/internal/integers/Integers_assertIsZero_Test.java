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
package org.assertj.core.internal.integers;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.testkit.ErrorMessagesForTest.shouldBeEqualMessage;
import static org.assertj.core.testkit.TestData.someInfo;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.Integers;
import org.assertj.core.internal.IntegersBaseTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for <code>{@link Integers#assertIsNegative(AssertionInfo, Integer)}</code>.
 *
 * @author Alex Ruiz
 * @author Joel Costigliola
 */
class Integers_assertIsZero_Test extends IntegersBaseTest {

  @Test
  void should_succeed_since_actual_is_zero() {
    integers.assertIsZero(someInfo(), 0);
  }

  @Test
  void should_fail_since_actual_is_not_zero() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> integers.assertIsZero(someInfo(), 2))
                                                   .withMessage(shouldBeEqualMessage("2", "0"));
  }

  @Test
  void should_succeed_since_actual_is_zero_whatever_custom_comparison_strategy_is() {
    integersWithAbsValueComparisonStrategy.assertIsZero(someInfo(), 0);
  }

  @Test
  void should_fail_since_actual_is_not_zero_whatever_custom_comparison_strategy_is() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> integersWithAbsValueComparisonStrategy.assertIsZero(someInfo(),
                                                                                                                         1))
                                                   .withMessage(shouldBeEqualMessage("1", "0"));
  }

}
