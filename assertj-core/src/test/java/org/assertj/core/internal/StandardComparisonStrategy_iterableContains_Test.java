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
package org.assertj.core.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

import java.util.List;

import org.assertj.core.api.comparisonstrategy.StandardComparisonStrategy;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StandardComparisonStrategy#iterableContains(java.util.Collection, Object)}.
 * 
 * @author Joel Costigliola
 */
class StandardComparisonStrategy_iterableContains_Test extends AbstractTest_StandardComparisonStrategy {

  @Test
  void should_pass() {
    List<?> list = newArrayList("Sam", "Merry", null, "Frodo");
    assertThat(standardComparisonStrategy.iterableContains(list, "Frodo")).isTrue();
    assertThat(standardComparisonStrategy.iterableContains(list, null)).isTrue();
    assertThat(standardComparisonStrategy.iterableContains(list, "Sauron")).isFalse();
  }

  @Test
  void should_return_false_if_iterable_is_null() {
    assertThat(standardComparisonStrategy.iterableContains(null, "Sauron")).isFalse();
  }

}
