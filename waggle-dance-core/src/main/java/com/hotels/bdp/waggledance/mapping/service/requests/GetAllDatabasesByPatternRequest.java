/**
 * Copyright (C) 2016-2023 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.bdp.waggledance.mapping.service.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.thrift.TException;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.hotels.bdp.waggledance.mapping.model.DatabaseMapping;

@AllArgsConstructor
public class GetAllDatabasesByPatternRequest implements RequestCallable<List<String>> {

  @Getter
  private final DatabaseMapping mapping;
  private final String pattern;
  private final BiFunction<String, DatabaseMapping, Boolean> filter;

  @Override
  public List<String> call() throws TException {
    List<String> databases = mapping.getClient().get_databases(pattern);
    List<String> mappedDatabases = new ArrayList<>();
    for (String database : databases) {
      if (filter.apply(database, mapping)) {
        mappedDatabases.addAll(mapping.transformOutboundDatabaseNameMultiple(database));
      }
    }
    return mappedDatabases;
  }
}
