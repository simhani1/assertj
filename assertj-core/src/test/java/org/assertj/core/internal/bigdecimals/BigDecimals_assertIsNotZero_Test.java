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
package org.assertj.core.internal.bigdecimals;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.testkit.TestData.someInfo;

import java.math.BigDecimal;
import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.BigDecimals;
import org.assertj.core.internal.BigDecimalsBaseTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for <code>{@link BigDecimals#assertIsNotZero(AssertionInfo, BigDecimal)}</code>.
 * 
 * @author Yvonne Wang
 * @author Joel Costigliola
 */
class BigDecimals_assertIsNotZero_Test extends BigDecimalsBaseTest {

  @Test
  void should_succeed_since_actual_is_zero() {
    numbers.assertIsNotZero(someInfo(), BigDecimal.ONE);
  }

  @Test
  void should_fail_since_actual_is_not_zero() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> numbers.assertIsNotZero(someInfo(), BigDecimal.ZERO))
                                                   .withMessage("%nExpecting actual:%n  0%nnot to be equal to:%n  0%n".formatted());
  }

  @Test
  void should_succeed_since_actual_is_zero_whatever_custom_comparison_strategy_is() {
    numbersWithComparatorComparisonStrategy.assertIsNotZero(someInfo(), BigDecimal.ONE);
  }

  @Test
  void should_fail_since_actual_is_not_zero_whatever_custom_comparison_strategy_is() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> numbersWithComparatorComparisonStrategy.assertIsNotZero(someInfo(),
                                                                                                                             BigDecimal.ZERO))
                                                   .withMessage("%nExpecting actual:%n  0%nnot to be equal to:%n  0%n".formatted());
  }

}
