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
package com.hotels.bdp.waggledance.mapping.service.impl;

import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;

import lombok.AllArgsConstructor;

import com.hotels.bdp.waggledance.api.model.AbstractMetaStore;
import com.hotels.bdp.waggledance.mapping.model.DatabaseMapping;
import com.hotels.bdp.waggledance.mapping.service.GrammarUtils;
import com.hotels.bdp.waggledance.mapping.service.MappingEventListener;
import com.hotels.bdp.waggledance.mapping.service.PanopticOperationHandler;
import com.hotels.bdp.waggledance.metrics.CurrentMonitoredMetaStoreHolder;

@AllArgsConstructor
public class MonitoredDatabaseMappingService implements MappingEventListener {

  private final MappingEventListener wrapped;

  @Override
  public DatabaseMapping primaryDatabaseMapping() {
    DatabaseMapping primaryDatabaseMapping = wrapped.primaryDatabaseMapping();
    CurrentMonitoredMetaStoreHolder.monitorMetastore(primaryDatabaseMapping.getMetastoreMappingName());
    return primaryDatabaseMapping;
  }

  @Override
  public DatabaseMapping databaseMapping(@NotNull String databaseName) throws NoSuchObjectException {
    databaseName = GrammarUtils.removeCatName(databaseName);
    DatabaseMapping databaseMapping = wrapped.databaseMapping(databaseName);
    CurrentMonitoredMetaStoreHolder.monitorMetastore(databaseMapping.getMetastoreMappingName());
    return databaseMapping;
  }

  @Override
  public void checkTableAllowed(String databaseName, String tableName,
      DatabaseMapping mapping) throws NoSuchObjectException {
    databaseName = GrammarUtils.removeCatName(databaseName);
      wrapped.checkTableAllowed(databaseName, tableName, mapping);
    }

  @Override
  public List<String> filterTables(String databaseName, List<String> tableNames, DatabaseMapping mapping) {
    return wrapped.filterTables(databaseName, tableNames, mapping);
  }

  @Override
  public PanopticOperationHandler getPanopticOperationHandler() {
    PanopticOperationHandler handler = wrapped.getPanopticOperationHandler();
    CurrentMonitoredMetaStoreHolder.monitorMetastore();
    return handler;
  }

  @Override
  public List<DatabaseMapping> getAvailableDatabaseMappings() {
    return wrapped.getAvailableDatabaseMappings();
  }

  @Override
  public List<DatabaseMapping> getAllDatabaseMappings() {
    return wrapped.getAllDatabaseMappings();
  }

  @Override
  public void close() throws IOException {
    wrapped.close();
  }

  @Override
  public void onRegister(AbstractMetaStore federatedMetaStore) {
    wrapped.onRegister(federatedMetaStore);
  }

  @Override
  public void onUnregister(AbstractMetaStore federatedMetaStore) {
    wrapped.onUnregister(federatedMetaStore);
  }

  @Override
  public void onUpdate(AbstractMetaStore oldMetaStore, AbstractMetaStore newMetaStore) {
    wrapped.onUpdate(oldMetaStore, newMetaStore);
  }
}
