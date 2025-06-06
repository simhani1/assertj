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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.filter.Filters.filter;
import static org.assertj.core.description.Description.mostRelevantDescription;
import static org.assertj.core.extractor.Extractors.byName;
import static org.assertj.core.extractor.Extractors.extractedDescriptionOf;
import static org.assertj.core.extractor.Extractors.extractedDescriptionOfMethod;
import static org.assertj.core.extractor.Extractors.resultOf;
import static org.assertj.core.internal.CommonValidations.checkSequenceIsNotNull;
import static org.assertj.core.internal.CommonValidations.checkSubsequenceIsNotNull;
import static org.assertj.core.internal.TypeComparators.defaultTypeComparators;
import static org.assertj.core.util.Arrays.array;
import static org.assertj.core.util.Arrays.isArray;
import static org.assertj.core.util.IterableUtil.toArray;
import static org.assertj.core.util.Lists.newArrayList;
import static org.assertj.core.util.Preconditions.checkArgument;
import static org.assertj.core.util.Preconditions.checkNotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.assertj.core.api.comparisonstrategy.AtomicReferenceArrayElementComparisonStrategy;
import org.assertj.core.api.comparisonstrategy.ComparatorBasedComparisonStrategy;
import org.assertj.core.api.filter.FilterOperator;
import org.assertj.core.api.filter.Filters;
import org.assertj.core.api.iterable.ThrowingExtractor;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.assertj.core.condition.Not;
import org.assertj.core.data.Index;
import org.assertj.core.description.Description;
import org.assertj.core.groups.FieldsOrPropertiesExtractor;
import org.assertj.core.groups.Tuple;
import org.assertj.core.internal.CommonErrors;
import org.assertj.core.internal.ConfigurableRecursiveFieldByFieldComparator;
import org.assertj.core.internal.ExtendedByTypesComparator;
import org.assertj.core.internal.Iterables;
import org.assertj.core.internal.ObjectArrays;
import org.assertj.core.internal.Objects;
import org.assertj.core.internal.TypeComparators;
import org.assertj.core.presentation.PredicateDescription;
import org.assertj.core.util.CheckReturnValue;
import org.assertj.core.util.introspection.IntrospectionError;

// suppression of deprecation works in Eclipse to hide warning for the deprecated classes in the imports
// Deprecation is raised by JDK-17. IntelliJ thinks this is redundant when it is not.
@SuppressWarnings({ "deprecation", "RedundantSuppression" })
public class AtomicReferenceArrayAssert<T>
    extends AbstractAssertWithComparator<AtomicReferenceArrayAssert<T>, AtomicReferenceArray<T>>
    implements IndexedObjectEnumerableAssert<AtomicReferenceArrayAssert<T>, T>,
    ArraySortedAssert<AtomicReferenceArrayAssert<T>, T> {

  private final T[] array;
  // TODO reduce the visibility of the fields annotated with @VisibleForTesting
  ObjectArrays arrays = ObjectArrays.instance();
  // TODO reduce the visibility of the fields annotated with @VisibleForTesting
  Iterables iterables = Iterables.instance();

  private TypeComparators comparatorsByType;
  private TypeComparators comparatorsForElementPropertyOrFieldTypes;

  public AtomicReferenceArrayAssert(AtomicReferenceArray<T> actual) {
    super(actual, AtomicReferenceArrayAssert.class);
    array = array(actual);
  }

  @Override
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> as(Description description) {
    return super.as(description);
  }

  @Override
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> as(String description, Object... args) {
    return super.as(description, args);
  }

  /**
   * Verifies that the AtomicReferenceArray is {@code null} or empty.
   * <p>
   * Example:
   * <pre><code class='java'> // assertions will pass
   * assertThat(new AtomicReferenceArray&lt;&gt;(new String[0])).isNullOrEmpty();
   * AtomicReferenceArray array = null;
   * assertThat(array).isNullOrEmpty();
   *
   * // assertion will fail
   * assertThat(new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "c"})).isNullOrEmpty();</code></pre>
   *
   * @throws AssertionError if the AtomicReferenceArray is not {@code null} or not empty.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public void isNullOrEmpty() {
    if (actual == null) return;
    isEmpty();
  }

  /**
   * Verifies that the AtomicReferenceArray is empty.
   * <p>
   * Example:
   * <pre><code class='java'> // assertion will pass
   * assertThat(new AtomicReferenceArray&lt;&gt;(new String[0])).isEmpty();
   *
   * // assertion will fail
   * assertThat(new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"})).isEmpty();</code></pre>
   *
   * @throws AssertionError if the AtomicReferenceArray is not empty.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public void isEmpty() {
    arrays.assertEmpty(info, array);
  }

  /**
   * Verifies that the AtomicReferenceArray is not empty.
   * <p>
   * Example:
   * <pre><code class='java'> // assertion will pass
   * assertThat(new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"})).isNotEmpty();
   *
   * // assertion will fail
   * assertThat(new AtomicReferenceArray&lt;&gt;(new String[0])).isNotEmpty();</code></pre>
   *
   * @return {@code this} assertion object.
   * @throws AssertionError if the AtomicReferenceArray is empty.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> isNotEmpty() {
    arrays.assertNotEmpty(info, array);
    return myself;
  }

  /**
   * Verifies that the AtomicReferenceArray has the given array.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; atomicArray = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertion will pass
   * assertThat(atomicArray).hasArray(new String[]{"a", "b", "c"});
   *
   * // assertion will fail
   * assertThat(atomicArray).hasArray(new String[]{"a", "b", "c", "d"});</code></pre>
   *
   * @param expected the array expected to be in the actual AtomicReferenceArray.
   * @return {@code this} assertion object.
   * @throws AssertionError if the AtomicReferenceArray does not have the given array.
   * @since 2.7.0 / 3.7.0
   */
  public AtomicReferenceArrayAssert<T> hasArray(T[] expected) {
    arrays.assertContainsExactly(info, array, expected);
    return myself;
  }

  /**
   * Verifies that the number of values in the AtomicReferenceArray is equal to the given one.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; atomicArray = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * assertThat(atomicArray).hasSize(3);
   *
   * // assertion will fail
   * assertThat(atomicArray).hasSize(1);</code></pre>
   *
   * @param expected the expected number of values in the actual AtomicReferenceArray.
   * @return {@code this} assertion object.
   * @throws AssertionError if the number of values of the AtomicReferenceArray is not equal to the given one.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSize(int expected) {
    arrays.assertHasSize(info, array, expected);
    return myself;
  }

  /**
   * Verifies that the number of values in the actual array is greater than the given boundary.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray atomicReferenceArray = new AtomicReferenceArray(new String[] { "a", "b", "c" });
   *
   * // assertion will pass
   * assertThat(atomicReferenceArray).hasSizeGreaterThan(1);
   *
   * // assertion will fail
   * assertThat(atomicReferenceArray).hasSizeGreaterThan(3);</code></pre>
   *
   * @param boundary the given value to compare the actual size to.
   * @return {@code this} assertion object.
   * @throws AssertionError if the number of values of the actual array is not greater than the boundary.
   * @since 3.12.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSizeGreaterThan(int boundary) {
    arrays.assertHasSizeGreaterThan(info, array, boundary);
    return myself;
  }

  /**
   * Verifies that the number of values in the actual array is greater than or equal to the given boundary.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray atomicReferenceArray = new AtomicReferenceArray(new String[] { "a", "b", "c" });
   *
   * // assertion will pass
   * assertThat(atomicReferenceArray).hasSizeGreaterThanOrEqualTo(3);
   *
   * // assertion will fail
   * assertThat(atomicReferenceArray).hasSizeGreaterThanOrEqualTo(5);</code></pre>
   *
   * @param boundary the given value to compare the actual size to.
   * @return {@code this} assertion object.
   * @throws AssertionError if the number of values of the actual array is not greater than or equal to the boundary.
   * @since 3.12.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSizeGreaterThanOrEqualTo(int boundary) {
    arrays.assertHasSizeGreaterThanOrEqualTo(info, array, boundary);
    return myself;
  }

  /**
   * Verifies that the number of values in the actual array is less than the given boundary.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray atomicReferenceArray = new AtomicReferenceArray(new String[] { "a", "b", "c" });
   *
   * // assertion will pass
   * assertThat(atomicReferenceArray).hasSizeLessThan(4);
   *
   * // assertion will fail
   * assertThat(atomicReferenceArray).hasSizeLessThan(2);</code></pre>
   *
   * @param boundary the given value to compare the actual size to.
   * @return {@code this} assertion object.
   * @throws AssertionError if the number of values of the actual array is not less than the boundary.
   * @since 3.12.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSizeLessThan(int boundary) {
    arrays.assertHasSizeLessThan(info, array, boundary);
    return myself;
  }

  /**
   * Verifies that the number of values in the actual array is less than or equal to the given boundary.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray atomicReferenceArray = new AtomicReferenceArray(new String[] { "a", "b", "c" });
   *
   * // assertion will pass
   * assertThat(atomicReferenceArray).hasSizeLessThanOrEqualTo(3);
   *
   * // assertion will fail
   * assertThat(atomicReferenceArray).hasSizeLessThanOrEqualTo(2);</code></pre>
   *
   * @param boundary the given value to compare the actual size to.
   * @return {@code this} assertion object.
   * @throws AssertionError if the number of values of the actual array is not less than or equal to the boundary.
   * @since 3.12.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSizeLessThanOrEqualTo(int boundary) {
    arrays.assertHasSizeLessThanOrEqualTo(info, array, boundary);
    return myself;
  }

  /**
   * Verifies that the number of values in the actual array is between the given boundaries (inclusive).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray atomicReferenceArray = new AtomicReferenceArray(new String[] { "a", "b", "c" });
   *
   * // assertion will pass
   * assertThat(atomicReferenceArray).hasSizeBetween(3, 4);
   *
   * // assertion will fail
   * assertThat(atomicReferenceArray).hasSizeBetween(4, 6);</code></pre>
   *
   * @param lowerBoundary the lower boundary compared to which actual size should be greater than or equal to.
   * @param higherBoundary the higher boundary compared to which actual size should be less than or equal to.
   * @return {@code this} assertion object.
   * @throws AssertionError if the number of values of the actual array is not between the boundaries.
   * @since 3.12.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSizeBetween(int lowerBoundary, int higherBoundary) {
    arrays.assertHasSizeBetween(info, array, lowerBoundary, higherBoundary);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray has the same size as the given array.
   * <p>
   * Parameter is declared as Object to accept both {@code Object[]} and primitive arrays (e.g. {@code int[]}).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * int[] fourFiveSix = {4, 5, 6};
   * int[] sevenEight = {7, 8};
   *
   * // assertion will pass
   * assertThat(abc).hasSameSizeAs(fourFiveSix);
   *
   * // assertion will fail
   * assertThat(abc).hasSameSizeAs(sevenEight);</code></pre>
   *
   * @param other the array to compare size with actual AtomicReferenceArray.
   * @return {@code this} assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the array parameter is {@code null} or is not a true array.
   * @throws AssertionError if actual AtomicReferenceArray and given array don't have the same size.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSameSizeAs(Object other) {
    arrays.assertHasSameSizeAs(info, array, other);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray has the same size as the given {@link Iterable}.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * Iterable&lt;Ring&gt; elvesRings = newArrayList(vilya, nenya, narya);
   *
   * // assertion will pass
   * assertThat(abc).hasSameSizeAs(elvesRings);
   *
   * // assertion will fail
   * assertThat(abc).hasSameSizeAs(Arrays.asList("a", "b"));</code></pre>
   *
   * @param other the {@code Iterable} to compare size with actual AtomicReferenceArray.
   * @return {@code this} assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the other {@code Iterable} is {@code null}.
   * @throws AssertionError if actual AtomicReferenceArray and given {@code Iterable} don't have the same size.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSameSizeAs(Iterable<?> other) {
    arrays.assertHasSameSizeAs(info, array, other);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given values, in any order.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertions will pass
   * assertThat(abc).contains("b", "a")
   *                .contains("b", "a", "b");
   *
   * // assertions will fail
   * assertThat(abc).contains("d");
   * assertThat(abc).contains("c", "d");</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws IllegalArgumentException if the given argument is an empty array.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given values.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> contains(T... values) {
    return containsForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsForProxy(T[] values) {
    arrays.assertContains(info, array, values);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains only the given values and nothing else, <b>in any order</b>  and ignoring duplicates (i.e. once a value is found, its duplicates are also considered found).
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertions will pass
   * assertThat(abc).containsOnly("c", "b", "a")
   *                .containsOnly("a", "a", "b", "c", "c");
   *
   * // assertion will fail because "c" is missing from the given values
   * assertThat(abc).containsOnly("a", "b");
   * // assertion will fail because abc does not contain "d"
   * assertThat(abc).containsOnly("a", "b", "c", "d");</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws IllegalArgumentException if the given argument is an empty array.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given values, i.e. the actual AtomicReferenceArray contains some
   *           or none of the given values, or the actual AtomicReferenceArray contains more values than the given ones.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsOnly(T... values) {
    return containsOnlyForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsOnlyForProxy(T[] values) {
    arrays.assertContainsOnly(info, array, values);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains only null elements and nothing else.
   * <p>
   * Example :
   * <pre><code class='java'> // assertion will pass
   * AtomicReferenceArray&lt;String&gt; items = new AtomicReferenceArray&lt;&gt;(new String[]{null, null, null});
   * assertThat(items).containsOnlyNulls();
   *
   * // assertion will fail because items2 contains not null element
   * AtomicReferenceArray&lt;String&gt; items2 = new AtomicReferenceArray&lt;&gt;(new String[]{null, null, "notNull"});
   * assertThat(items2).containsOnlyNulls();
   *
   * // assertion will fail since an empty array does not contain any elements and therefore no null ones.
   * AtomicReferenceArray&lt;String&gt; empty = new AtomicReferenceArray&lt;&gt;(new String[0]);
   * assertThat(empty).containsOnlyNulls();</code></pre>
   *
   * @return {@code this} assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray is empty or contains non-null elements.
   * @since 2.9.0 / 3.9.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsOnlyNulls() {
    arrays.assertContainsOnlyNulls(info, array);
    return myself;
  }

  /**
   * Verifies that actual contains all elements of the given {@code Iterable} and nothing else, <b>in any order</b>.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass:
   * assertThat(elvesRings).hasSameElementsAs(newArrayList(nenya, narya, vilya))
   *                       .hasSameElementsAs(newArrayList(nenya, narya, vilya, nenya));
   *
   * // assertions will fail:
   * assertThat(elvesRings).hasSameElementsAs(newArrayList(nenya, narya));
   * assertThat(elvesRings).hasSameElementsAs(newArrayList(nenya, narya, vilya, oneRing));</code></pre>
   *
   * @param iterable the {@code Iterable} whose elements we expect to be present
   * @return this assertion object
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}
   * @throws NullPointerException if the given {@code Iterable} is {@code null}
   * @throws AssertionError if the actual {@code Iterable} does not have the same elements, in any order, as the given
   *           {@code Iterable}
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasSameElementsAs(Iterable<? extends T> iterable) {
    return containsOnly(toArray(iterable));
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given values only once.
   * <p>
   * Examples :
   * <pre><code class='java'> // array is a factory method to create arrays.
   * AtomicReferenceArray&lt;String&gt; got = new AtomicReferenceArray&lt;&gt;(new String[]{&quot;winter&quot;, &quot;is&quot;, &quot;coming&quot;});
   *
   * // assertions will pass
   * assertThat(got).containsOnlyOnce(&quot;winter&quot;)
   *                .containsOnlyOnce(&quot;coming&quot;, &quot;winter&quot;);
   *
   * // assertions will fail
   * AtomicReferenceArray&lt;String&gt; stark= new AtomicReferenceArray&lt;&gt;(new String[]{&quot;Arya&quot;, &quot;Stark&quot;, &quot;daughter&quot;, &quot;of&quot;, &quot;Ned&quot;, &quot;Stark&quot;)});
   * assertThat(got).containsOnlyOnce(&quot;Lannister&quot;);
   * assertThat(stark).containsOnlyOnce(&quot;Stark&quot;);</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws IllegalArgumentException if the given argument is an empty array.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given values, i.e. the actual AtomicReferenceArray contains some
   *           or none of the given values, or the actual AtomicReferenceArray contains more than once these values.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsOnlyOnce(T... values) {
    return containsOnlyOnceForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsOnlyOnceForProxy(T[] values) {
    arrays.assertContainsOnlyOnce(info, array, values);
    return myself;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsOnlyOnceElementsOf(Iterable<? extends T> iterable) {
    return containsOnlyOnce(toArray(iterable));
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains only the given values and nothing else, <b>in order</b>.<br>
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass
   * assertThat(elvesRings).containsExactly(vilya, nenya, narya);
   *
   * // assertion will fail as actual and expected order differ
   * assertThat(elvesRings).containsExactly(nenya, vilya, narya);</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given values with same order, i.e. the actual AtomicReferenceArray
   *           contains some or none of the given values, or the actual AtomicReferenceArray contains more values than the given ones
   *           or values are the same but the order is not.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsExactly(T... values) {
    return containsExactlyForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsExactlyForProxy(T[] values) {
    arrays.assertContainsExactly(info, array, values);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains exactly the given values and nothing else, <b>in any order</b>.<br>
   *
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass
   * assertThat(elvesRings).containsExactlyInAnyOrder(vilya, vilya, nenya, narya);
   *
   * // assertion will fail as vilya is contained twice in elvesRings.
   * assertThat(elvesRings).containsExactlyInAnyOrder(nenya, vilya, narya);</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual group is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given values, i.e. it
   *           contains some or none of the given values, or more values than the given ones.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsExactlyInAnyOrder(T... values) {
    return containsExactlyInAnyOrderForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsExactlyInAnyOrderForProxy(T[] values) {
    arrays.assertContainsExactlyInAnyOrder(info, array, values);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains exactly the given values and nothing else, <b>in any order</b>.<br>
   *
   * <p>
   * Example :
   * <pre><code class='java'>
   * AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray(new Ring[]{vilya, nenya, narya, vilya});
   * AtomicReferenceArray&lt;Ring&gt; elvesRingsSomeMissing = new AtomicReferenceArray(new Ring[]{vilya, nenya, narya});
   * AtomicReferenceArray&lt;Ring&gt; elvesRingsDifferentOrder = new AtomicReferenceArray(new Ring[]{nenya, narya, vilya, vilya});
   *
   * // assertion will pass
   * assertThat(elvesRings).containsExactlyInAnyOrder(elvesRingsDifferentOrder);
   *
   * // assertion will fail as vilya is contained twice in elvesRings.
   * assertThat(elvesRings).containsExactlyInAnyOrder(elvesRingsSomeMissing);</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual group is {@code null}.
   * @throws AssertionError if the actual group does not contain the given values, i.e. the actual group
   *           contains some or none of the given values, or the actual group contains more values than the given ones.
   * @since 2.9.0 / 3.9.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsExactlyInAnyOrderElementsOf(Iterable<? extends T> values) {
    return containsExactlyInAnyOrder(toArray(values));
  }

  /**
   * Same as {@link #containsExactly(Object...)} but handles the {@link Iterable} to array conversion : verifies that
   * actual contains all elements of the given {@code Iterable} and nothing else <b>in the same order</b>.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass
   * assertThat(elvesRings).containsExactlyElementsOf(newLinkedList(vilya, nenya, narya));
   *
   * // assertion will fail as actual and expected order differ
   * assertThat(elvesRings).containsExactlyElementsOf(newLinkedList(nenya, vilya, narya));</code></pre>
   *
   * @param iterable the given {@code Iterable} we will get elements from.
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsExactlyElementsOf(Iterable<? extends T> iterable) {
    return containsExactly(toArray(iterable));
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given sequence in the correct order and <b>without extra values between the sequence values</b>.
   * <p>
   * Use {@link #containsSubsequence(Object...)} to allow values between the expected sequence values.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass
   * assertThat(elvesRings).containsSequence(vilya, nenya)
   *                       .containsSequence(nenya, narya);
   *
   * // assertions will fail, the elements order is correct but there is a value between them (nenya)
   * assertThat(elvesRings).containsSequence(vilya, narya);
   * assertThat(elvesRings).containsSequence(nenya, vilya);</code></pre>
   *
   * @param sequence the sequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given sequence.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsSequence(T... sequence) {
    return containsSequenceForProxy(sequence);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsSequenceForProxy(T[] sequence) {
    arrays.assertContainsSequence(info, array, sequence);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given sequence in the correct order and <b>without extra values between the sequence values</b>.
   * <p>
   * Use {@link #containsSubsequence(Object...)} to allow values between the expected sequence values.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass
   * assertThat(elvesRings).containsSequence(newArrayList(vilya, nenya))
   *                       .containsSequence(newArrayList(nenya, narya));
   *
   * // assertions will fail, the elements order is correct but there is a value between them (nenya)
   * assertThat(elvesRings).containsSequence(newArrayList(vilya, narya));
   * assertThat(elvesRings).containsSequence(newArrayList(nenya, vilya));</code></pre>
   *
   * @param sequence the sequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given sequence.
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsSequence(Iterable<? extends T> sequence) {
    checkSequenceIsNotNull(sequence);
    arrays.assertContainsSequence(info, array, toArray(sequence));
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given sequence in the given order and <b>without extra values between the sequence values</b>.
   * <p>
   * Use {@link #doesNotContainSubsequence(Object...)} to also ensure the sequence does not exist with values between the expected sequence values.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass, the elements order is correct but there is a value between them (nenya)
   * assertThat(elvesRings).containsSequence(vilya, narya);
   * assertThat(elvesRings).containsSequence(nenya, vilya);
   *
   * // assertions will fail
   * assertThat(elvesRings).containsSequence(vilya, nenya);
   * assertThat(elvesRings).containsSequence(nenya, narya);</code></pre>
   *
   * @param sequence the sequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given sequence.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> doesNotContainSequence(T... sequence) {
    return doesNotContainSequenceForProxy(sequence);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> doesNotContainSequenceForProxy(T[] sequence) {
    arrays.assertDoesNotContainSequence(info, array, sequence);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given sequence in the given order and <b>without extra values between the sequence values</b>.
   * <p>
   * Use {@link #doesNotContainSubsequence(Iterable)} to also ensure the sequence does not exist with values between the expected sequence values.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertion will pass, the elements order is correct but there is a value between them (nenya)
   * assertThat(elvesRings).containsSequence(newArrayList(vilya, narya));
   * assertThat(elvesRings).containsSequence(newArrayList(nenya, vilya));
   *
   * // assertions will fail
   * assertThat(elvesRings).containsSequence(newArrayList(vilya, nenya));
   * assertThat(elvesRings).containsSequence(newArrayList(nenya, narya));</code></pre>
   *
   * @param sequence the sequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given sequence.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotContainSequence(Iterable<? extends T> sequence) {
    checkSequenceIsNotNull(sequence);
    arrays.assertDoesNotContainSequence(info, array, toArray(sequence));
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given subsequence in the correct order (possibly with other values between them).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass
   * assertThat(elvesRings).containsSubsequence(vilya, nenya)
   *                       .containsSubsequence(vilya, narya);
   *
   * // assertion will fail
   * assertThat(elvesRings).containsSubsequence(nenya, vilya);</code></pre>
   *
   * @param subsequence the subsequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given subsequence.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsSubsequence(T... subsequence) {
    return containsSubsequenceForProxy(subsequence);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsSubsequenceForProxy(T[] subsequence) {
    arrays.assertContainsSubsequence(info, array, subsequence);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given subsequence in the correct order (possibly with other values between them).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass
   * assertThat(elvesRings).containsSubsequence(newArrayList(vilya, nenya))
   *                       .containsSubsequence(newArrayList(vilya, narya));
   *
   * // assertion will fail
   * assertThat(elvesRings).containsSubsequence(newArrayList(nenya, vilya));</code></pre>
   *
   * @param subsequence the subsequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given subsequence.
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsSubsequence(Iterable<? extends T> subsequence) {
    checkSubsequenceIsNotNull(subsequence);
    arrays.assertContainsSubsequence(info, array, toArray(subsequence));
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain the given subsequence in the correct order (possibly
   * with other values between them).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass
   * assertThat(elvesRings).doesNotContainSubsequence(nenya, vilya);
   *
   * // assertion will fail
   * assertThat(elvesRings).doesNotContainSubsequence(vilya, nenya);
   * assertThat(elvesRings).doesNotContainSubsequence(vilya, narya);</code></pre>
   *
   * @param subsequence the subsequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains the given subsequence.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> doesNotContainSubsequence(T... subsequence) {
    return doesNotContainSubsequenceForProxy(subsequence);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> doesNotContainSubsequenceForProxy(T[] subsequence) {
    arrays.assertDoesNotContainSubsequence(info, array, subsequence);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain the given subsequence in the correct order (possibly
   * with other values between them).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass
   * assertThat(elvesRings).doesNotContainSubsequence(newArrayList(nenya, vilya));
   *
   * // assertion will fail
   * assertThat(elvesRings).doesNotContainSubsequence(newArrayList(vilya, nenya));
   * assertThat(elvesRings).doesNotContainSubsequence(newArrayList(vilya, narya));</code></pre>
   *
   * @param subsequence the subsequence of objects to look for.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the given array is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains the given subsequence.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotContainSubsequence(Iterable<? extends T> subsequence) {
    checkSubsequenceIsNotNull(subsequence);
    arrays.assertDoesNotContainSubsequence(info, array, toArray(subsequence));
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains the given object at the given index.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass
   * assertThat(elvesRings).contains(vilya, atIndex(0))
   *                       .contains(nenya, atIndex(1))
   *                       .contains(narya, atIndex(2));
   *
   * // assertions will fail
   * assertThat(elvesRings).contains(vilya, atIndex(1));
   * assertThat(elvesRings).contains(nenya, atIndex(2));
   * assertThat(elvesRings).contains(narya, atIndex(0));</code></pre>
   *
   * @param value the object to look for.
   * @param index the index where the object should be stored in the actual AtomicReferenceArray.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null} or empty.
   * @throws NullPointerException if the given {@code Index} is {@code null}.
   * @throws IndexOutOfBoundsException if the value of the given {@code Index} is equal to or greater than the size of the actual
   *           AtomicReferenceArray.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain the given object at the given index.
   */
  @Override
  public AtomicReferenceArrayAssert<T> contains(T value, Index index) {
    arrays.assertContains(info, array, value, index);
    return myself;
  }

  /**
   * Verifies that all elements of the actual group are instances of given classes or interfaces.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Object&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Object[]{"", new StringBuilder()});
   *
   * // assertions will pass
   * assertThat(objects).hasOnlyElementsOfTypes(CharSequence.class);
   * assertThat(objects).hasOnlyElementsOfTypes(String.class, StringBuilder.class);
   *
   * // assertions will fail
   * assertThat(objects).hasOnlyElementsOfTypes(Number.class);
   * assertThat(objects).hasOnlyElementsOfTypes(String.class, Number.class);
   * assertThat(objects).hasOnlyElementsOfTypes(String.class);</code></pre>
   *
   * @param types the expected classes and interfaces
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual group is {@code null}.
   * @throws AssertionError if not all elements of the actual group are instances of one of the given types
   * @since 2.7.0 / 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasOnlyElementsOfTypes(Class<?>... types) {
    arrays.assertHasOnlyElementsOfTypes(info, array, types);
    return myself;
  }

  /**
   * Verifies that the actual elements are of the given types in the given order, there should be as many expected types as there are actual elements.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Object&gt; objects = new AtomicReferenceArray&lt;&gt;(new Object[] { 1, "a", 1.00 });
   *
   * // assertion succeeds
   * assertThat(objects).hasExactlyElementsOfTypes(Integer.class, String.class, String.class, Double.class);
   *
   * // assertions fail
   * // missing second String type
   * assertThat(objects).hasExactlyElementsOfTypes(Integer.class, String.class, Double.class);
   * // no Float type in actual
   * assertThat(objects).hasExactlyElementsOfTypes(Float.class, String.class, String.class, Double.class);
   * // correct types but wrong order
   * assertThat(objects).hasExactlyElementsOfTypes(String.class, Integer.class, String.class, Double.class);
   * // actual has more elements than the specified expected types
   * assertThat(objects).hasExactlyElementsOfTypes(String.class);</code></pre>
   *
   * @param expectedTypes the expected types
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given type array is {@code null}.
   * @throws AssertionError if actual is {@code null}.
   * @throws AssertionError if the actual elements types don't exactly match the given ones (in the given order).
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasExactlyElementsOfTypes(Class<?>... expectedTypes) {
    arrays.assertHasExactlyElementsOfTypes(info, array, expectedTypes);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain the given object at the given index.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass
   * assertThat(elvesRings).doesNotContain(vilya, atIndex(1))
   *                       .doesNotContain(nenya, atIndex(2))
   *                       .doesNotContain(narya, atIndex(0));
   *
   * // assertions will fail
   * assertThat(elvesRings).doesNotContain(vilya, atIndex(0));
   * assertThat(elvesRings).doesNotContain(nenya, atIndex(1));
   * assertThat(elvesRings).doesNotContain(narya, atIndex(2));</code></pre>
   *
   * @param value the object to look for.
   * @param index the index where the object should not be stored in the actual AtomicReferenceArray.
   * @return this assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws NullPointerException if the given {@code Index} is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains the given object at the given index.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotContain(T value, Index index) {
    arrays.assertDoesNotContain(info, array, value, index);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain the given values.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertion will pass
   * assertThat(abc).doesNotContain("d", "e");
   *
   * // assertions will fail
   * assertThat(abc).doesNotContain("a");
   * assertThat(abc).doesNotContain("a", "b", "c");
   * assertThat(abc).doesNotContain("a", "x");</code></pre>
   *
   * @param values the given values.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws IllegalArgumentException if the given argument is an empty array.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains any of the given values.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> doesNotContain(T... values) {
    return doesNotContainForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> doesNotContainForProxy(T[] values) {
    arrays.assertDoesNotContain(info, array, values);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain any elements of the given {@link Iterable} (i.e. none).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertion will pass
   * assertThat(actual).doesNotContainAnyElementsOf(newArrayList("d", "e"));
   *
   * // assertions will fail
   * assertThat(actual).doesNotContainAnyElementsOf(newArrayList("a", "b"));
   * assertThat(actual).doesNotContainAnyElementsOf(newArrayList("d", "e", "a"));</code></pre>
   *
   * @param iterable the {@link Iterable} whose elements must not be in the actual AtomicReferenceArray.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws IllegalArgumentException if the given argument is an empty iterable.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains some elements of the given {@link Iterable}.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotContainAnyElementsOf(Iterable<? extends T> iterable) {
    arrays.assertDoesNotContainAnyElementsOf(info, array, iterable);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain duplicates.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; aaa = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "a", "a"});
   *
   * // assertion will pass
   * assertThat(abc).doesNotHaveDuplicates();
   *
   * // assertion will fail
   * assertThat(aaa).doesNotHaveDuplicates();</code></pre>
   *
   * @return {@code this} assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains duplicates.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotHaveDuplicates() {
    arrays.assertDoesNotHaveDuplicates(info, array);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray starts with the given sequence of objects, without any other objects between them.
   * Similar to <code>{@link #containsSequence(Object...)}</code>, but it also verifies that the first element in the
   * sequence is also the first element of the actual AtomicReferenceArray.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertion will pass
   * assertThat(abc).startsWith("a", "b");
   *
   * // assertion will fail
   * assertThat(abc).startsWith("c");</code></pre>
   *
   * @param sequence the sequence of objects to look for.
   * @return this assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws IllegalArgumentException if the given argument is an empty array.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not start with the given sequence of objects.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> startsWith(T... sequence) {
    return startsWithForProxy(sequence);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> startsWithForProxy(T[] sequence) {
    arrays.assertStartsWith(info, array, sequence);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray ends with the given sequence of objects, without any other objects between them.
   * Similar to <code>{@link #containsSequence(Object...)}</code>, but it also verifies that the last element in the
   * sequence is also last element of the actual AtomicReferenceArray.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertion will pass
   * assertThat(abc).endsWith("b", "c");
   *
   * // assertion will fail
   * assertThat(abc).endsWith("a");</code></pre>
   *
   * @param first the first element of the end sequence of objects to look for.
   * @param sequence the rest of the end sequence of objects to look for.
   * @return this assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not end with the given sequence of objects.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> endsWith(T first, T... sequence) {
    return endsWithForProxy(first, sequence);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> endsWithForProxy(T first, T[] sequence) {
    arrays.assertEndsWith(info, array, first, sequence);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray ends with the given sequence of objects, without any other objects between them.
   * Similar to <code>{@link #containsSequence(Object...)}</code>, but it also verifies that the last element in the
   * sequence is also last element of the actual AtomicReferenceArray.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertions will pass
   * assertThat(abc).endsWith(new String[0])
   *                .endsWith(new String[] {"b", "c"});
   *
   * // assertion will fail
   * assertThat(abc).endsWith(new String[] {"a"});</code></pre>
   *
   * @param sequence the (possibly empty) sequence of objects to look for.
   * @return this assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not end with the given sequence of objects.
   */
  @Override
  public AtomicReferenceArrayAssert<T> endsWith(T[] sequence) {
    arrays.assertEndsWith(info, array, sequence);
    return myself;
  }

  /**
   * Verifies that all elements of actual are present in the given {@code Iterable}.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   * List&lt;Ring&gt; ringsOfPower = newArrayList(oneRing, vilya, nenya, narya, dwarfRing, manRing);
   *
   * // assertion will pass:
   * assertThat(elvesRings).isSubsetOf(ringsOfPower);
   *
   * // assertion will fail:
   * assertThat(elvesRings).isSubsetOf(newArrayList(nenya, narya));</code></pre>
   *
   * @param values the {@code Iterable} that should contain all actual elements.
   * @return this assertion object.
   * @throws AssertionError if the actual {@code Iterable} is {@code null}.
   * @throws NullPointerException if the given {@code Iterable} is {@code null}.
   * @throws AssertionError if the actual {@code Iterable} is not subset of set {@code Iterable}.
   */
  @Override
  public AtomicReferenceArrayAssert<T> isSubsetOf(Iterable<? extends T> values) {
    arrays.assertIsSubsetOf(info, array, values);
    return myself;
  }

  /**
   * Verifies that all elements of actual are present in the given values.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Ring&gt; elvesRings = new AtomicReferenceArray&lt;&gt;(new Ring[]{vilya, nenya, narya});
   *
   * // assertions will pass:
   * assertThat(elvesRings).isSubsetOf(vilya, nenya, narya)
   *                       .isSubsetOf(vilya, nenya, narya, dwarfRing);
   *
   * // assertions will fail:
   * assertThat(elvesRings).isSubsetOf(vilya, nenya);
   * assertThat(elvesRings).isSubsetOf(vilya, nenya, dwarfRing);</code></pre>
   *
   * @param values the values that should be used for checking the elements of actual.
   * @return this assertion object.
   * @throws AssertionError if the actual {@code Iterable} is {@code null}.
   * @throws AssertionError if the actual {@code Iterable} is not subset of the given values.
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> isSubsetOf(T... values) {
    return isSubsetOfForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> isSubsetOfForProxy(T[] values) {
    arrays.assertIsSubsetOf(info, array, Arrays.asList(values));
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains at least a null element.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abNull = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", null});
   *
   * // assertion will pass
   * assertThat(abNull).containsNull();
   *
   * // assertion will fail
   * assertThat(abc).containsNull();</code></pre>
   *
   * @return {@code this} assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain a null element.
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsNull() {
    arrays.assertContainsNull(info, array);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray does not contain null elements.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abNull = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", null});
   *
   * // assertion will pass
   * assertThat(abc).doesNotContainNull();
   *
   * // assertion will fail
   * assertThat(abNull).doesNotContainNull();</code></pre>
   *
   * @return {@code this} assertion object.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray contains a null element.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotContainNull() {
    arrays.assertDoesNotContainNull(info, array);
    return myself;
  }

  /**
   * Verifies that each element value satisfies the given condition
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * Condition&lt;String&gt; singleCharacterString
   *      = new Condition&lt;&gt;(s -&gt; s.length() == 1, "single character String");
   *
   * // assertion will pass
   * assertThat(abc).are(singleCharacterString);
   *
   * // assertion will fail
   * assertThat(abcc).are(singleCharacterString);</code></pre>
   *
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if one or more elements don't satisfy the given condition.
   */
  @Override
  public AtomicReferenceArrayAssert<T> are(Condition<? super T> condition) {
    arrays.assertAre(info, array, condition);
    return myself;
  }

  /**
   * Verifies that each element value does not satisfy the given condition
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * Condition&lt;String&gt; moreThanOneCharacter =
   *     = new Condition&lt;&gt;(s -&gt; s.length() &gt; 1, "more than one character");
   *
   * // assertion will pass
   * assertThat(abc).areNot(moreThanOneCharacter);
   *
   * // assertion will fail
   * assertThat(abcc).areNot(moreThanOneCharacter);</code></pre>
   *
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if one or more elements satisfy the given condition.
   */
  @Override
  public AtomicReferenceArrayAssert<T> areNot(Condition<? super T> condition) {
    arrays.assertAreNot(info, array, condition);
    return myself;
  }

  /**
   * Verifies that all elements satisfy the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * Condition&lt;String&gt; onlyOneCharacter =
   *     = new Condition&lt;&gt;(s -&gt; s.length() == 1, "only one character");
   *
   * // assertion will pass
   * assertThat(abc).have(onlyOneCharacter);
   *
   * // assertion will fail
   * assertThat(abcc).have(onlyOneCharacter);</code></pre>
   *
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if one or more elements do not satisfy the given condition.
   */
  @Override
  public AtomicReferenceArrayAssert<T> have(Condition<? super T> condition) {
    arrays.assertHave(info, array, condition);
    return myself;
  }

  /**
   * Verifies that all elements don't satisfy the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * Condition&lt;String&gt; moreThanOneCharacter =
   *     = new Condition&lt;&gt;(s -&gt; s.length() &gt; 1, "more than one character");
   *
   * // assertion will pass
   * assertThat(abc).doNotHave(moreThanOneCharacter);
   *
   * // assertion will fail
   * assertThat(abcc).doNotHave(moreThanOneCharacter);</code></pre>
   *
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if one or more elements satisfy the given condition.
   */
  @Override
  public AtomicReferenceArrayAssert<T> doNotHave(Condition<? super T> condition) {
    arrays.assertDoNotHave(info, array, condition);
    return myself;
  }

  /**
   * Verifies that there are <b>at least</b> <i>n</i> elements in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Integer&gt; oneTwoThree = new AtomicReferenceArray&lt;&gt;(new Integer[]{1, 2, 3});
   *
   * Condition&lt;Integer&gt; oddNumber = new Condition&lt;&gt;(value % 2 == 1, "odd number");
   *
   * // assertion will pass
   * oneTwoThree.areAtLeast(2, oddNumber);
   *
   * // assertion will fail
   * oneTwoThree.areAtLeast(3, oddNumber);</code></pre>
   *
   * @param times the minimum number of times the condition should be verified.
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element can not be cast to T.
   * @throws AssertionError if the number of elements satisfying the given condition is &lt; n.
   */
  @Override
  public AtomicReferenceArrayAssert<T> areAtLeast(int times, Condition<? super T> condition) {
    arrays.assertAreAtLeast(info, array, times, condition);
    return myself;
  }

  /**
   * Verifies that there is <b>at least <i>one</i></b> element in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * This method is an alias for {@code areAtLeast(1, condition)}.
   * <p>
   * Example:
   * <pre><code class='java'> // jedi is a Condition&lt;String&gt;
   * AtomicReferenceArray&lt;String&gt; rebels = new AtomicReferenceArray&lt;&gt;(new String[]{"Luke", "Solo", "Leia"});
   *
   * assertThat(rebels).areAtLeastOne(jedi);</code></pre>
   *
   * @see #haveAtLeast(int, Condition)
   */
  @Override
  public AtomicReferenceArrayAssert<T> areAtLeastOne(Condition<? super T> condition) {
    areAtLeast(1, condition);
    return myself;
  }

  /**
   * Verifies that there are <b>at most</b> <i>n</i> elements in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Integer&gt; oneTwoThree = new AtomicReferenceArray&lt;&gt;(new Integer[]{1, 2, 3});
   *
   * Condition&lt;Integer&gt; oddNumber = new Condition&lt;&gt;(value % 2 == 1, "odd number");
   *
   * // assertions will pass
   * oneTwoThree.areAtMost(2, oddNumber);
   * oneTwoThree.areAtMost(3, oddNumber);
   *
   * // assertion will fail
   * oneTwoThree.areAtMost(1, oddNumber);</code></pre>
   *
   * @param times the number of times the condition should be at most verified.
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if the number of elements satisfying the given condition is &gt; n.
   */
  @Override
  public AtomicReferenceArrayAssert<T> areAtMost(int times, Condition<? super T> condition) {
    arrays.assertAreAtMost(info, array, times, condition);
    return myself;
  }

  /**
   * Verifies that there are <b>exactly</b> <i>n</i> elements in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Integer&gt; oneTwoThree = new AtomicReferenceArray&lt;&gt;(new Integer[]{1, 2, 3});
   *
   * Condition&lt;Integer&gt; oddNumber = new Condition&lt;&gt;(value % 2 == 1, "odd number");
   *
   * // assertion will pass
   * oneTwoThree.areExactly(2, oddNumber);
   *
   * // assertions will fail
   * oneTwoThree.areExactly(1, oddNumber);
   * oneTwoThree.areExactly(3, oddNumber);</code></pre>
   *
   * @param times the exact number of times the condition should be verified.
   * @param condition the given condition.
   * @return {@code this} object.
   * @throws NullPointerException if the given condition is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if the number of elements satisfying the given condition is &ne; n.
   */
  @Override
  public AtomicReferenceArrayAssert<T> areExactly(int times, Condition<? super T> condition) {
    arrays.assertAreExactly(info, array, times, condition);
    return myself;
  }

  /**
   * Verifies that there is <b>at least <i>one</i></b> element in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * This method is an alias for {@code haveAtLeast(1, condition)}.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;BasketBallPlayer&gt; bullsPlayers = new AtomicReferenceArray&lt;&gt;(new BasketBallPlayer[]{butler, rose});
   *
   * // potentialMvp is a Condition&lt;BasketBallPlayer&gt;
   * assertThat(bullsPlayers).haveAtLeastOne(potentialMvp);</code></pre>
   *
   * @see #haveAtLeast(int, Condition)
   */
  @Override
  public AtomicReferenceArrayAssert<T> haveAtLeastOne(Condition<? super T> condition) {
    return haveAtLeast(1, condition);
  }

  /**
   * Verifies that there are <b>at least <i>n</i></b> elements in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Integer&gt; oneTwoThree = new AtomicReferenceArray&lt;&gt;(new Integer[]{1, 2, 3});
   *
   * Condition&lt;Integer&gt; oddNumber = new Condition&lt;&gt;(value % 2 == 1, "odd number");
   *
   * // assertion will pass
   * oneTwoThree.haveAtLeast(2, oddNumber);
   *
   * // assertion will fail
   * oneTwoThree.haveAtLeast(3, oddNumber);</code></pre>
   *
   * This method is an alias for {@link #areAtLeast(int, Condition)}.
   */
  @Override
  public AtomicReferenceArrayAssert<T> haveAtLeast(int times, Condition<? super T> condition) {
    arrays.assertHaveAtLeast(info, array, times, condition);
    return myself;
  }

  /**
   * Verifies that there are <b>at most</b> <i>n</i> elements in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Integer&gt; oneTwoThree = new AtomicReferenceArray&lt;&gt;(new Integer[]{1, 2, 3});
   *
   * Condition&lt;Integer&gt; oddNumber = new Condition&lt;&gt;(value % 2 == 1, "odd number");
   *
   * // assertions will pass
   * oneTwoThree.haveAtMost(2, oddNumber);
   * oneTwoThree.haveAtMost(3, oddNumber);
   *
   * // assertion will fail
   * oneTwoThree.haveAtMost(1, oddNumber);</code></pre>
   *
   * This method is an alias {@link #areAtMost(int, Condition)}.
   */
  @Override
  public AtomicReferenceArrayAssert<T> haveAtMost(int times, Condition<? super T> condition) {
    arrays.assertHaveAtMost(info, array, times, condition);
    return myself;
  }

  /**
   * Verifies that there are <b>exactly</b> <i>n</i> elements in the actual AtomicReferenceArray satisfying the given condition.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;Integer&gt; oneTwoThree = new AtomicReferenceArray&lt;&gt;(new Integer[]{1, 2, 3});
   *
   * Condition&lt;Integer&gt; oddNumber = new Condition&lt;&gt;(value % 2 == 1, "odd number");
   *
   * // assertion will pass
   * oneTwoThree.haveExactly(2, oddNumber);
   *
   * // assertions will fail
   * oneTwoThree.haveExactly(1, oddNumber);
   * oneTwoThree.haveExactly(3, oddNumber);</code></pre>
   *
   * This method is an alias {@link #areExactly(int, Condition)}.
   */
  @Override
  public AtomicReferenceArrayAssert<T> haveExactly(int times, Condition<? super T> condition) {
    arrays.assertHaveExactly(info, array, times, condition);
    return myself;
  }

  /**
   * Verifies that at least one element in the actual AtomicReferenceArray has the specified type (matching
   * includes subclasses of the given type).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Number&gt; numbers = new AtomicReferenceArray&lt;&gt;(new Number[]{ 2, 6L, 8.0 });
   *
   * // successful assertion:
   * assertThat(numbers).hasAtLeastOneElementOfType(Long.class);
   *
   * // assertion failure:
   * assertThat(numbers).hasAtLeastOneElementOfType(Float.class);</code></pre>
   *
   * @param expectedType the expected type.
   * @return this assertion object.
   * @throws NullPointerException if the given type is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not have any elements of the given type.
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasAtLeastOneElementOfType(Class<?> expectedType) {
    arrays.assertHasAtLeastOneElementOfType(info, array, expectedType);
    return myself;
  }

  /**
   * Verifies that all the elements in the actual AtomicReferenceArray belong to the specified type (matching includes
   * subclasses of the given type).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Number&gt; numbers = new AtomicReferenceArray&lt;&gt;(new Number[]{ 2, 6, 8 });
   *
   * // successful assertion:
   * assertThat(numbers).hasOnlyElementsOfType(Integer.class);
   *
   * // assertion failure:
   * assertThat(numbers).hasOnlyElementsOfType(Long.class);</code></pre>
   *
   * @param expectedType the expected type.
   * @return this assertion object.
   * @throws NullPointerException if the given type is {@code null}.
   * @throws AssertionError if one element is not of the expected type.
   */
  @Override
  public AtomicReferenceArrayAssert<T> hasOnlyElementsOfType(Class<?> expectedType) {
    arrays.assertHasOnlyElementsOfType(info, array, expectedType);
    return myself;
  }

  /**
   * Verifies that all the elements in the actual AtomicReferenceArray do not belong to the specified types (including subclasses).
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;Number&gt; numbers = new AtomicReferenceArray&lt;&gt;(new Number[]{ 2, 6, 8.0 });
   *
   * // successful assertion:
   * assertThat(numbers).doesNotHaveAnyElementsOfTypes(Long.class, Float.class);
   *
   * // assertion failure:
   * assertThat(numbers).doesNotHaveAnyElementsOfTypes(Long.class, Integer.class);</code></pre>
   *
   * @param unexpectedTypes the not expected types.
   * @return this assertion object.
   * @throws NullPointerException if the given types is {@code null}.
   * @throws AssertionError if one element's type matches the given types.
   * @since 2.9.0 / 3.9.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> doesNotHaveAnyElementsOfTypes(Class<?>... unexpectedTypes) {
    arrays.assertDoesNotHaveAnyElementsOfTypes(info, array, unexpectedTypes);
    return myself;
  }

  /** {@inheritDoc} */
  @Override
  public AtomicReferenceArrayAssert<T> isSorted() {
    arrays.assertIsSorted(info, array);
    return myself;
  }

  /** {@inheritDoc} */
  @Override
  public AtomicReferenceArrayAssert<T> isSortedAccordingTo(Comparator<? super T> comparator) {
    arrays.assertIsSortedAccordingToComparator(info, array, comparator);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains all the elements of given {@code Iterable}, in any order.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertion will pass
   * assertThat(abc).containsAll(Arrays.asList("b", "c"));
   *
   * // assertions will fail
   * assertThat(abc).containsAll(Arrays.asList("d"));
   * assertThat(abc).containsAll(Arrays.asList("a", "b", "c", "d"));</code></pre>
   *
   * @param iterable the given {@code Iterable} we will get elements from.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the given argument is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray is {@code null}.
   * @throws AssertionError if the actual AtomicReferenceArray does not contain all the elements of given {@code Iterable}.
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsAll(Iterable<? extends T> iterable) {
    arrays.assertContainsAll(info, array, iterable);
    return myself;
  }

  /**
   * Use given custom comparator instead of relying on actual element type <code>equals</code> method to compare AtomicReferenceArray
   * elements for incoming assertion checks.
   * <p>
   * Custom comparator is bound to assertion instance, meaning that if a new assertion is created, it will use default
   * comparison strategy.
   * <p>
   * Examples :
   * <pre><code class='java'> // compares invoices by payee
   * assertThat(invoiceArray).usingComparator(invoicePayeeComparator).isEqualTo(expectedInvoiceArray).
   *
   * // compares invoices by date, doesNotHaveDuplicates and contains both use the given invoice date comparator
   * assertThat(invoiceArray).usingComparator(invoiceDateComparator).doesNotHaveDuplicates().contains(may2010Invoice)
   *
   * // as assertThat(invoiceArray) creates a new assertion, it falls back to standard comparison strategy
   * // based on Invoice's equal method to compare invoiceArray elements to lowestInvoice.
   * assertThat(invoiceArray).contains(lowestInvoice).
   *
   * // standard comparison : the fellowshipOfTheRing includes Gandalf but not Sauron (believe me) ...
   * assertThat(fellowshipOfTheRing).contains(gandalf)
   *                                .doesNotContain(sauron);
   *
   * // ... but if we compare only races, Sauron is in fellowshipOfTheRing because he's a Maia like Gandalf.
   * assertThat(fellowshipOfTheRing).usingElementComparator(raceComparator)
   *                                .contains(sauron);</code></pre>
   *
   * @param elementComparator the comparator to use for incoming assertion checks.
   * @throws NullPointerException if the given comparator is {@code null}.
   * @return {@code this} assertion object.
   */
  @Override
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> usingElementComparator(Comparator<? super T> elementComparator) {
    this.arrays = new ObjectArrays(new ComparatorBasedComparisonStrategy(elementComparator));
    objects = new Objects(new AtomicReferenceArrayElementComparisonStrategy<>(elementComparator));
    return myself;
  }

  /** {@inheritDoc} */
  @Override
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> usingDefaultElementComparator() {
    this.arrays = ObjectArrays.instance();
    return myself;
  }

  /**
   * Allows to set a specific comparator for the given type of elements or their fields.
   * <p>
   * Example:
   * <pre><code class='java'> // assertion will pass
   * assertThat(new AtomicReferenceArray&lt;&gt;(new Object[] { "some", new BigDecimal("4.2") }))
   *       .usingComparatorForType(BIG_DECIMAL_COMPARATOR, BigDecimal.class)
   *       .contains(new BigDecimal("4.20"));
   * </code></pre>
   *
   * @param <C> the type to compare.
   * @param comparator the {@link java.util.Comparator} to use
   * @param type the {@link java.lang.Class} of the type of the element or element fields the comparator should be used for
   * @return {@code this} assertions object
   * @since 2.9.0 / 3.9.0
   */
  @CheckReturnValue
  public <C> AtomicReferenceArrayAssert<T> usingComparatorForType(Comparator<C> comparator, Class<C> type) {
    if (arrays.getComparator() == null) {
      usingElementComparator(new ExtendedByTypesComparator(getComparatorsByType()));
    }

    getComparatorsForElementPropertyOrFieldTypes().registerComparator(type, comparator);
    getComparatorsByType().registerComparator(type, comparator);

    return myself;
  }

  /**
   * Enable using a recursive field by field comparison strategy similar to {@link #usingRecursiveComparison()} but contrary to the latter <b>you can chain any iterable assertions after this method</b> (this is why this method exists).
   * <p>
   * This method uses the default {@link RecursiveComparisonConfiguration}, if you need to customize it use {@link #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)} instead.
   * <p>
   * <b>Breaking change:</b> since 3.20.0 the comparison won't use any comparators set with:
   * <ul>
   *   <li>{@link #usingComparatorForType(Comparator, Class)}</li>
   * </ul>
   * <p>
   * These features (and many more) are provided through {@link #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)} with a customized {@link RecursiveComparisonConfiguration} where there methods are called:
   * <ul>
   *   <li>{@link RecursiveComparisonConfiguration#registerComparatorForType(Comparator, Class) registerComparatorForType(Comparator, Class)} / {@link RecursiveComparisonConfiguration.Builder#withComparatorForType(Comparator, Class) withComparatorForType(Comparator, Class)} (using {@link RecursiveComparisonConfiguration.Builder})</li>
   *   <li>{@link RecursiveComparisonConfiguration#registerEqualsForType(java.util.function.BiPredicate, Class) registerEqualsForType(BiPredicate, Class)} / {@link RecursiveComparisonConfiguration.Builder#withComparatorForType(Comparator, Class) withComparatorForType(Comparator, Class)} (using {@link RecursiveComparisonConfiguration.Builder})</li>
   *   <li>{@link RecursiveComparisonConfiguration#registerComparatorForFields(Comparator, String...) registerComparatorForFields(Comparator comparator, String... fields)} / {@link RecursiveComparisonConfiguration.Builder#withComparatorForFields(Comparator, String...) withComparatorForField(Comparator comparator, String... fields)} (using {@link RecursiveComparisonConfiguration.Builder})</li>
   * </ul>
   * <p>
   * There are differences between this approach and {@link #usingRecursiveComparison()}:
   * <ul>
   *   <li>contrary to {@link RecursiveComparisonAssert}, you can chain any iterable assertions after this method.</li>
   *   <li>no comparators registered with {@link AbstractIterableAssert#usingComparatorForType(Comparator, Class)} will be used, you need to register them in the configuration object.</li>
   *   <li>the assertion errors won't be as detailed as {@link RecursiveComparisonAssert#isEqualTo(Object)} which shows the field differences.</li>
   * </ul>
   * <p>
   * This last point makes sense, take the {@link #contains(Object...)} assertion, it would not be relevant to report the differences of all the iterable's elements differing from the values to look for.
   * <p>
   * Example:
   * <pre><code class='java'> public class Person {
   *   String name;
   *   boolean hasPhd;
   * }
   *
   * public class Doctor {
   *  String name;
   *  boolean hasPhd;
   * }
   *
   * Doctor drSheldon = new Doctor("Sheldon Cooper", true);
   * Doctor drLeonard = new Doctor("Leonard Hofstadter", true);
   * Doctor drRaj = new Doctor("Raj Koothrappali", true);
   *
   * Person sheldon = new Person("Sheldon Cooper", true);
   * Person leonard = new Person("Leonard Hofstadter", true);
   * Person raj = new Person("Raj Koothrappali", true);
   * Person howard = new Person("Howard Wolowitz", true);
   *
   * AtomicReferenceArray&lt;Doctor&gt; doctors = new AtomicReferenceArray&lt;&gt;(array(drSheldon, drLeonard, drRaj));
   * AtomicReferenceArray&lt;Person&gt; persons = new AtomicReferenceArray&lt;&gt;(array(sheldon, leonard, raj));
   *
   * // assertion succeeds as both lists contains equivalent items in order.
   * assertThat(doctors).usingRecursiveFieldByFieldElementComparator()
   *                    .contains(sheldon);
   *
   * // assertion fails because leonard names are different.
   * leonard.setName("Leonard Ofstater");
   * assertThat(doctors).usingRecursiveFieldByFieldElementComparator()
   *                    .contains(leonard);
   *
   * // assertion fails because howard is missing and leonard is not expected.
   * people = list(howard, sheldon, raj)
   * assertThat(doctors).usingRecursiveFieldByFieldElementComparator()
   *                    .contains(howard);</code></pre>
   * <p>
   * Another point worth mentioning: <b>elements order does matter if the expected iterable is ordered</b>, for example comparing a {@code Set<Person>} to a {@code List<Person>} fails as {@code List} is ordered and {@code Set} is not.<br>
   * The ordering can be ignored by calling {@link RecursiveComparisonAssert#ignoringCollectionOrder ignoringCollectionOrder} allowing ordered/unordered iterable comparison, note that {@link RecursiveComparisonAssert#ignoringCollectionOrder ignoringCollectionOrder} is applied recursively on any nested iterable fields, if this behavior is too generic,
   * use the more fine-grained {@link RecursiveComparisonAssert#ignoringCollectionOrderInFields(String...)
   * ignoringCollectionOrderInFields} or
   * {@link RecursiveComparisonAssert#ignoringCollectionOrderInFieldsMatchingRegexes(String...) ignoringCollectionOrderInFieldsMatchingRegexes}.
   *
   * @return {@code this} assertion object.
   * @since 2.7.0 / 3.7.0 - breaking change in 3.20.0
   * @see RecursiveComparisonConfiguration
   * @see #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> usingRecursiveFieldByFieldElementComparator() {
    return usingRecursiveFieldByFieldElementComparator(new RecursiveComparisonConfiguration(info.representation()));
  }

  /**
   * Enable using a recursive field by field comparison strategy similar to {@link #usingRecursiveComparison()} but contrary to the latter <b>you can chain any iterable assertions after this method</b> (this is why this method exists).
   * <p>
   * The given {@link RecursiveComparisonConfiguration} is used to tweak the comparison behavior, for example by {@link RecursiveComparisonConfiguration#ignoreCollectionOrder(boolean) ignoring collection order}.
   * <p>
   * <b>Warning:</b> the comparison won't use any comparators set with:
   * <ul>
   *   <li>{@link #usingComparatorForType(Comparator, Class)}</li>
   * </ul>
   * <p>
   * These features (and many more) are provided through {@link RecursiveComparisonConfiguration} with:
   * <ul>
   *   <li>{@link RecursiveComparisonConfiguration#registerComparatorForType(Comparator, Class) registerComparatorForType(Comparator, Class)} / {@link RecursiveComparisonConfiguration.Builder#withComparatorForType(Comparator, Class) withComparatorForType(Comparator, Class)} (using {@link RecursiveComparisonConfiguration.Builder})</li>
   *   <li>{@link RecursiveComparisonConfiguration#registerEqualsForType(java.util.function.BiPredicate, Class) registerEqualsForType(BiPredicate, Class)} / {@link RecursiveComparisonConfiguration.Builder#withComparatorForType(Comparator, Class) withComparatorForType(Comparator, Class)} (using {@link RecursiveComparisonConfiguration.Builder})</li>
   *   <li>{@link RecursiveComparisonConfiguration#registerComparatorForFields(Comparator, String...) registerComparatorForFields(Comparator comparator, String... fields)} / {@link RecursiveComparisonConfiguration.Builder#withComparatorForFields(Comparator, String...) withComparatorForField(Comparator comparator, String... fields)} (using {@link RecursiveComparisonConfiguration.Builder})</li>
   * </ul>
   * <p>
   * RecursiveComparisonConfiguration exposes a {@link RecursiveComparisonConfiguration.Builder builder} to ease setting the comparison behaviour,
   * call {@link RecursiveComparisonConfiguration#builder() RecursiveComparisonConfiguration.builder()} to start building your configuration.
   * <p>
   * There are differences between this approach and {@link #usingRecursiveComparison()}:
   * <ul>
   *   <li>contrary to {@link RecursiveComparisonAssert}, you can chain any iterable assertions after this method.</li>
   *   <li>no comparators registered with {@link AbstractIterableAssert#usingComparatorForType(Comparator, Class)} will be used, you need to register them in the configuration object.</li>
   *   <li>the assertion errors won't be as detailed as {@link RecursiveComparisonAssert#isEqualTo(Object)} which shows the field differences.</li>
   * </ul>
   * <p>
   * This last point makes sense, take the {@link #contains(Object...)} assertion, it would not be relevant to report the differences of all the iterable's elements differing from the values to look for.
   * <p>
   * Example:
   * <pre><code class='java'> public class Person {
   *   String name;
   *   boolean hasPhd;
   * }
   *
   * public class Doctor {
   *  String name;
   *  boolean hasPhd;
   * }
   *
   * Doctor drSheldon = new Doctor("Sheldon Cooper", true);
   * Doctor drLeonard = new Doctor("Leonard Hofstadter", true);
   * Doctor drRaj = new Doctor("Raj Koothrappali", true);
   *
   * Person sheldon = new Person("Sheldon Cooper", false);
   * Person leonard = new Person("Leonard Hofstadter", false);
   * Person raj = new Person("Raj Koothrappali", false);
   * Person howard = new Person("Howard Wolowitz", false);
   *
   * AtomicReferenceArray&lt;Doctor&gt; doctors = new AtomicReferenceArray&lt;&gt;(array(drSheldon, drLeonard, drRaj));
   * AtomicReferenceArray&lt;Person&gt; persons = new AtomicReferenceArray&lt;&gt;(array(sheldon, leonard, raj));
   *
   * RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration.builder()
   *                                                                                  .withIgnoredFields​("hasPhd");
   *
   * // assertion succeeds as both lists contains equivalent items in order.
   * assertThat(doctors).usingRecursiveFieldByFieldElementComparator(configuration)
   *                    .contains(sheldon);
   *
   * // assertion fails because leonard names are different.
   * leonard.setName("Leonard Ofstater");
   * assertThat(doctors).usingRecursiveFieldByFieldElementComparator(configuration)
   *                    .contains(leonard);
   *
   * // assertion fails because howard is missing and leonard is not expected.
   * people = list(howard, sheldon, raj)
   * assertThat(doctors).usingRecursiveFieldByFieldElementComparator(configuration)
   *                    .contains(howard);</code></pre>
   *
   * A detailed documentation for the recursive comparison is available here: <a href="https://assertj.github.io/doc/#assertj-core-recursive-comparison">https://assertj.github.io/doc/#assertj-core-recursive-comparison</a>.
   * <p>
   * A point worth mentioning: <b>elements order does matter if the expected iterable is ordered</b>, for example comparing a {@code Set<Person>} to a {@code List<Person>} fails as {@code List} is ordered and {@code Set} is not.<br>
   * The ordering can be ignored by calling {@link RecursiveComparisonAssert#ignoringCollectionOrder ignoringCollectionOrder} allowing ordered/unordered iterable comparison, note that {@link RecursiveComparisonAssert#ignoringCollectionOrder ignoringCollectionOrder} is applied recursively on any nested iterable fields, if this behavior is too generic,
   * use the more fine-grained {@link RecursiveComparisonAssert#ignoringCollectionOrderInFields(String...) ignoringCollectionOrderInFields} or
   * {@link RecursiveComparisonAssert#ignoringCollectionOrderInFieldsMatchingRegexes(String...) ignoringCollectionOrderInFieldsMatchingRegexes}.
   *
   * @param configuration the recursive comparison configuration.
   *
   * @return {@code this} assertion object.
   * @since 3.20.0
   * @see RecursiveComparisonConfiguration
   */
  public AtomicReferenceArrayAssert<T> usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration configuration) {
    return usingElementComparator(new ConfigurableRecursiveFieldByFieldComparator(configuration));
  }

  /**
   * The assertions chained after this method will use a recursive field by field comparison on the given fields (including
   * inherited fields) instead of relying on the element <code>equals</code> method.
   * This is handy when the element <code>equals</code> method is not overridden or implemented as you expect.
   * <p>
   * Nested fields are supported and are expressed like: {@code name.first}
   * <p>
   * The comparison is <b>recursive</b>: elements are compared field by field, if a field type has fields they are also compared
   * field by field (and so on).
   * <p>
   * Example:
   * <pre><code class='java'> Player derrickRose = new Player(new Name("Derrick", "Rose"), "Chicago Bulls");
   * derrickRose.nickname = new Name("Crazy", "Dunks");
   *
   * Player jalenRose = new Player(new Name("Jalen", "Rose"), "Chicago Bulls");
   * jalenRose.nickname = new Name("Crazy", "Defense");
   *
   * // assertion succeeds as all compared fields match
   * assertThat(atomicArray(derrickRose)).usingRecursiveFieldByFieldElementComparatorOnFields("name.last", "team", "nickname.first")
   *                                     .contains(jalenRose);
   *
   * // assertion fails, name.first values differ
   * assertThat(atomicArray(derrickRose)).usingRecursiveFieldByFieldElementComparatorOnFields("name")
   *                                     .contains(jalenRose);</code></pre>
   * <p>
   * This method is actually a shortcut of {@link #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)}
   * with a configuration comparing only the given fields, the previous example can be written as:
   * <pre><code class='java'> RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration.builder()
   *                                                                                  .withComparedFields("name.last", "team", "nickname.first")
   *                                                                                  .build();
   *
   * assertThat(atomicArray(derrickRose)).usingRecursiveFieldByFieldElementComparator(configuration)
   *                                     .contains(jalenRose);</code></pre>
   * The recursive comparison is documented here: <a href="https://assertj.github.io/doc/#assertj-core-recursive-comparison">https://assertj.github.io/doc/#assertj-core-recursive-comparison</a>
   * <p>
   * @param fields the field names to exclude in the elements comparison.
   * @return {@code this} assertion object.
   * @since 3.20.0
   * @see #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)
   * @see <a href="https://assertj.github.io/doc/#assertj-core-recursive-comparison">https://assertj.github.io/doc/#assertj-core-recursive-comparison</a>
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> usingRecursiveFieldByFieldElementComparatorOnFields(String... fields) {
    RecursiveComparisonConfiguration recursiveComparisonConfiguration = RecursiveComparisonConfiguration.builder()
                                                                                                        .withComparedFields(fields)
                                                                                                        .build();
    return usingRecursiveFieldByFieldElementComparator(recursiveComparisonConfiguration);
  }

  /**
   * The assertions chained after this method will use a recursive field by field comparison on all fields (including inherited
   * fields) <b>except</b> the given ones instead of relying on the element <code>equals</code> method.
   * This is handy when the element <code>equals</code> method is not overridden or implemented as you expect.
   * <p>
   * Nested fields are supported and are expressed like: {@code name.first}
   * <p>
   * The comparison is <b>recursive</b>: elements are compared field by field, if a field type has fields they are also compared
   * field by field (and so on).
   * <p>
   * Example:
   * <pre><code class='java'> Player derrickRose = new Player(new Name("Derrick", "Rose"), "Chicago Bulls");
   * derrickRose.nickname = new Name("Crazy", "Dunks");
   *
   * Player jalenRose = new Player(new Name("Jalen", "Rose"), "Chicago Bulls");
   * jalenRose.nickname = new Name("Crazy", "Defense");
   *
   * // assertion succeeds
   * assertThat(atomicArray(derrickRose)).usingRecursiveFieldByFieldElementComparatorIgnoringFields("name.first", "nickname.last")
   *                                     .contains(jalenRose);
   *
   * // assertion fails, names are ignored but nicknames are not and nickname.last values differ
   * assertThat(atomicArray(derrickRose)).usingRecursiveFieldByFieldElementComparatorIgnoringFields("name")
   *                                     .contains(jalenRose);</code></pre>
   * <p>
   * This method is actually a shortcut of {@link #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)}
   * with a configuration ignoring the given fields, the previous example can be written as:
   * <pre><code class='java'> RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration.builder()
   *                                                                                  .withIgnoredFields("name.first", "nickname.last")
   *                                                                                  .build();
   *
   * assertThat(atomicArray(derrickRose)).usingRecursiveFieldByFieldElementComparator(configuration)
   *                                     .contains(jalenRose);</code></pre>
   * The recursive comparison is documented here: <a href="https://assertj.github.io/doc/#assertj-core-recursive-comparison">https://assertj.github.io/doc/#assertj-core-recursive-comparison</a>
   * <p>
   * @param fields the field names to exclude in the elements comparison.
   * @return {@code this} assertion object.
   * @since 3.20.0
   * @see #usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration)
   * @see <a href="https://assertj.github.io/doc/#assertj-core-recursive-comparison">https://assertj.github.io/doc/#assertj-core-recursive-comparison</a>
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> usingRecursiveFieldByFieldElementComparatorIgnoringFields(String... fields) {
    RecursiveComparisonConfiguration recursiveComparisonConfiguration = RecursiveComparisonConfiguration.builder()
                                                                                                        .withIgnoredFields(fields)
                                                                                                        .build();
    return usingRecursiveFieldByFieldElementComparator(recursiveComparisonConfiguration);
  }

  /**
   * Extract the values of given field or property from the array's elements under test into a new array, this new array
   * becoming the array under test.
   * <p>
   * It allows you to test a field/property of the array's elements instead of testing the elements themselves, which can
   * be much less work !
   * <p>
   * Let's take an example to make things clearer :
   * <pre><code class='java'> // Build a array of TolkienCharacter, a TolkienCharacter has a name (String) and a Race (a class)
   * // they can be public field or properties, both works when extracting their values.
   * AtomicReferenceArray&lt;TolkienCharacter&gt; fellowshipOfTheRing = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[]{
   *   new TolkienCharacter(&quot;Frodo&quot;, 33, HOBBIT),
   *   new TolkienCharacter(&quot;Sam&quot;, 38, HOBBIT),
   *   new TolkienCharacter(&quot;Gandalf&quot;, 2020, MAIA),
   *   new TolkienCharacter(&quot;Legolas&quot;, 1000, ELF),
   *   new TolkienCharacter(&quot;Pippin&quot;, 28, HOBBIT),
   *   new TolkienCharacter(&quot;Gimli&quot;, 139, DWARF),
   *   new TolkienCharacter(&quot;Aragorn&quot;, 87, MAN,
   *   new TolkienCharacter(&quot;Boromir&quot;, 37, MAN)
   * };
   *
   * // let's verify the names of TolkienCharacter in fellowshipOfTheRing :
   *
   * assertThat(fellowshipOfTheRing).extracting(&quot;name&quot;)
   *           .contains(&quot;Boromir&quot;, &quot;Gandalf&quot;, &quot;Frodo&quot;)
   *           .doesNotContain(&quot;Sauron&quot;, &quot;Elrond&quot;);
   *
   * // you can also extract nested field/property like the name of Race :
   *
   * assertThat(fellowshipOfTheRing).extracting(&quot;race.name&quot;)
   *                                .contains(&quot;Hobbit&quot;, &quot;Elf&quot;)
   *                                .doesNotContain(&quot;Orc&quot;);</code></pre>
   *
   * A property with the given name is looked for first, if it does not exist then a field with the given name
   * is looked for.
   * <p>
   * Note that the order of extracted field/property values is consistent with the array order.
   *
   * @param fieldOrProperty the field/property to extract from the array under test
   * @return a new assertion object whose object under test is the array of extracted field/property values.
   * @throws IntrospectionError if no field or property exists with the given name
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public ObjectArrayAssert<Object> extracting(String fieldOrProperty) {
    Object[] values = FieldsOrPropertiesExtractor.extract(array, byName(fieldOrProperty));
    String extractedDescription = extractedDescriptionOf(fieldOrProperty);
    String description = mostRelevantDescription(info.description(), extractedDescription);
    return new ObjectArrayAssert<>(values).as(description);
  }

  /**
   * Extract the values of given field or property from the array's elements under test into a new array, this new array
   * becoming the array under test with type.
   * <p>
   * It allows you to test a field/property of the array's elements instead of testing the elements themselves, which can
   * be much less work !
   * <p>
   * Let's take an example to make things clearer :
   * <pre><code class='java'> // Build an array of TolkienCharacter, a TolkienCharacter has a name (String) and a Race (a class)
   * // they can be public field or properties, both works when extracting their values.
   * AtomicReferenceArray&lt;TolkienCharacter&gt; fellowshipOfTheRing = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[]{
   *   new TolkienCharacter(&quot;Frodo&quot;, 33, HOBBIT),
   *   new TolkienCharacter(&quot;Sam&quot;, 38, HOBBIT),
   *   new TolkienCharacter(&quot;Gandalf&quot;, 2020, MAIA),
   *   new TolkienCharacter(&quot;Legolas&quot;, 1000, ELF),
   *   new TolkienCharacter(&quot;Pippin&quot;, 28, HOBBIT),
   *   new TolkienCharacter(&quot;Gimli&quot;, 139, DWARF),
   *   new TolkienCharacter(&quot;Aragorn&quot;, 87, MAN,
   *   new TolkienCharacter(&quot;Boromir&quot;, 37, MAN)
   * };
   *
   * // let's verify the names of TolkienCharacter in fellowshipOfTheRing :
   *
   * assertThat(fellowshipOfTheRing).extracting(&quot;name&quot;, String.class)
   *           .contains(&quot;Boromir&quot;, &quot;Gandalf&quot;, &quot;Frodo&quot;)
   *           .doesNotContain(&quot;Sauron&quot;, &quot;Elrond&quot;);
   *
   * // you can also extract nested field/property like the name of Race :
   *
   * assertThat(fellowshipOfTheRing).extracting(&quot;race.name&quot;, String.class)
   *                                .contains(&quot;Hobbit&quot;, &quot;Elf&quot;)
   *                                .doesNotContain(&quot;Orc&quot;);</code></pre>
   *
   * A property with the given name is looked for first, if it does not exist then a field with the given name
   * is looked for.
   * <p>
   * Note that the order of extracted field/property values is consistent with the order of the array under test.
   *
   * @param <P> the extracted type
   * @param fieldOrProperty the field/property to extract from the array under test
   * @param extractingType type to return
   * @return a new assertion object whose object under test is the array of extracted field/property values.
   * @throws IntrospectionError if no field or property exists with the given name
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public <P> ObjectArrayAssert<P> extracting(String fieldOrProperty, Class<P> extractingType) {
    @SuppressWarnings("unchecked")
    P[] values = (P[]) FieldsOrPropertiesExtractor.extract(array, byName(fieldOrProperty));
    String extractedDescription = extractedDescriptionOf(fieldOrProperty);
    String description = mostRelevantDescription(info.description(), extractedDescription);
    return new ObjectArrayAssert<>(values).as(description);
  }

  /**
   * Extract the values of given fields/properties from the array's elements under test into a new array composed of
   * Tuple (a simple data structure), this new array becoming the array under test.
   * <p>
   * It allows you to test fields/properties of the array's elements instead of testing the elements themselves, it
   * can be sometimes much less work !
   * <p>
   * The Tuple data corresponds to the extracted values of the given fields/properties, for instance if you ask to
   * extract "id", "name" and "email" then each Tuple data will be composed of id, name and email extracted from the
   * element of the initial array (the Tuple's data order is the same as the given fields/properties order).
   * <p>
   * Let's take an example to make things clearer :
   * <pre><code class='java'> // Build an array of TolkienCharacter, a TolkienCharacter has a name (String) and a Race (a class)
   * // they can be public field or properties, both works when extracting their values.
   * AtomicReferenceArray&lt;TolkienCharacter&gt; fellowshipOfTheRing = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[]{
   *   new TolkienCharacter(&quot;Frodo&quot;, 33, HOBBIT),
   *   new TolkienCharacter(&quot;Sam&quot;, 38, HOBBIT),
   *   new TolkienCharacter(&quot;Gandalf&quot;, 2020, MAIA),
   *   new TolkienCharacter(&quot;Legolas&quot;, 1000, ELF),
   *   new TolkienCharacter(&quot;Pippin&quot;, 28, HOBBIT),
   *   new TolkienCharacter(&quot;Gimli&quot;, 139, DWARF),
   *   new TolkienCharacter(&quot;Aragorn&quot;, 87, MAN,
   *   new TolkienCharacter(&quot;Boromir&quot;, 37, MAN)
   * };
   *
   * // let's verify 'name' and 'age' of some TolkienCharacter in fellowshipOfTheRing :
   *
   * assertThat(fellowshipOfTheRing).extracting(&quot;name&quot;, &quot;age&quot;)
   *                                .contains(tuple(&quot;Boromir&quot;, 37),
   *                                          tuple(&quot;Sam&quot;, 38),
   *                                          tuple(&quot;Legolas&quot;, 1000));
   *
   *
   * // extract 'name', 'age' and Race name values.
   *
   * assertThat(fellowshipOfTheRing).extracting(&quot;name&quot;, &quot;age&quot;, &quot;race.name&quot;)
   *                                .contains(tuple(&quot;Boromir&quot;, 37, &quot;Man&quot;),
   *                                          tuple(&quot;Sam&quot;, 38, &quot;Hobbit&quot;),
   *                                          tuple(&quot;Legolas&quot;, 1000, &quot;Elf&quot;));</code></pre>
   *
   * A property with the given name is looked for first, if it does not exist a field with the given name is looked for.
   * <p>
   * Note that the order of extracted property/field values is consistent with the iteration order of the array under
   * test.
   *
   * @param propertiesOrFields the properties/fields to extract from the initial array under test
   * @return a new assertion object whose object under test is the list of Tuple with extracted properties/fields values
   *         as data.
   * @throws IntrospectionError if one of the given name does not match a field or property in one of the initial
   *         Iterable's element.
   */
  @CheckReturnValue
  public ObjectArrayAssert<Tuple> extracting(String... propertiesOrFields) {
    Object[] values = FieldsOrPropertiesExtractor.extract(array, byName(propertiesOrFields));
    Tuple[] result = Arrays.copyOf(values, values.length, Tuple[].class);
    String extractedDescription = extractedDescriptionOf(propertiesOrFields);
    String description = mostRelevantDescription(info.description(), extractedDescription);
    return new ObjectArrayAssert<>(result).as(description);
  }

  /**
   * Extract the values from the array's elements by applying an extracting function on them. The returned
   * array becomes a new object under test.
   * <p>
   * It allows to test values from the elements in safer way than by using {@link #extracting(String)}, as it
   * doesn't utilize introspection.
   * <p>
   * Let's take a look an example:
   * <pre><code class='java'> // Build a list of TolkienCharacter, a TolkienCharacter has a name, and age and a Race (a specific class)
   * // they can be public field or properties, both can be extracted.
   * AtomicReferenceArray&lt;TolkienCharacter&gt; fellowshipOfTheRing = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[]{
   *   new TolkienCharacter(&quot;Frodo&quot;, 33, HOBBIT),
   *   new TolkienCharacter(&quot;Sam&quot;, 38, HOBBIT),
   *   new TolkienCharacter(&quot;Gandalf&quot;, 2020, MAIA),
   *   new TolkienCharacter(&quot;Legolas&quot;, 1000, ELF),
   *   new TolkienCharacter(&quot;Pippin&quot;, 28, HOBBIT),
   *   new TolkienCharacter(&quot;Gimli&quot;, 139, DWARF),
   *   new TolkienCharacter(&quot;Aragorn&quot;, 87, MAN,
   *   new TolkienCharacter(&quot;Boromir&quot;, 37, MAN)
   * };
   *
   *
   * // this extracts the race
   * Function&lt;TolkienCharacter, Race&gt; race = new Function&lt;TolkienCharacter, Race&gt;() {
   *    {@literal @}Override
   *    public Race extract(TolkienCharacter input) {
   *        return input.getRace();
   *    }
   * }
   *
   * // fellowship has hobbits, right, my presioussss?
   * assertThat(fellowshipOfTheRing).extracting(race).contains(HOBBIT);</code></pre>
   *
   * Note that the order of extracted property/field values is consistent with the iteration order of the Iterable under
   * test, for example if it's a {@link HashSet}, you won't be able to make any assumptions on the extracted values
   * order.
   *
   * @param <U> the extracted values type
   * @param extractor the object transforming input object to desired one
   * @return a new assertion object whose object under test is the list of values extracted
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public <U> ObjectArrayAssert<U> extracting(Function<? super T, U> extractor) {
    U[] extracted = FieldsOrPropertiesExtractor.extract(array, extractor);

    return new ObjectArrayAssert<>(extracted);
  }

  /**
   * Extract the values from the array's elements by applying an extracting function (which might throw an
   * exception) on them. The returned array becomes a new object under test.
   * <p>
   * Any checked exception raised in the extractor is rethrown wrapped in a {@link RuntimeException}.
   * <p>
   * It allows to test values from the elements in safer way than by using {@link #extracting(String)}, as it
   * doesn't utilize introspection.
   * <p>
   * Let's take a look an example:
   * <pre><code class='java'> // Build a list of TolkienCharacter, a TolkienCharacter has a name, and age and a Race (a specific class)
   * // they can be public field or properties, both can be extracted.
   * AtomicReferenceArray&lt;TolkienCharacter&gt; fellowshipOfTheRing = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[]{
   *   new TolkienCharacter(&quot;Frodo&quot;, 33, HOBBIT),
   *   new TolkienCharacter(&quot;Sam&quot;, 38, HOBBIT),
   *   new TolkienCharacter(&quot;Gandalf&quot;, 2020, MAIA),
   *   new TolkienCharacter(&quot;Legolas&quot;, 1000, ELF),
   *   new TolkienCharacter(&quot;Pippin&quot;, 28, HOBBIT),
   *   new TolkienCharacter(&quot;Gimli&quot;, 139, DWARF),
   *   new TolkienCharacter(&quot;Aragorn&quot;, 87, MAN,
   *   new TolkienCharacter(&quot;Boromir&quot;, 37, MAN)
   * };
   *
   * assertThat(fellowshipOfTheRing).extracting(input -&gt; {
   *   if (input.getAge() &lt; 20) {
   *     throw new Exception("age &lt; 20");
   *   }
   *   return input.getName();
   * }).contains("Frodo");</code></pre>
   *
   * Note that the order of extracted property/field values is consistent with the iteration order of the Iterable under
   * test, for example if it's a {@link HashSet}, you won't be able to make any assumptions on the extracted values
   * order.
   *
   * @param <U> the extracted values type
   * @param <EXCEPTION> the exception type
   * @param extractor the object transforming input object to desired one
   * @return a new assertion object whose object under test is the list of values extracted
   * @since 3.7.0
   */
  @CheckReturnValue
  public <U, EXCEPTION extends Exception> ObjectArrayAssert<U> extracting(ThrowingExtractor<? super T, U, EXCEPTION> extractor) {
    U[] extracted = FieldsOrPropertiesExtractor.extract(array, extractor);

    return new ObjectArrayAssert<>(extracted);
  }

  /**
   * Extract the Iterable values from the array's elements by applying an Iterable extracting function on them
   * and concatenating the result lists into an array which becomes the new object under test.
   * <p>
   * It allows testing the results of extracting values that are represented by Iterables.
   * <p>
   * For example:
   * <pre><code class='java'> CartoonCharacter bart = new CartoonCharacter("Bart Simpson");
   * CartoonCharacter lisa = new CartoonCharacter("Lisa Simpson");
   * CartoonCharacter maggie = new CartoonCharacter("Maggie Simpson");
   * CartoonCharacter homer = new CartoonCharacter("Homer Simpson");
   * homer.addChildren(bart, lisa, maggie);
   *
   * CartoonCharacter pebbles = new CartoonCharacter("Pebbles Flintstone");
   * CartoonCharacter fred = new CartoonCharacter("Fred Flintstone");
   * fred.getChildren().add(pebbles);
   *
   * Function&lt;CartoonCharacter, List&lt;CartoonCharacter&gt;&gt; childrenOf = new Function&lt;CartoonCharacter, List&lt;CartoonCharacter&gt;&gt;() {
   *    {@literal @}Override
   *    public List&lt;CartoonChildren&gt; extract(CartoonCharacter input) {
   *        return input.getChildren();
   *    }
   * }
   *
   * AtomicReferenceArray&lt;CartoonCharacter&gt; parents = new AtomicReferenceArray&lt;&gt;(new CartoonCharacter[]{ homer, fred });
   * // check children
   * assertThat(parents).flatExtracting(childrenOf)
   *                    .containsOnly(bart, lisa, maggie, pebbles);</code></pre>
   *
   * The order of extracted values is consisted with both the order of the collection itself, and the extracted
   * collections.
   *
   * @param <U> the type of elements to extract.
   * @param <C> the type of collection to flat/extract.
   * @param extractor the object transforming input object to an Iterable of desired ones
   * @return a new assertion object whose object under test is the list of values extracted
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public <U, C extends Collection<U>> ObjectArrayAssert<U> flatExtracting(Function<? super T, C> extractor) {
    return doFlatExtracting(extractor);
  }

  /**
   * Extract the Iterable values from the array's elements by applying an Iterable extracting function (which might
   * throw an exception) on them and concatenating the result lists into an array which becomes the new object under
   * test.
   * <p>
   * It allows testing the results of extracting values that are represented by Iterables.
   * <p>
   * For example:
   * <pre><code class='java'> CartoonCharacter bart = new CartoonCharacter("Bart Simpson");
   * CartoonCharacter lisa = new CartoonCharacter("Lisa Simpson");
   * CartoonCharacter maggie = new CartoonCharacter("Maggie Simpson");
   * CartoonCharacter homer = new CartoonCharacter("Homer Simpson");
   * homer.addChildren(bart, lisa, maggie);
   *
   * CartoonCharacter pebbles = new CartoonCharacter("Pebbles Flintstone");
   * CartoonCharacter fred = new CartoonCharacter("Fred Flintstone");
   * fred.getChildren().add(pebbles);
   *
   * AtomicReferenceArray&lt;CartoonCharacter&gt; parents = new AtomicReferenceArray&lt;&gt;(new CartoonCharacter[]{ homer, fred });
   * // check children
   * assertThat(parents).flatExtracting(input -&gt; {
   *   if (input.getChildren().size() == 0) {
   *     throw new Exception("no children");
   *   }
   *   return input.getChildren();
   * }).containsOnly(bart, lisa, maggie, pebbles);</code></pre>
   *
   * The order of extracted values is consisted with both the order of the collection itself, and the extracted
   * collections.
   *
   * @param <U> the type of elements to extract.
   * @param <C> the type of collection to flat/extract.
   * @param <EXCEPTION> the exception type
   * @param extractor the object transforming input object to an Iterable of desired ones
   * @return a new assertion object whose object under test is the list of values extracted
   * @since 3.7.0
   */
  @CheckReturnValue
  public <U, C extends Collection<U>, EXCEPTION extends Exception> ObjectArrayAssert<U> flatExtracting(ThrowingExtractor<? super T, C, EXCEPTION> extractor) {
    return doFlatExtracting(extractor);
  }

  private <U, C extends Collection<U>> ObjectArrayAssert<U> doFlatExtracting(Function<? super T, C> extractor) {
    List<U> result = FieldsOrPropertiesExtractor.extract(Arrays.asList(array), extractor).stream()
                                                .flatMap(Collection::stream).collect(toList());
    return new ObjectArrayAssert<>(toArray(result));
  }

  /**
   * Extract from array's elements the Iterable/Array values corresponding to the given property/field name and
   * concatenate them into a single array becoming the new object under test.
   * <p>
   * It allows testing the elements of extracting values that are represented by iterables or arrays.
   * <p>
   * For example:
   * <pre><code class='java'> CartoonCharacter bart = new CartoonCharacter("Bart Simpson");
   * CartoonCharacter lisa = new CartoonCharacter("Lisa Simpson");
   * CartoonCharacter maggie = new CartoonCharacter("Maggie Simpson");
   * CartoonCharacter homer = new CartoonCharacter("Homer Simpson");
   * homer.addChildren(bart, lisa, maggie);
   *
   * CartoonCharacter pebbles = new CartoonCharacter("Pebbles Flintstone");
   * CartoonCharacter fred = new CartoonCharacter("Fred Flintstone");
   * fred.getChildren().add(pebbles);
   *
   * AtomicReferenceArray&lt;CartoonCharacter&gt; parents = new AtomicReferenceArray&lt;&gt;(new CartoonCharacter[]{ homer, fred });
   * // check children
   * assertThat(parents).flatExtracting("children")
   *                    .containsOnly(bart, lisa, maggie, pebbles);</code></pre>
   *
   * The order of extracted values is consisted with both the order of the collection itself, and the extracted
   * collections.
   *
   * @param propertyName the object transforming input object to an Iterable of desired ones
   * @return a new assertion object whose object under test is the list of values extracted
   * @throws IllegalArgumentException if one of the extracted property value was not an array or an iterable.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public ObjectArrayAssert<Object> flatExtracting(String propertyName) {
    List<Object> extractedValues = newArrayList();
    List<?> extractedGroups = FieldsOrPropertiesExtractor.extract(Arrays.asList(array), byName(propertyName));
    for (Object group : extractedGroups) {
      // expecting AtomicReferenceArray to be an iterable or an array
      if (isArray(group)) {
        int size = Array.getLength(group);
        for (int i = 0; i < size; i++) {
          extractedValues.add(Array.get(group, i));
        }
      } else if (group instanceof Iterable<?> iterable) {
        for (Object value : iterable) {
          extractedValues.add(value);
        }
      } else {
        CommonErrors.wrongElementTypeForFlatExtracting(group);
      }
    }
    return new ObjectArrayAssert<>(extractedValues.toArray());
  }

  /**
   * Extract the result of given method invocation from the array's elements under test into a new array, this new array
   * becoming the array under test.
   * <p>
   * It allows you to test a method results of the array's elements instead of testing the elements themselves, which can be
   * much less work!
   * <p>
   * It is especially useful for classes that does not conform to the Java Bean's getter specification (i.e. public String
   * toString() or public String status() instead of public String getStatus()).
   * <p>
   * Let's take an example to make things clearer :
   * <pre><code class='java'> // Build a array of WesterosHouse, a WesterosHouse has a method: public String sayTheWords()
   * AtomicReferenceArray&lt;WesterosHouse&gt; greatHousesOfWesteros = new AtomicReferenceArray&lt;&gt;(new WesterosHouse[]{
   *     new WesterosHouse(&quot;Stark&quot;, &quot;Winter is Coming&quot;),
   *     new WesterosHouse(&quot;Lannister&quot;, &quot;Hear Me Roar!&quot;),
   *     new WesterosHouse(&quot;Greyjoy&quot;, &quot;We Do Not Sow&quot;),
   *     new WesterosHouse(&quot;Baratheon&quot;, &quot;Our is the Fury&quot;),
   *     new WesterosHouse(&quot;Martell&quot;, &quot;Unbowed, Unbent, Unbroken&quot;),
   *     new WesterosHouse(&quot;Tyrell&quot;, &quot;Growing Strong&quot;) });
   *
   * // let's verify the words of the great houses of Westeros:
   * assertThat(greatHousesOfWesteros).extractingResultOf(&quot;sayTheWords&quot;)
   *                                  .contains(&quot;Winter is Coming&quot;, &quot;We Do Not Sow&quot;, &quot;Hear Me Roar&quot;)
   *                                  .doesNotContain(&quot;Lannisters always pay their debts&quot;);</code></pre>
   *
   * <p>
   * Following requirements have to be met to extract method results:
   * <ul>
   * <li>method has to be public,</li>
   * <li>method cannot accept any arguments,</li>
   * <li>method cannot return void.</li>
   * </ul>
   * <p>
   * Note that the order of extracted values is consistent with the order of the array under test.
   *
   * @param method the name of the method which result is to be extracted from the array under test
   * @return a new assertion object whose object under test is the array of extracted values.
   * @throws IllegalArgumentException if no method exists with the given name, or method is not public, or method does
   *           return void, or method accepts arguments.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public ObjectArrayAssert<Object> extractingResultOf(String method) {
    Object[] values = FieldsOrPropertiesExtractor.extract(array, resultOf(method));
    String extractedDescription = extractedDescriptionOfMethod(method);
    String description = mostRelevantDescription(info.description(), extractedDescription);
    return new ObjectArrayAssert<>(values).as(description);
  }

  /**
   * Extract the result of given method invocation from the array's elements under test into a new array, this new array
   * becoming the array under test.
   * <p>
   * It allows you to test a method results of the array's elements instead of testing the elements themselves, which can be
   * much less work!
   * <p>
   * It is especially useful for classes that do not conform to the Java Bean's getter specification (i.e. public String
   * toString() or public String status() instead of public String getStatus()).
   * <p>
   * Let's take an example to make things clearer :
   * <pre><code class='java'> // Build a array of WesterosHouse, a WesterosHouse has a method: public String sayTheWords()
   * AtomicReferenceArray&lt;WesterosHouse&gt; greatHousesOfWesteros = new AtomicReferenceArray&lt;&gt;(new WesterosHouse[]{
   *     new WesterosHouse(&quot;Stark&quot;, &quot;Winter is Coming&quot;),
   *     new WesterosHouse(&quot;Lannister&quot;, &quot;Hear Me Roar!&quot;),
   *     new WesterosHouse(&quot;Greyjoy&quot;, &quot;We Do Not Sow&quot;),
   *     new WesterosHouse(&quot;Baratheon&quot;, &quot;Our is the Fury&quot;),
   *     new WesterosHouse(&quot;Martell&quot;, &quot;Unbowed, Unbent, Unbroken&quot;),
   *     new WesterosHouse(&quot;Tyrell&quot;, &quot;Growing Strong&quot;) });
   *
   * // let's verify the words of the great houses of Westeros:
   * assertThat(greatHousesOfWesteros).extractingResultOf(&quot;sayTheWords&quot;, String.class)
   *                                  .contains(&quot;Winter is Coming&quot;, &quot;We Do Not Sow&quot;, &quot;Hear Me Roar&quot;)
   *                                  .doesNotContain(&quot;Lannisters always pay their debts&quot;);</code></pre>
   *
   * <p>
   * Following requirements have to be met to extract method results:
   * <ul>
   * <li>method has to be public,</li>
   * <li>method can not accept any arguments,</li>
   * <li>method can not return void.</li>
   * </ul>
   * <p>
   * Note that the order of extracted values is consistent with the order of the array under test.
   *
   * @param <P> the extracted type
   * @param method the name of the method which result is to be extracted from the array under test
   * @param extractingType type to return
   * @return a new assertion object whose object under test is the array of extracted values.
   * @throws IllegalArgumentException if no method exists with the given name, or method is not public, or method does
   *           return void, or method accepts arguments.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public <P> ObjectArrayAssert<P> extractingResultOf(String method, Class<P> extractingType) {
    @SuppressWarnings("unchecked")
    P[] values = (P[]) FieldsOrPropertiesExtractor.extract(array, resultOf(method));
    String extractedDescription = extractedDescriptionOfMethod(method);
    String description = mostRelevantDescription(info.description(), extractedDescription);
    return new ObjectArrayAssert<>(values).as(description);
  }

  /**
   * Enable hexadecimal object representation of Iterable elements instead of standard java representation in error
   * messages.
   * <p>
   * It can be useful to better understand what the error was with a more meaningful error message.
   * <p>
   * Example
   * <pre><code class='java'>
   * AtomicReferenceArray&lt;Byte&gt; bytes = new AtomicReferenceArray&lt;&gt;(new Byte[]{ 0x10, 0x20 });
   * assertThat(bytes).inHexadecimal().contains(new Byte[] { 0x30 });</code></pre>
   *
   * With standard error message:
   * <pre><code class='java'> Expecting:
   *  &lt;[16, 32]&gt;
   * to contain:
   *  &lt;[48]&gt;
   * but could not find:
   *  &lt;[48]&gt;</code></pre>
   *
   * With Hexadecimal error message:
   * <pre><code class='java'> Expecting:
   *  &lt;[0x10, 0x20]&gt;
   * to contain:
   *  &lt;[0x30]&gt;
   * but could not find:
   *  &lt;[0x30]&gt;</code></pre>
   *
   * @return {@code this} assertion object.
   * @since 2.7.0 / 3.7.0
   */
  @Override
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> inHexadecimal() {
    return super.inHexadecimal();
  }

  @Override
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> inBinary() {
    return super.inBinary();
  }

  /**
   * Filter the array under test keeping only elements having a property or field equal to {@code expectedValue}, the
   * property/field is specified by {@code propertyOrFieldName} parameter.
   * <p>
   * The filter first tries to get the value from a property (named {@code propertyOrFieldName}), if no such property
   * exists it tries to read the value from a field. Reading private fields is supported by default, this can be
   * globally disabled by calling {@link Assertions#setAllowExtractingPrivateFields(boolean)
   * Assertions.setAllowExtractingPrivateFields(false)}.
   * <p>
   * When reading <b>nested</b> property/field, if an intermediate value is null the whole nested property/field is
   * considered to be null, thus reading "address.street.name" value will return null if "street" value is null.
   * <p>
   *
   * As an example, let's check all employees 800 years old (yes, special employees):
   * <pre><code class='java'> Employee yoda   = new Employee(1L, new Name("Yoda"), 800);
   * Employee obiwan = new Employee(2L, new Name("Obiwan"), 800);
   * Employee luke   = new Employee(3L, new Name("Luke", "Skywalker"), 26);
   * Employee noname = new Employee(4L, null, 50);
   *
   * AtomicReferenceArray&lt;Employee&gt; employees = new AtomicReferenceArray&lt;&gt;(new Employee[]{ yoda, luke, obiwan, noname });
   *
   * assertThat(employees).filteredOn("age", 800)
   *                      .containsOnly(yoda, obiwan);</code></pre>
   *
   * Nested properties/fields are supported:
   * <pre><code class='java'> // Name is bean class with 'first' and 'last' String properties
   *
   * // name is null for noname =&gt; it does not match the filter on "name.first"
   * assertThat(employees).filteredOn("name.first", "Luke")
   *                      .containsOnly(luke);
   *
   * assertThat(employees).filteredOn("name.last", "Vader")
   *                      .isEmpty();</code></pre>
   * <p>
   * If you want to filter on null value, use {@link #filteredOnNull(String)} as Java will resolve the call to
   * {@link #filteredOn(String, FilterOperator)} instead of this method.
   * <p>
   * An {@link IntrospectionError} is thrown if the given propertyOrFieldName can't be found in one of the array
   * elements.
   * <p>
   * You can chain filters:
   * <pre><code class='java'> // fellowshipOfTheRing is an array of TolkienCharacter having race and name fields
   * // 'not' filter is statically imported from Assertions.not
   *
   * assertThat(fellowshipOfTheRing).filteredOn("race.name", "Man")
   *                                .filteredOn("name", not("Boromir"))
   *                                .containsOnly(aragorn);</code></pre>
   * If you need more complex filter, use {@link #filteredOn(Condition)} and provide a {@link Condition} to specify the
   * filter to apply.
   *
   * @param propertyOrFieldName the name of the property or field to read
   * @param expectedValue the value to compare element's property or field with
   * @return a new assertion object with the filtered array under test
   * @throws IllegalArgumentException if the given propertyOrFieldName is {@code null} or empty.
   * @throws IntrospectionError if the given propertyOrFieldName can't be found in one of the array elements.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> filteredOn(String propertyOrFieldName, Object expectedValue) {
    return internalFilteredOn(propertyOrFieldName, expectedValue);
  }

  /**
   * Filter the array under test keeping only elements whose property or field specified by {@code propertyOrFieldName}
   * is null.
   * <p>
   * exists it tries to read the value from a field. Reading private fields is supported by default, this can be
   * globally disabled by calling {@link Assertions#setAllowExtractingPrivateFields(boolean)
   * Assertions.setAllowExtractingPrivateFields(false)}.
   * <p>
   * When reading <b>nested</b> property/field, if an intermediate value is null the whole nested property/field is
   * considered to be null, thus reading "address.street.name" value will return null if "street" value is null.
   * <p>
   * As an example, let's check all employees 800 years old (yes, special employees):
   * <pre><code class='java'> Employee yoda   = new Employee(1L, new Name("Yoda"), 800);
   * Employee obiwan = new Employee(2L, new Name("Obiwan"), 800);
   * Employee luke   = new Employee(3L, new Name("Luke", "Skywalker"), 26);
   * Employee noname = new Employee(4L, null, 50);
   *
   * AtomicReferenceArray&lt;Employee&gt; employees = new AtomicReferenceArray&lt;&gt;(new Employee[]{ yoda, luke, obiwan, noname });
   *
   * assertThat(employees).filteredOnNull("name")
   *                      .containsOnly(noname);</code></pre>
   *
   * Nested properties/fields are supported:
   * <pre><code class='java'> // Name is bean class with 'first' and 'last' String properties
   *
   * assertThat(employees).filteredOnNull("name.last")
   *                      .containsOnly(yoda, obiwan, noname);</code></pre>
   *
   * An {@link IntrospectionError} is thrown if the given propertyOrFieldName can't be found in one of the array
   * elements.
   * <p>
   * If you need more complex filter, use {@link #filteredOn(Condition)} and provide a {@link Condition} to specify the
   * filter to apply.
   *
   * @param propertyOrFieldName the name of the property or field to read
   * @return a new assertion object with the filtered array under test
   * @throws IntrospectionError if the given propertyOrFieldName can't be found in one of the array elements.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> filteredOnNull(String propertyOrFieldName) {
    // call internalFilteredOn to avoid double proxying in soft assertions
    return internalFilteredOn(propertyOrFieldName, null);
  }

  /**
   * Filter the array under test keeping only elements having a property or field matching the filter expressed with
   * the {@link FilterOperator}, the property/field is specified by {@code propertyOrFieldName} parameter.
   * <p>
   * The existing filters are :
   * <ul>
   * <li> {@link Assertions#not(Object) not(Object)}</li>
   * <li> {@link Assertions#in(Object...) in(Object...)}</li>
   * <li> {@link Assertions#notIn(Object...) notIn(Object...)}</li>
   * </ul>
   * <p>
   * Whatever filter is applied, it first tries to get the value from a property (named {@code propertyOrFieldName}), if
   * no such property exists it tries to read the value from a field. Reading private fields is supported by default,
   * this can be globally disabled by calling {@link Assertions#setAllowExtractingPrivateFields(boolean)
   * Assertions.setAllowExtractingPrivateFields(false)}.
   * <p>
   * When reading <b>nested</b> property/field, if an intermediate value is null the whole nested property/field is
   * considered to be null, thus reading "address.street.name" value will return null if "street" value is null.
   * <p>
   *
   * As an example, let's check stuff on some special employees :
   * <pre><code class='java'> Employee yoda   = new Employee(1L, new Name("Yoda"), 800);
   * Employee obiwan = new Employee(2L, new Name("Obiwan"), 800);
   * Employee luke   = new Employee(3L, new Name("Luke", "Skywalker"), 26);
   *
   * AtomicReferenceArray&lt;Employee&gt; employees = new AtomicReferenceArray&lt;&gt;(new Employee[]{ yoda, luke, obiwan, noname });
   *
   * // 'not' filter is statically imported from Assertions.not
   * assertThat(employees).filteredOn("age", not(800))
   *                      .containsOnly(luke);
   *
   * // 'in' filter is statically imported from Assertions.in
   * // Name is bean class with 'first' and 'last' String properties
   * assertThat(employees).filteredOn("name.first", in("Yoda", "Luke"))
   *                      .containsOnly(yoda, luke);
   *
   * // 'notIn' filter is statically imported from Assertions.notIn
   * assertThat(employees).filteredOn("name.first", notIn("Yoda", "Luke"))
   *                      .containsOnly(obiwan);</code></pre>
   *
   * An {@link IntrospectionError} is thrown if the given propertyOrFieldName can't be found in one of the array
   * elements.
   * <p>
   * Note that combining filter operators is not supported, thus the following code is not correct:
   * <pre><code class='java'> // Combining filter operators like not(in(800)) is NOT supported
   * // -&gt; throws UnsupportedOperationException
   * assertThat(employees).filteredOn("age", not(in(800)))
   *                      .contains(luke);</code></pre>
   * <p>
   * You can chain filters:
   * <pre><code class='java'> // fellowshipOfTheRing is an array of TolkienCharacter having race and name fields
   * // 'not' filter is statically imported from Assertions.not
   *
   * assertThat(fellowshipOfTheRing).filteredOn("race.name", "Man")
   *                                .filteredOn("name", not("Boromir"))
   *                                .containsOnly(aragorn);</code></pre>
   *
   * If you need more complex filter, use {@link #filteredOn(Condition)} or {@link #filteredOn(Predicate)} and
   * provide a {@link Condition} or {@link Predicate} to specify the filter to apply.
   *
   * @param propertyOrFieldName the name of the property or field to read
   * @param filterOperator the filter operator to apply
   * @return a new assertion object with the filtered array under test
   * @throws IllegalArgumentException if the given propertyOrFieldName is {@code null} or empty.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> filteredOn(String propertyOrFieldName, FilterOperator<?> filterOperator) {
    checkNotNull(filterOperator);
    Filters<? extends T> filter = filter(array).with(propertyOrFieldName);
    filterOperator.applyOn(filter);
    return new AtomicReferenceArrayAssert<>(new AtomicReferenceArray<>(toArray(filter.get())));
  }

  /**
   * Filter the array under test keeping only elements matching the given {@link Condition}.
   * <p>
   * Let's check old employees whose age &gt; 100:
   * <pre><code class='java'> Employee yoda   = new Employee(1L, new Name("Yoda"), 800);
   * Employee obiwan = new Employee(2L, new Name("Obiwan"), 800);
   * Employee luke   = new Employee(3L, new Name("Luke", "Skywalker"), 26);
   * Employee noname = new Employee(4L, null, 50);
   *
   * AtomicReferenceArray&lt;Employee&gt; employees = new AtomicReferenceArray&lt;&gt;(new Employee[]{ yoda, luke, obiwan, noname });
   *
   * // old employee condition, "old employees" describes the condition in error message
   * // you just have to implement 'matches' method
   * Condition&lt;Employee&gt; oldEmployees = new Condition&lt;Employee&gt;("old employees") {
   *       {@literal @}Override
   *       public boolean matches(Employee employee) {
   *         return employee.getAge() &gt; 100;
   *       }
   *     };
   *   }
   * assertThat(employees).filteredOn(oldEmployees)
   *                      .containsOnly(yoda, obiwan);</code></pre>
   *
   * You can combine {@link Condition} with condition operator like {@link Not}:
   * <pre><code class='java'> // 'not' filter is statically imported from Assertions.not
   * assertThat(employees).filteredOn(not(oldEmployees))
   *                      .contains(luke, noname);</code></pre>
   *
   * @param condition the filter condition / predicate
   * @return a new assertion object with the filtered array under test
   * @throws IllegalArgumentException if the given condition is {@code null}.
   * @since 2.7.0 / 3.7.0
   */
  @CheckReturnValue
  public AtomicReferenceArrayAssert<T> filteredOn(Condition<? super T> condition) {
    Iterable<? extends T> filteredIterable = filter(array).being(condition).get();
    return new AtomicReferenceArrayAssert<>(new AtomicReferenceArray<>(toArray(filteredIterable)));
  }

  /**
   * Filter the array under test into a list composed of the elements matching the given {@link Predicate},
   * allowing to perform assertions on the filtered list.
   * <p>
   * Example : check old employees whose age &gt; 100:
   *
   * <pre><code class='java'> Employee yoda   = new Employee(1L, new Name("Yoda"), 800);
   * Employee obiwan = new Employee(2L, new Name("Obiwan"), 800);
   * Employee luke   = new Employee(3L, new Name("Luke", "Skywalker"), 26);
   *
   * AtomicReferenceArray&lt;Employee&gt; employees = new AtomicReferenceArray&lt;&gt;(new Employee[]{ yoda, luke, obiwan, noname });
   *
   * assertThat(employees).filteredOn(employee -&gt; employee.getAge() &gt; 100)
   *                      .containsOnly(yoda, obiwan);</code></pre>
   *
   * @param predicate the filter predicate
   * @return a new assertion object with the filtered array under test
   * @throws IllegalArgumentException if the given predicate is {@code null}.
   * @since 3.16.0
   */
  public AtomicReferenceArrayAssert<T> filteredOn(Predicate<? super T> predicate) {
    return internalFilteredOn(predicate);
  }

  /**
   * Filter the array under test into a list composed of the elements for which the result of the {@code function} is equal to {@code expectedValue}.
   * <p>
   * It allows to filter elements in more safe way than by using {@link #filteredOn(String, Object)} as it doesn't utilize introspection.
   * <p>
   * As an example, let's check all employees 800 years old (yes, special employees):
   * <pre><code class='java'> Employee yoda   = new Employee(1L, new Name("Yoda"), 800);
   * Employee obiwan = new Employee(2L, new Name("Obiwan"), 800);
   * Employee luke   = new Employee(3L, new Name("Luke", "Skywalker"), 26);
   * Employee noname = new Employee(4L, null, 50);
   *
   * AtomicReferenceArray&lt;Employee&gt; employees = new AtomicReferenceArray&lt;&gt;(new Employee[]{ yoda, luke, obiwan, noname });
   *
   * assertThat(employees).filteredOn(Employee::getAge, 800)
   *                      .containsOnly(yoda, obiwan);
   *
   * assertThat(employees).filteredOn(e -&gt; e.getName(), null)
   *                      .containsOnly(noname);</code></pre>
   *
   * If you need more complex filter, use {@link #filteredOn(Predicate)} or {@link #filteredOn(Condition)}.
   *
   * @param <U> result type of the filter function
   * @param function the filter function
   * @param expectedValue the expected value of the filter function
   * @return a new assertion object with the filtered array under test
   * @throws IllegalArgumentException if the given function is {@code null}.
   * @since 3.17.0
   */
  @CheckReturnValue
  public <U> AtomicReferenceArrayAssert<T> filteredOn(Function<? super T, U> function, U expectedValue) {
    checkArgument(function != null, "The filter function should not be null");
    // call internalFilteredOn to avoid double proxying in soft assertions
    return internalFilteredOn(element -> java.util.Objects.equals(function.apply(element), expectedValue));
  }

  /**
   * Verifies that all elements match the given {@link Predicate}.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "cc"});
   *
   * // assertion will pass
   * assertThat(abc).allMatch(s -&gt; s.length() == 1);
   *
   * // assertion will fail
   * assertThat(abcc).allMatch(s -&gt; s.length() == 1);</code></pre>
   *
   * Note that you can achieve the same result with {@link #are(Condition) are(Condition)} or {@link #have(Condition) have(Condition)}.
   *
   * @param predicate the given {@link Predicate}.
   * @return {@code this} object.
   * @throws NullPointerException if the given predicate is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if one or more elements don't satisfy the given predicate.
   * @since 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> allMatch(Predicate<? super T> predicate) {
    iterables.assertAllMatch(info, newArrayList(array), predicate, PredicateDescription.GIVEN);
    return myself;
  }

  /**
   * Verifies that all the elements of actual's array match the given {@link Predicate}. The predicate description is used
   * to get an informative error message.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "cc"});
   *
   * // assertion will pass
   * assertThat(abc).allMatch(s -&gt; s.length() == 1, "length of 1");
   *
   * // assertion will fail
   * assertThat(abcc).allMatch(s -&gt; s.length() == 1, "length of 1");</code></pre>
   *
   * The message of the failed assertion would be:
   * <pre><code class='java'>Expecting all elements of:
   *  &lt;["a", "b", "cc"]&gt;
   *  to match 'length of 1' predicate but this element did not:
   *  &lt;"cc"&gt;</code></pre>
   *
   *
   * @param predicate the given {@link Predicate}.
   * @param predicateDescription a description of the {@link Predicate} used in the error message
   * @return {@code this} object.
   * @throws NullPointerException if the given predicate is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if one or more elements don't satisfy the given predicate.
   * @since 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> allMatch(Predicate<? super T> predicate, String predicateDescription) {
    iterables.assertAllMatch(info, newArrayList(array), predicate, new PredicateDescription(predicateDescription));
    return myself;
  }

  /**
   * Verifies that all the elements satisfy given requirements expressed as a {@link Consumer}.
   * <p>
   * This is useful to perform a group of assertions on elements.
   * <p>
   * Grouping assertions example:
   * <pre><code class='java'> // myIcelanderFriends is an AtomicReferenceArray&lt;Person&gt;
   * assertThat(myIcelanderFriends).allSatisfy(person -&gt; {
   *                                 assertThat(person.getCountry()).isEqualTo("Iceland");
   *                                 assertThat(person.getPhoneCountryCode()).isEqualTo("+354");
   *                               });</code></pre>
   *
   * @param requirements the given {@link Consumer}.
   * @return {@code this} object.
   * @throws NullPointerException if the given {@link Consumer} is {@code null}.
   * @throws AssertionError if one or more elements don't satisfy given requirements.
   * @since 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> allSatisfy(Consumer<? super T> requirements) {
    return internalAllSatisfy(requirements);
  }

  /**
   * Verifies that all the elements satisfy the given requirements expressed as a {@link ThrowingConsumer}.
   * <p>
   * This is useful to perform a group of assertions on elements.
   * <p>
   * This is the same assertion as {@link #allSatisfy(Consumer)} but the given consumer can throw checked exceptions.<br>
   * More precisely, {@link RuntimeException} and {@link AssertionError} are rethrown as they are and {@link Throwable} wrapped in a {@link RuntimeException}. 
   * <p>
   * Example:
   * <pre><code class='java'>  // read() throws IOException
   * // note that the code would not compile if isNotEmpty, startsWithA or startsWithZ were declared as a Consumer&lt;Reader&gt; 
   * ThrowingConsumer&lt;Reader&gt; isNotEmpty = reader -&gt; assertThat(reader.read()).isEqualTo(-1);
   * ThrowingConsumer&lt;Reader&gt; startsWithA = reader -&gt; assertThat(reader.read()).isEqualTo('A');
   *
   * // ABC.txt contains: ABC  
   * // XYZ.txt contains: XYZ  
   * AtomicReferenceArray&lt;FileReader&gt; fileReaders = new AtomicReferenceArray&lt;&gt;(new FileReader[] {new FileReader("ABC.txt"), new FileReader("XYZ.txt")});
   * 
   * // assertion succeeds as none of the files are empty
   * assertThat(fileReaders).allSatisfy(isNotEmpty);
   *
   * // assertion fails as XYZ.txt does not start with 'A':
   * assertThat(fileReaders).allSatisfy(startsWithA);</code></pre>
   * <p>
   * If the actual iterable is empty, this assertion succeeds as there is nothing to check.
   *
   * @param requirements the given {@link ThrowingConsumer}.
   * @return {@code this} object.
   * @throws NullPointerException if given {@link ThrowingConsumer} is null
   * @throws RuntimeException rethrown as is by the given {@link ThrowingConsumer} or wrapping any {@link Throwable}.    
   * @throws AssertionError if one or more elements don't satisfy the given requirements.
   * @since 3.21.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> allSatisfy(ThrowingConsumer<? super T> requirements) {
    return internalAllSatisfy(requirements);
  }

  private AtomicReferenceArrayAssert<T> internalAllSatisfy(Consumer<? super T> requirements) {
    iterables.assertAllSatisfy(info, newArrayList(array), requirements);
    return myself;
  }

  /**
   * Verifies whether any elements match the provided {@link Predicate}.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "c"});
   * AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[] {"a", "b", "cc"});
   *
   * // assertion will pass
   * assertThat(abc).anyMatch(s -&gt; s.length() == 2);
   *
   * // assertion will fail
   * assertThat(abcc).anyMatch(s -&gt; s.length() &gt; 2);</code></pre>
   *
   * Note that you can achieve the same result with {@link #areAtLeastOne(Condition) areAtLeastOne(Condition)}
   * or {@link #haveAtLeastOne(Condition) haveAtLeastOne(Condition)}.
   *
   * @param predicate the given {@link Predicate}.
   * @return {@code this} object.
   * @throws NullPointerException if the given predicate is {@code null}.
   * @throws AssertionError if no elements satisfy the given predicate.
   * @since 3.9.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> anyMatch(Predicate<? super T> predicate) {
    iterables.assertAnyMatch(info, newArrayList(array), predicate, PredicateDescription.GIVEN);
    return myself;
  }

  /**
   * Verifies whether any elements match the provided {@link Predicate}. The predicate description is used
   * to get an informative error message.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * // assertion will pass
   * assertThat(abcc).anyMatch(s -&gt; s.length() == 2, "length of 2");
   *
   * // assertion will fail
   * assertThat(abcc).anyMatch(s -&gt; s.length() &gt; 2, "length greater than 2);</code></pre>
   *
   * The message of the failed assertion would be:
   * <pre><code class='java'>Expecting any elements of:
   *  &lt;["a", "b", "cc"]&gt;
   *  to match 'length greater than 2' predicate but none did.</code></pre>
   *
   * @param predicate the given {@link Predicate}.
   * @param predicateDescription a description of the {@link Predicate} used in the error message
   * @return {@code this} object.
   * @throws NullPointerException if the given predicate is {@code null}.
   * @throws AssertionError if no elements satisfy the given predicate.
   * @since 3.27.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> anyMatch(Predicate<? super T> predicate, String predicateDescription) {
    iterables.assertAnyMatch(info, newArrayList(array), predicate, new PredicateDescription(predicateDescription));
    return myself;
  }

  /**
   * Verifies that at least one element satisfies the given requirements expressed as a {@link Consumer}.
   * <p>
   * This is useful to check that a group of assertions is verified by (at least) one element.
   * <p>
   * If the {@link AtomicReferenceArray} to assert is empty, the assertion will fail.
   * <p>
   * Grouping assertions example:
   * <pre><code class='java'> // myIcelanderFriends is an AtomicReferenceArray&lt;Person&gt;
   * assertThat(myIcelanderFriends).anySatisfy(person -&gt; {
   *                                 assertThat(person.getCountry()).isEqualTo("Iceland");
   *                                 assertThat(person.getPhoneCountryCode()).isEqualTo("+354");
   *                                 assertThat(person.getSurname()).endsWith("son");
   *                               });
   *
   * // assertion fails for empty group, whatever the requirements are.
   * assertThat(emptyArray).anySatisfy($ -&gt; {
   *                         assertThat(true).isTrue();
   *                       });</code></pre>
   *
   * @param requirements the given {@link Consumer}.
   * @return {@code this} object.
   * @throws NullPointerException if the given {@link Consumer} is {@code null}.
   * @throws AssertionError if all elements don't satisfy given requirements.
   * @since 3.7.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> anySatisfy(Consumer<? super T> requirements) {
    return internalAnySatisfy(requirements);
  }

  /**
   * Verifies that at least one element satisfies the given requirements expressed as a {@link ThrowingConsumer}.
   * <p>
   * This is useful to check that a group of assertions is verified by (at least) one element.
   * <p>
   * This is the same assertion as {@link #anySatisfy(Consumer)} but the given consumer can throw checked exceptions.<br>
   * More precisely, {@link RuntimeException} and {@link AssertionError} are rethrown as they are and {@link Throwable} wrapped in a {@link RuntimeException}. 
   * <p>
   * Example:
   * <pre><code class='java'>  // read() throws IOException
   * // note that the code would not compile if startsWithA, startsWithY or startsWithZ were declared as a Consumer&lt;Reader&gt; 
   * ThrowingConsumer&lt;Reader&gt; startsWithA = reader -&gt; assertThat(reader.read()).isEqualTo('A');
   * ThrowingConsumer&lt;Reader&gt; startsWithZ = reader -&gt; assertThat(reader.read()).isEqualTo('Z');
   *
   * // ABC.txt contains: ABC  
   * // XYZ.txt contains: XYZ  
   * AtomicReferenceArray&lt;FileReader&gt; fileReaders = new AtomicReferenceArray&lt;&gt;(new FileReader[] {new FileReader("ABC.txt"), new FileReader("XYZ.txt")});
   * 
   * // assertion succeeds as ABC.txt starts with 'A'
   * assertThat(fileReaders).anySatisfy(startsWithA);
   *
   * // assertion fails none of the files starts with 'Z':
   * assertThat(fileReaders).anySatisfy(startsWithZ);</code></pre>
   * <p>
   * If the actual iterable is empty, this assertion succeeds as there is nothing to check.
   *
   * @param requirements the given {@link ThrowingConsumer}.
   * @return {@code this} object.
   * @throws NullPointerException if given {@link ThrowingConsumer} is null
   * @throws RuntimeException rethrown as is by the given {@link ThrowingConsumer} or wrapping any {@link Throwable}.    
   * @throws AssertionError no elements satisfy the given requirements.
   * @since 3.21.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> anySatisfy(ThrowingConsumer<? super T> requirements) {
    return internalAnySatisfy(requirements);
  }

  private AtomicReferenceArrayAssert<T> internalAnySatisfy(Consumer<? super T> requirements) {
    iterables.assertAnySatisfy(info, newArrayList(array), requirements);
    return myself;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AtomicReferenceArrayAssert<T> noneSatisfy(Consumer<? super T> restrictions) {
    return internalNoneSatisfy(restrictions);
  }

  /**
   * Verifies that no elements satisfy the given restrictions expressed as a {@link Consumer}.
   * <p>
   * This is useful to check that a group of assertions is verified by (at least) one element.
   * <p>
   * This is the same assertion as {@link #anySatisfy(Consumer)} but the given consumer can throw checked exceptions.<br>
   * More precisely, {@link RuntimeException} and {@link AssertionError} are rethrown as they are and {@link Throwable} wrapped in a {@link RuntimeException}. 
   * <p>
   * Example:
   * <pre><code class='java'>  // read() throws IOException
   * // note that the code would not compile if startsWithA, startsWithY or startsWithZ were declared as a Consumer&lt;Reader&gt; 
   * ThrowingConsumer&lt;Reader&gt; startsWithA = reader -&gt; assertThat(reader.read()).isEqualTo('A');
   * ThrowingConsumer&lt;Reader&gt; startsWithZ = reader -&gt; assertThat(reader.read()).isEqualTo('Z');
   *
   * // ABC.txt contains: ABC  
   * // XYZ.txt contains: XYZ  
   * AtomicReferenceArray&lt;FileReader&gt; fileReaders = new AtomicReferenceArray&lt;&gt;(new FileReader[] {new FileReader("ABC.txt"), new FileReader("XYZ.txt")});
   * 
   * // assertion succeeds as none of the file starts 'Z'
   * assertThat(fileReaders).noneSatisfy(startsWithZ);
   *
   * // assertion fails as ABC.txt starts with 'A':
   * assertThat(fileReaders).noneSatisfy(startsWithA);</code></pre>
   * <p>
   * Note that this assertion succeeds if the group (collection, array, ...) is empty whatever the restrictions are.
   *
   * @param restrictions the given {@link ThrowingConsumer}.
   * @return {@code this} object.
   * @throws NullPointerException if given {@link ThrowingConsumer} is null
   * @throws RuntimeException rethrown as is by the given {@link ThrowingConsumer} or wrapping any {@link Throwable}.    
   * @throws AssertionError if one or more elements satisfy the given requirements.
   * @since 3.21.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> noneSatisfy(ThrowingConsumer<? super T> restrictions) {
    return internalNoneSatisfy(restrictions);
  }

  private AtomicReferenceArrayAssert<T> internalNoneSatisfy(Consumer<? super T> restrictions) {
    iterables.assertNoneSatisfy(info, newArrayList(array), restrictions);
    return myself;
  }

  /**
   * Verifies that each element satisfies the requirements corresponding to its index, so the first element must satisfy the
   * first requirements, the second element the second requirements etc...
   * <p>
   * Each requirement is expressed as a {@link Consumer}, there must be as many requirements as there are iterable elements.
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;TolkienCharacter&gt; characters = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[] {frodo, aragorn, legolas});
   *
   * // assertions succeed
   * assertThat(characters).satisfiesExactly(character -&gt; assertThat(character.getRace()).isEqualTo("Hobbit"),
   *                                         character -&gt; assertThat(character.isMortal()).isTrue(),
   *                                         character -&gt; assertThat(character.getName()).isEqualTo("Legolas"));
   *
   * // you can specify more that one assertion per requirements
   * assertThat(characters).satisfiesExactly(character -&gt; {
   *                                            assertThat(character.getRace()).isEqualTo("Hobbit");
   *                                            assertThat(character.getName()).isEqualTo("Frodo");
   *                                         },
   *                                         character -&gt; {
   *                                            assertThat(character.isMortal()).isTrue();
   *                                            assertThat(character.getName()).isEqualTo("Aragorn");
   *                                         },
   *                                         character -&gt; {
   *                                            assertThat(character.getRace()).isEqualTo("Elf");
   *                                            assertThat(character.getName()).isEqualTo("Legolas");
   *                                         });
   *
   * // assertion fails as aragorn does not meet the second requirements
   * assertThat(characters).satisfiesExactly(character -&gt; assertThat(character.getRace()).isEqualTo("Hobbit"),
   *                                         character -&gt; assertThat(character.isMortal()).isFalse(),
   *                                         character -&gt; assertThat(character.getName()).isEqualTo("Legolas"));</code></pre>
   *
   * @param requirements the requirements to meet.
   * @return {@code this} to chain assertions.
   * @throws NullPointerException if given requirements are null.
   * @throws AssertionError if any element does not satisfy the requirements at the same index
   * @throws AssertionError if there are not as many requirements as there are iterable elements.
   * @since 3.19.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> satisfiesExactly(Consumer<? super T>... requirements) {
    return satisfiesExactlyForProxy(requirements);
  }

  /**
   * Verifies that each element satisfies the requirements corresponding to its index, so the first element must satisfy the
   * first requirements, the second element the second requirements etc...
   * <p>
   * Each requirement is expressed as a {@link ThrowingConsumer}, there must be as many requirements as there are iterable elements.
   * <p>
   * This is the same assertion as {@link #satisfiesExactly(Consumer...)} but the given consumers can throw checked exceptions.<br>
   * More precisely, {@link RuntimeException} and {@link AssertionError} are rethrown as they are and {@link Throwable} wrapped in a {@link RuntimeException}. 
   * <p>
   * Example:
   * <pre><code class='java'> AtomicReferenceArray&lt;TolkienCharacter&gt; characters = new AtomicReferenceArray&lt;&gt;(new TolkienCharacter[] {frodo, aragorn, legolas});
   * 
   * // the code would compile even if TolkienCharacter.getRace(), isMortal() or getName() threw a checked exception
   *
   * // assertions succeed
   * assertThat(characters).satisfiesExactly(character -&gt; assertThat(character.getRace()).isEqualTo("Hobbit"),
   *                                         character -&gt; assertThat(character.isMortal()).isTrue(),
   *                                         character -&gt; assertThat(character.getName()).isEqualTo("Legolas"));
   *
   * // you can specify more that one assertion per requirements
   * assertThat(characters).satisfiesExactly(character -&gt; {
   *                                            assertThat(character.getRace()).isEqualTo("Hobbit");
   *                                            assertThat(character.getName()).isEqualTo("Frodo");
   *                                         },
   *                                         character -&gt; {
   *                                            assertThat(character.isMortal()).isTrue();
   *                                            assertThat(character.getName()).isEqualTo("Aragorn");
   *                                         },
   *                                         character -&gt; {
   *                                            assertThat(character.getRace()).isEqualTo("Elf");
   *                                            assertThat(character.getName()).isEqualTo("Legolas");
   *                                         });
   *
   * // assertion fails as aragorn does not meet the second requirements
   * assertThat(characters).satisfiesExactly(character -&gt; assertThat(character.getRace()).isEqualTo("Hobbit"),
   *                                         character -&gt; assertThat(character.isMortal()).isFalse(),
   *                                         character -&gt; assertThat(character.getName()).isEqualTo("Legolas"));</code></pre>
   *
   * @param requirements the requirements to meet.
   * @return {@code this} to chain assertions.
   * @throws NullPointerException if given requirements are null.
   * @throws RuntimeException rethrown as is by the given {@link ThrowingConsumer} or wrapping any {@link Throwable}.    
   * @throws AssertionError if any element does not satisfy the requirements at the same index
   * @throws AssertionError if there are not as many requirements as there are iterable elements.
   * @since 3.21.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> satisfiesExactly(ThrowingConsumer<? super T>... requirements) {
    return satisfiesExactlyForProxy(requirements);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> satisfiesExactlyForProxy(Consumer<? super T>[] requirements) {
    iterables.assertSatisfiesExactly(info, newArrayList(array), requirements);
    return myself;
  }

  /**
   * Verifies that at least one combination of iterable elements exists that satisfies the consumers in order (there must be as
   * many consumers as iterable elements and once a consumer is matched it cannot be reused to match other elements).
   * <p>
   * This is a variation of {@link #satisfiesExactly(Consumer...)} where order does not matter.
   * <p>
   * Examples:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; starWarsCharacterNames = new AtomicReferenceArray&lt;&gt;(new String[] {"Luke", "Leia", "Yoda"});
   *
   * // these assertions succeed:
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Y"), // matches "Yoda"
   *                                                               name -&gt; assertThat(name).contains("L"), // matches "Luke" and "Leia"
   *                                                               name -&gt; {
   *                                                                 assertThat(name).hasSize(4);
   *                                                                 assertThat(name).doesNotContain("a"); // matches "Luke" but not "Leia"
   *                                                               })
   *                                   .satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Yo"),
   *                                                               name -&gt; assertThat(name).contains("Lu"),
   *                                                               name -&gt; assertThat(name).contains("Le"))
   *                                   .satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Le"),
   *                                                               name -&gt; assertThat(name).contains("Yo"),
   *                                                               name -&gt; assertThat(name).contains("Lu"));
   *
   * // this assertion fails as 3 consumer/requirements are expected
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Y"),
   *                                                               name -&gt; assertThat(name).contains("L"));
   *
   * // this assertion fails as no element contains "Han" (first consumer/requirements can't be met)
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Han"),
   *                                                               name -&gt; assertThat(name).contains("L"),
   *                                                               name -&gt; assertThat(name).contains("Y"));
   *
   * // this assertion fails as "Yoda" element can't satisfy any consumers/requirements (even though all consumers/requirements are met)
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("L"),
   *                                                               name -&gt; assertThat(name).contains("L"),
   *                                                               name -&gt; assertThat(name).contains("L"));
   *
   * // this assertion fails as no combination of elements can satisfy the consumers in order
   * // the problem is if the last consumer is matched by Leia then no other consumer can match Luke (and vice versa)
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Y"),
   *                                                               name -&gt; assertThat(name).contains("o"),
   *                                                               name -&gt; assertThat(name).contains("L"));</code></pre>
   *
   * @param requirements the consumers that are expected to be satisfied by the elements of the given {@code Iterable}.
   * @return this assertion object.
   * @throws NullPointerException if the given consumers array or any consumer is {@code null}.
   * @throws AssertionError if there is no permutation of elements that satisfies the individual consumers in order
   * @throws AssertionError if there are not as many requirements as there are iterable elements.
   *
   * @since 3.19.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> satisfiesExactlyInAnyOrder(Consumer<? super T>... requirements) {
    return satisfiesExactlyInAnyOrderForProxy(requirements);
  }

  /**
   * Verifies that at least one combination of iterable elements exists that satisfies the {@link ThrowingConsumer}s in order (there must be as
   * many consumers as iterable elements and once a consumer is matched it cannot be reused to match other elements).
   * <p>
   * This is a variation of {@link #satisfiesExactly(ThrowingConsumer...)} where order does not matter.
   * <p>
   * Examples:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; starWarsCharacterNames = new AtomicReferenceArray&lt;&gt;(new String[] {"Luke", "Leia", "Yoda"});
   *
   * // these assertions succeed:
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Y"), // matches "Yoda"
   *                                                               name -&gt; assertThat(name).contains("L"), // matches "Luke" and "Leia"
   *                                                               name -&gt; {
   *                                                                 assertThat(name).hasSize(4);
   *                                                                 assertThat(name).doesNotContain("a"); // matches "Luke" but not "Leia"
   *                                                               })
   *                                   .satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Yo"),
   *                                                               name -&gt; assertThat(name).contains("Lu"),
   *                                                               name -&gt; assertThat(name).contains("Le"))
   *                                   .satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Le"),
   *                                                               name -&gt; assertThat(name).contains("Yo"),
   *                                                               name -&gt; assertThat(name).contains("Lu"));
   *
   * // this assertion fails as 3 consumers/requirements are expected
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Y"),
   *                                                               name -&gt; assertThat(name).contains("L"));
   *
   * // this assertion fails as no element contains "Han" (first consumer/requirements can't be met)
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Han"),
   *                                                               name -&gt; assertThat(name).contains("L"),
   *                                                               name -&gt; assertThat(name).contains("Y"));
   *
   * // this assertion fails as "Yoda" element can't satisfy any consumers/requirements (even though all consumers/requirements are met)
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("L"),
   *                                                               name -&gt; assertThat(name).contains("L"),
   *                                                               name -&gt; assertThat(name).contains("L"));
   *
   * // this assertion fails as no combination of elements can satisfy the consumers in order
   * // the problem is if the last consumer is matched by Leia then no other consumer can match Luke (and vice versa)
   * assertThat(starWarsCharacterNames).satisfiesExactlyInAnyOrder(name -&gt; assertThat(name).contains("Y"),
   *                                                               name -&gt; assertThat(name).contains("o"),
   *                                                               name -&gt; assertThat(name).contains("L"));</code></pre>
   *
   * @param requirements the consumers that are expected to be satisfied by the elements of the given {@code Iterable}.
   * @return this assertion object.
   * @throws NullPointerException if the given consumers array or any consumer is {@code null}.
   * @throws RuntimeException rethrown as is by the given {@link ThrowingConsumer} or wrapping any {@link Throwable}.    
   * @throws AssertionError if there is no permutation of elements that satisfies the individual consumers in order
   * @throws AssertionError if there are not as many requirements as there are iterable elements.
   * @since 3.21.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> satisfiesExactlyInAnyOrder(ThrowingConsumer<? super T>... requirements) {
    return satisfiesExactlyInAnyOrderForProxy(requirements);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> satisfiesExactlyInAnyOrderForProxy(Consumer<? super T>[] requirements) {
    iterables.assertSatisfiesExactlyInAnyOrder(info, newArrayList(array), requirements);
    return myself;
  }

  /**
   * Verifies that there is exactly one element in the {@link AtomicReferenceArray} under test that satisfies the {@link Consumer}.
   * <p>
   * Examples:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; starWarsCharacterNames = new AtomicReferenceArray&lt;&gt;(new String[] {"Luke", "Leia", "Yoda"});
   *
   * // these assertions succeed:
   * assertThat(starWarsCharacterNames).satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Y")) // matches only "Yoda"
   *                                   .satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Lu")) // matches only "Luke"
   *                                   .satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Le")); // matches only "Leia"
   *
   * // this assertion fails because the requirements are satisfied two times
   * assertThat(starWarsCharacterNames).satisfiesOnlyOnce(name -&gt; assertThat(name).contains("a")); // matches "Leia" and "Yoda"
   *
   * // this assertion fails because no element contains "Han"
   * assertThat(starWarsCharacterNames).satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Han"));</code></pre>
   *
   * @param requirements the {@link Consumer} that is expected to be satisfied only once by the elements of the given {@code Iterable}.
   * @return this assertion object.
   * @throws NullPointerException if the given requirements are {@code null}.
   * @throws AssertionError if the requirements are not satisfied only once
   * @since 3.24.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> satisfiesOnlyOnce(Consumer<? super T> requirements) {
    return satisfiesOnlyOnceForProxy(requirements);
  }

  /**
   * Verifies that there is exactly one element in the {@link AtomicReferenceArray} under test that satisfies the {@link ThrowingConsumer}.
   * <p>
   * Examples:
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; starWarsCharacterNames = new AtomicReferenceArray&lt;&gt;(new String[] {"Luke", "Leia", "Yoda"});
   *
   * // these assertions succeed:
   * assertThat(starWarsCharacterNames).satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Y")) // matches only "Yoda"
   *                                   .satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Lu")) // matches only "Luke"
   *                                   .satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Le")); // matches only "Leia"
   *
   * // this assertion fails because the requirements are satisfied two times
   * assertThat(starWarsCharacterNames).satisfiesOnlyOnce(name -&gt; assertThat(name).contains("a")); // matches "Leia" and "Yoda"
   *
   * // this assertion fails because no element contains "Han"
   * assertThat(starWarsCharacterNames).satisfiesOnlyOnce(name -&gt; assertThat(name).contains("Han"));</code></pre>
   *
   * @param requirements the {@link ThrowingConsumer} that is expected to be satisfied only once by the elements of the given {@code Iterable}.
   * @return this assertion object.
   * @throws NullPointerException if the given requirements are {@code null}.
   * @throws RuntimeException rethrown as is by the given {@link ThrowingConsumer} or wrapping any {@link Throwable}.    
   * @throws AssertionError if the requirements are not satisfied only once
   * @since 3.24.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> satisfiesOnlyOnce(ThrowingConsumer<? super T> requirements) {
    return satisfiesOnlyOnceForProxy(requirements);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> satisfiesOnlyOnceForProxy(Consumer<? super T> requirements) {
    iterables.assertSatisfiesOnlyOnce(info, newArrayList(array), requirements);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains at least one of the given values.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertions will pass
   * assertThat(abc).containsAnyOf("b")
   *                .containsAnyOf("b", "c")
   *                .containsAnyOf("a", "b", "c")
   *                .containsAnyOf("a", "b", "c", "d")
   *                .containsAnyOf("e", "f", "g", "b");
   *
   * // assertions will fail
   * assertThat(abc).containsAnyOf("d");
   * assertThat(abc).containsAnyOf("d", "e", "f", "g");</code></pre>
   *
   * @param values the values whose at least one which is expected to be in the {@code AtomicReferenceArray} under test.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the array of values is {@code null}.
   * @throws IllegalArgumentException if the array of values is empty and the {@code AtomicReferenceArray} under test is not empty.
   * @throws AssertionError if the {@code AtomicReferenceArray} under test is {@code null}.
   * @throws AssertionError if the {@code AtomicReferenceArray} under test does not contain any of the given {@code values}.
   * @since 2.9.0 / 3.9.0
   */
  @Override
  @SafeVarargs
  public final AtomicReferenceArrayAssert<T> containsAnyOf(T... values) {
    return containsAnyOfForProxy(values);
  }

  // This method is protected in order to be proxied for SoftAssertions / Assumptions.
  // The public method for it (the one not ending with "ForProxy") is marked as final and annotated with @SafeVarargs
  // in order to avoid compiler warning in user code
  protected AtomicReferenceArrayAssert<T> containsAnyOfForProxy(T[] values) {
    arrays.assertContainsAnyOf(info, array, values);
    return myself;
  }

  /**
   * Verifies that the actual AtomicReferenceArray contains at least one of the given {@link Iterable} elements.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "c"});
   *
   * // assertions will pass
   * assertThat(abc).containsAnyElementsOf(Arrays.asList("b"))
   *                .containsAnyElementsOf(Arrays.asList("b", "c"))
   *                .containsAnyElementsOf(Arrays.asList("a", "b", "c"))
   *                .containsAnyElementsOf(Arrays.asList("a", "b", "c", "d"))
   *                .containsAnyElementsOf(Arrays.asList("e", "f", "g", "b"));
   *
   * // assertions will fail
   * assertThat(abc).containsAnyElementsOf(Arrays.asList("d"));
   * assertThat(abc).containsAnyElementsOf(Arrays.asList("d", "e", "f", "g"));</code></pre>
   *
   * @param iterable the iterable whose at least one element is expected to be in the {@code AtomicReferenceArray} under test.
   * @return {@code this} assertion object.
   * @throws NullPointerException if the iterable of expected values is {@code null}.
   * @throws IllegalArgumentException if the iterable of expected values is empty and the {@code AtomicReferenceArray} under test is not empty.
   * @throws AssertionError if the {@code AtomicReferenceArray} under test is {@code null}.
   * @throws AssertionError if the {@code AtomicReferenceArray} under test does not contain any of elements from the given {@code Iterable}.
   * @since 2.9.0 / 3.9.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> containsAnyElementsOf(Iterable<? extends T> iterable) {
    return containsAnyOf(toArray(iterable));
  }

  /**
   * Verifies that no elements match the given {@link Predicate}.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * // assertion will pass
   * assertThat(abcc).noneMatch(s -&gt; s.isEmpty());
   *
   * // assertion will fail
   * assertThat(abcc).noneMatch(s -&gt; s.length() == 2);</code></pre>
   *
   * Note that you can achieve the same result with {@link #areNot(Condition) areNot(Condition)}
   * or {@link #doNotHave(Condition) doNotHave(Condition)}.
   *
   * @param predicate the given {@link Predicate}.
   * @return {@code this} object.
   * @throws NullPointerException if the given predicate is {@code null}.
   * @throws AssertionError if an element cannot be cast to T.
   * @throws AssertionError if any element satisfy the given predicate.
   * @since 3.9.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> noneMatch(Predicate<? super T> predicate) {
    iterables.assertNoneMatch(info, newArrayList(array), predicate, PredicateDescription.GIVEN);
    return myself;
  }

  /**
   * Verifies that no elements match the given {@link Predicate}. The predicate description is used
   * to get an informative error message.
   * <p>
   * Example :
   * <pre><code class='java'> AtomicReferenceArray&lt;String&gt; abcc = new AtomicReferenceArray&lt;&gt;(new String[]{"a", "b", "cc"});
   *
   * // assertion will pass
   * assertThat(abcc).noneMatch(s -&gt; s.isEmpty(), "is empty");
   *
   * // assertion will fail
   * assertThat(abcc).noneMatch(s -&gt; s.length() == 2, "length of 2");</code></pre>
   *
   * The message of the failed assertion would be:
   * <pre><code class='java'>Expecting no elements of:
   *  &lt;["a", "b", "cc"]&gt;
   *  to match 'length of 2' predicate but this element did:
   *  &lt;"cc"&gt;</code></pre>
   *
   * @param predicate the given {@link Predicate}.
   * @param predicateDescription a description of the {@link Predicate} used in the error message
   * @return {@code this} object.
   * @throws NullPointerException if the given predicate is {@code null}.
   * @throws AssertionError if any elements satisfy the given predicate.
   * @since 3.27.0
   */
  @Override
  public AtomicReferenceArrayAssert<T> noneMatch(Predicate<? super T> predicate, String predicateDescription) {
    iterables.assertNoneMatch(info, newArrayList(array), predicate, new PredicateDescription(predicateDescription));
    return myself;
  }

  // lazy init TypeComparators
  protected TypeComparators getComparatorsByType() {
    if (comparatorsByType == null) comparatorsByType = defaultTypeComparators();
    return comparatorsByType;
  }

  // lazy init TypeComparators
  protected TypeComparators getComparatorsForElementPropertyOrFieldTypes() {
    if (comparatorsForElementPropertyOrFieldTypes == null) comparatorsForElementPropertyOrFieldTypes = defaultTypeComparators();
    return comparatorsForElementPropertyOrFieldTypes;
  }

  private AtomicReferenceArrayAssert<T> internalFilteredOn(String propertyOrFieldName, Object expectedValue) {
    Iterable<? extends T> filteredIterable = filter(array).with(propertyOrFieldName, expectedValue).get();
    return new AtomicReferenceArrayAssert<>(new AtomicReferenceArray<>(toArray(filteredIterable)));
  }

  private AtomicReferenceArrayAssert<T> internalFilteredOn(Predicate<? super T> predicate) {
    checkArgument(predicate != null, "The filter predicate should not be null");
    List<T> filteredList = stream(array).filter(predicate).collect(toList());
    return new AtomicReferenceArrayAssert<>(new AtomicReferenceArray<>(toArray(filteredList)));
  }

}
