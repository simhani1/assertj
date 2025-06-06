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
package org.assertj.tests.core.api.recursive.comparison.fields;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.tests.core.api.recursive.data.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecursiveComparisonAssert_bddSoftAssertions_Test extends WithComparingFieldsIntrospectionStrategyBaseTest {

  private BDDSoftAssertions softly;

  @BeforeEach
  public void beforeEachTest() {
    super.beforeEachTest();
    Assertions.setRemoveAssertJRelatedElementsFromStackTrace(false);
    softly = new BDDSoftAssertions();
  }

  @Test
  void should_pass_with_bdd_soft_assertions() {
    // GIVEN
    Person actual = new Person("John");
    actual.home.address.number = 1;
    Person expected = new Person("John");
    expected.home.address.number = 1;
    // WHEN
    softly.then(actual).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(expected);
    // THEN
    softly.assertAll();
  }

  @Test
  void should_report_all_errors_with_bdd_soft_assertions() {
    // GIVEN
    Person john = new Person("John");
    john.home.address.number = 1;
    Person jack = new Person("Jack");
    jack.home.address.number = 2;
    // WHEN
    softly.then(john).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(jack);
    softly.then(jack).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(john);
    // THEN
    List<Throwable> errorsCollected = softly.errorsCollected();
    then(errorsCollected).hasSize(2);
    then(errorsCollected.get(0)).hasMessageContaining("field/property 'home.address.number' differ:")
                                .hasMessageContaining("- actual value  : 1")
                                .hasMessageContaining("- expected value: 2");
    then(errorsCollected.get(1)).hasMessageContaining("field/property 'home.address.number' differ:")
                                .hasMessageContaining("- actual value  : 2")
                                .hasMessageContaining("- expected value: 1");
  }

}
