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
package org.assertj.core.api.bytearray;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.testkit.AlwaysEqualComparator.alwaysEqual;

import java.util.Comparator;

import org.assertj.core.api.ByteArrayAssert;
import org.assertj.core.api.ByteArrayAssertBaseTest;
import org.assertj.core.internal.ByteArrays;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for <code>{@link ByteArrayAssert#usingComparator(java.util.Comparator)}</code>.
 *
 * @author Joel Costigliola
 * @author Mikhail Mazursky
 */
class ByteArrayAssert_usingComparator_Test extends ByteArrayAssertBaseTest {

  private Comparator<byte[]> comparator = alwaysEqual();

  private ByteArrays arraysBefore;

  @BeforeEach
  void before() {
    arraysBefore = getArrays(assertions);
  }

  @Override
  protected ByteArrayAssert invoke_api_method() {
    // in that test, the comparator type is not important, we only check that we correctly switch of comparator
    return assertions.usingComparator(comparator);
  }

  @Override
  protected void verify_internal_effects() {
    assertThat(getObjects(assertions).getComparator()).isSameAs(comparator);
    assertThat(getArrays(assertions)).isSameAs(arraysBefore);
  }
}
