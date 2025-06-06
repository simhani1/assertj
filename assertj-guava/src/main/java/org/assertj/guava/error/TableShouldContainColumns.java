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
package org.assertj.guava.error;

import java.util.Set;

import org.assertj.core.error.BasicErrorMessageFactory;
import org.assertj.core.error.ErrorMessageFactory;

/**
 * @author Jan Gorman
 */
public class TableShouldContainColumns extends BasicErrorMessageFactory {

  public static ErrorMessageFactory tableShouldContainColumns(Object actual, Object[] columns, Set<?> columnsNotFound) {
    return columns.length == 1 ? new TableShouldContainColumns(actual, columns[0])
        : new TableShouldContainColumns(actual, columns,
                                        columnsNotFound);
  }

  private TableShouldContainColumns(Object actual, Object row) {
    super("%nExpecting:%n  %s%nto contain column:%n  %s", actual, row);
  }

  public TableShouldContainColumns(Object actual, Object[] rows, Set<?> columnsNotFound) {
    super("%nExpecting:%n  %s%nto contain columns:%n  %s%nbut could not find:%n  %s", actual, rows, columnsNotFound);
  }
}
