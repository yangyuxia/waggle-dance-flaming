/**
 * Copyright (C) 2016-2024 Expedia, Inc.
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
package com.hotels.bdp.waggledance.api.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FederatedMetaStoreTest extends AbstractMetaStoreTest<FederatedMetaStore> {

  public FederatedMetaStoreTest() {
    super(new FederatedMetaStore());
  }

  @Test
  public void testOverride() {
    assertThat(metaStore.getAccessControlType(), is(AccessControlType.READ_ONLY));

    metaStore.setAccessControlType(AccessControlType.READ_AND_WRITE_ON_DATABASE_WHITELIST);
    assertThat(metaStore.getAccessControlType(), is(AccessControlType.READ_AND_WRITE_ON_DATABASE_WHITELIST));
  }

  @Test
  public void testFederationType() {
    assertThat(metaStore.getFederationType(), is(FederationType.FEDERATED));
  }

  @Test
  public void emptyDatabasePrefix() {
    metaStore.setDatabasePrefix("");
    Set<ConstraintViolation<FederatedMetaStore>> violations = validator.validate(metaStore);
    assertThat(violations.size(), is(0));
    assertThat(metaStore.getDatabasePrefix(), is(""));
  }

  @Test
  public void nonEmptyDatabasePrefix() {
    metaStore.setDatabasePrefix("override");
    Set<ConstraintViolation<FederatedMetaStore>> violations = validator.validate(metaStore);
    assertThat(violations.size(), is(0));
    assertThat("override", is(metaStore.getDatabasePrefix()));
  }

  @Test
  public void nullDatabasePrefix() {
    metaStore.setDatabasePrefix(null);
    Set<ConstraintViolation<FederatedMetaStore>> violations = validator.validate(metaStore);
    assertThat(violations.size(), is(0));
    assertThat("name_", is(metaStore.getDatabasePrefix()));
  }

  @Test
  public void toJson() throws Exception {
    String expected = "{\"accessControlType\":\"READ_ONLY\",\"connectionType\":\"DIRECT\",\"databaseNameMapping\":{},\"databasePrefix\":\"name_\",\"federationType\":\"FEDERATED\",\"hiveMetastoreFilterHook\":null,\"impersonationEnabled\":false,\"latency\":0,\"mappedDatabases\":null,\"mappedTables\":null,\"metastoreTunnel\":null,\"name\":\"name\",\"remoteMetaStoreUris\":\"uri\",\"status\":\"UNKNOWN\",\"writableDatabaseWhiteList\":[]}";
    ObjectMapper mapper = new ObjectMapper();
    // Sorting to get deterministic test behaviour
    mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    String json = mapper.writerFor(FederatedMetaStore.class).writeValueAsString(metaStore);
    assertThat(json, is(expected));
  }

  @Test
  public void nonEmptyConstructor() {
    String storeName = "name";
    String storeUri = "remoteMetaStoreUris";
    FederatedMetaStore store = new FederatedMetaStore(storeName, storeUri);
    assertThat(store.getName(), is(storeName));
    assertThat(store.getRemoteMetaStoreUris(), is(storeUri));
  }

}
