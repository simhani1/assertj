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
package org.assertj.tests.core.internal.classes;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.error.ShouldHaveNoFields.shouldHaveNoDeclaredFields;
import static org.assertj.core.error.ShouldOnlyHaveFields.shouldOnlyHaveDeclaredFields;
import static org.assertj.tests.core.testkit.TestData.someInfo;
import static org.assertj.core.util.Arrays.array;
import static org.assertj.core.util.FailureMessages.actualIsNull;
import static org.assertj.core.util.Sets.newLinkedHashSet;

import java.util.LinkedHashSet;
import org.assertj.tests.core.internal.ClassesBaseTest;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for
 * <code
 * >{@link org.assertj.core.internal.Classes#assertHasOnlyDeclaredFields(org.assertj.core.api.AssertionInfo, Class, String...)}</code>
 * .
 *
 * @author Filip Hrisafov
 */
class Classes_assertHasOnlyDeclaredFields_Test extends ClassesBaseTest {

  private static final LinkedHashSet<String> EMPTY_STRING_SET = Sets.newLinkedHashSet();

  @BeforeEach
  void setupActual() {
    actual = AnnotatedClass.class;
  }

  @Test
  void should_pass_if_class_has_all_the_expected_declared_fields() {
    classes.assertHasOnlyDeclaredFields(someInfo(), actual, "publicField", "publicField2", "protectedField",
                                        "privateField");
  }

  @Test
  void should_pass_if_class_has_all_the_expected_declared_fields_whatever_the_order_is() {
    classes.assertHasOnlyDeclaredFields(someInfo(), actual, "protectedField", "privateField", "publicField2",
                                        "publicField");
  }

  @Test
  void should_pass_if_class_has_no_declared_fields_and_none_are_expected() {
    classes.assertHasOnlyDeclaredFields(someInfo(), NoField.class);
  }

  @Test
  void should_fail_if_actual_is_null() {
    actual = null;
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> classes.assertHasOnlyDeclaredFields(someInfo(), actual))
                                                   .withMessage(actualIsNull());
  }

  @Test
  void should_fail_if_not_all_fields_are_expected() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> classes.assertHasOnlyDeclaredFields(someInfo(),
                                                                                                         actual,
                                                                                                         "publicField",
                                                                                                         "protectedField",
                                                                                                         "privateField"))
                                                   .withMessage(shouldOnlyHaveDeclaredFields(actual,
                                                                                             newLinkedHashSet("publicField",
                                                                                                              "protectedField",
                                                                                                              "privateField"),
                                                                                             EMPTY_STRING_SET,
                                                                                             newLinkedHashSet("publicField2")).create()
                                                                                                                              .formatted());
  }

  @Test
  void should_fail_if_fields_are_missing() {
    String[] expected = array("missingField", "publicField", "publicField2", "protectedField", "privateField");
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> classes.assertHasOnlyDeclaredFields(someInfo(),
                                                                                                         actual,
                                                                                                         expected))
                                                   .withMessage(shouldOnlyHaveDeclaredFields(actual,
                                                                                             newLinkedHashSet(expected),
                                                                                             newLinkedHashSet("missingField"),
                                                                                             EMPTY_STRING_SET).create()
                                                                                                              .formatted());
  }

  @Test
  void should_fail_if_fields_are_not_expected_and_not_found() {
    String[] expected = array("publicField", "publicField2", "missing", "privateField");
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> classes.assertHasOnlyDeclaredFields(someInfo(),
                                                                                                         actual,
                                                                                                         expected))
                                                   .withMessage(shouldOnlyHaveDeclaredFields(actual,
                                                                                             newLinkedHashSet(expected),
                                                                                             newLinkedHashSet("missing"),
                                                                                             newLinkedHashSet("protectedField")).create()
                                                                                                                                .formatted());
  }

  @Test
  void should_fail_if_no_declared_fields_are_expected_and_class_has_some() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> classes.assertHasOnlyDeclaredFields(someInfo(),
                                                                                                         actual))
                                                   .withMessage(shouldHaveNoDeclaredFields(actual,
                                                                                           newLinkedHashSet("publicField",
                                                                                                            "publicField2",
                                                                                                            "protectedField",
                                                                                                            "privateField")).create()
                                                                                                                            .formatted());
  }

}
