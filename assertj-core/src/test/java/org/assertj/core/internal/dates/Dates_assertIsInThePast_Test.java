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
package org.assertj.core.internal.dates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.error.ShouldBeInThePast.shouldBeInThePast;
import static org.assertj.core.testkit.TestData.someInfo;
import static org.assertj.core.util.DateUtil.monthOf;
import static org.assertj.core.util.FailureMessages.actualIsNull;
import static org.mockito.Mockito.verify;

import java.util.Calendar;
import java.util.Date;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.Dates;
import org.assertj.core.internal.DatesBaseTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for <code>{@link Dates#assertIsInThePast(AssertionInfo, Date)}</code>.
 * 
 * @author Joel Costigliola
 */
class Dates_assertIsInThePast_Test extends DatesBaseTest {

  @Test
  void should_fail_if_actual_is_not_in_the_past() {
    AssertionInfo info = someInfo();
    // init actual so that it is in the future compared to the instant when we call dates.assertIsInThePast
    long oneSecond = 1000;
    actual = new Date(System.currentTimeMillis() + oneSecond);

    Throwable error = catchThrowable(() -> dates.assertIsInThePast(info, actual));

    assertThat(error).isInstanceOf(AssertionError.class);
    verify(failures).failure(info, shouldBeInThePast(actual));
  }

  @Test
  void should_fail_if_actual_is_null() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> dates.assertIsInThePast(someInfo(), null))
                                                   .withMessage(actualIsNull());
  }

  @Test
  void should_pass_if_actual_is_in_the_past() {
    actual = parseDate("2000-01-01");
    dates.assertIsInThePast(someInfo(), actual);
  }

  @Test
  void should_fail_if_actual_is_not_in_the_past_according_to_custom_comparison_strategy() {
    AssertionInfo info = someInfo();
    // set actual to a date in the future according to our comparison strategy (that compares only month and year)
    actual = parseDate("2111-01-01");

    Throwable error = catchThrowable(() -> datesWithCustomComparisonStrategy.assertIsInThePast(info, actual));

    assertThat(error).isInstanceOf(AssertionError.class);
    verify(failures).failure(info, shouldBeInThePast(actual, yearAndMonthComparisonStrategy));
  }

  @Test
  void should_fail_if_actual_is_today_according_to_custom_comparison_strategy() {
    AssertionInfo info = someInfo();
    // we want actual to "now" according to our comparison strategy (that compares only month and year)
    // => if we are at the end of the month we subtract one day instead of adding one
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date tomorrow = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, -2);
    Date yesterday = cal.getTime();
    actual = monthOf(tomorrow) == monthOf(new Date()) ? tomorrow : yesterday;

    Throwable error = catchThrowable(() -> datesWithCustomComparisonStrategy.assertIsInThePast(info, actual));

    assertThat(error).isInstanceOf(AssertionError.class);
    verify(failures).failure(info, shouldBeInThePast(actual, yearAndMonthComparisonStrategy));
  }

  @Test
  void should_fail_if_actual_is_null_whatever_custom_comparison_strategy_is() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> datesWithCustomComparisonStrategy.assertIsInThePast(someInfo(),
                                                                                                                         null))
                                                   .withMessage(actualIsNull());
  }

  @Test
  void should_pass_if_actual_is_in_the_past_according_to_custom_comparison_strategy() {
    actual = parseDate("2000-01-01");
    datesWithCustomComparisonStrategy.assertIsInThePast(someInfo(), actual);
  }

}
