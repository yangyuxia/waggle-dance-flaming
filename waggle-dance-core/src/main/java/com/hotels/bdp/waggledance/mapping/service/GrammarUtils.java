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
package com.hotels.bdp.waggledance.mapping.service;

import static org.apache.hadoop.hive.metastore.utils.MetaStoreUtils.CATALOG_DB_SEPARATOR;
import static org.apache.hadoop.hive.metastore.utils.MetaStoreUtils.CATALOG_DB_THRIFT_NAME_MARKER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.metastore.Warehouse;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public final class GrammarUtils {

  private static final String OR_SEPARATOR = "|";
  private static final Splitter OR_SPLITTER = Splitter.on(OR_SEPARATOR);
  private static final Joiner OR_JOINER = Joiner.on(OR_SEPARATOR);
  private final static String MATCH_ALL = "*";

  private static String DEFAULT_CAT_NAME = StringUtils.join(String.valueOf(CATALOG_DB_THRIFT_NAME_MARKER),
          Warehouse.DEFAULT_CATALOG_NAME, CATALOG_DB_SEPARATOR);

  private GrammarUtils() {}

  @VisibleForTesting
  static String[] splitPattern(String prefix, String pattern) {
    if (pattern.startsWith(prefix)) {
      return new String[] { prefix, pattern.substring(prefix.length()) };
    }

    // Find the longest sub-pattern that matches the prefix
    String subPattern = pattern;
    int index = pattern.length();
    while (index >= 0) {
      String subPatternRegex = subPattern.replaceAll("\\*", ".*");
      if (prefix.matches(subPatternRegex)) {
        if (subPattern.endsWith("*")) {
          // * is a multi character match so belongs to prefix and pattern.
          return new String[] { subPattern, pattern.substring(subPattern.length() - 1) };
        }
        // Dot is a one character x match so can't belong to the pattern anymore.
        return new String[] { subPattern, pattern.substring(subPattern.length()) };
      }
      // Skip last * or . and find the next sub-pattern
      if (subPattern.endsWith("*") || subPattern.endsWith(".")) {
        subPattern = subPattern.substring(0, subPattern.length() - 1);
      }
      int lastStar = subPattern.lastIndexOf('*');
      int lastDot = subPattern.lastIndexOf('.');
      if (lastStar > lastDot) {
        index = lastStar;
        if (lastStar >= 0) {
          subPattern = subPattern.substring(0, index + 1);
        }
      } else {
        index = lastDot;
        if (lastDot >= 0) {
          subPattern = subPattern.substring(0, subPattern.length() - 1);
        }
      }
    }
    return new String[] {};
  }

  /**
   * Selects Waggle Dance database mappings that can potentially match the provided pattern.
   * <p>
   * This implementation is using {@link org.apache.hadoop.hive.metastore.ObjectStore#getDatabases(String)} as reference
   * for pattern matching.
   * <p>
   * To learn more about Hive DDL patterns refer to
   * <a href="https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-Show">Language
   * Manual</a> for details
   *
   * @param prefixes Federation prefixes
   * @param dbPatterns Database name patterns
   * @return A map of possible database prefixes to be used for interrogation with their pattern
   */
  public static Map<String, String> selectMatchingPrefixes(Set<String> prefixes, String dbPatterns) {
    Map<String, String> matchingPrefixes = new HashMap<>();
    if ((dbPatterns == null) || MATCH_ALL.equals(dbPatterns) || StringUtils.equalsIgnoreCase(DEFAULT_CAT_NAME, dbPatterns)) {
      for (String prefix : prefixes) {
        matchingPrefixes.put(prefix, dbPatterns);
      }
      return matchingPrefixes;
    }

    dbPatterns = removeCatName(dbPatterns);

    Map<String, List<String>> prefixPatterns = new HashMap<>();
    for (String subPattern : OR_SPLITTER.split(dbPatterns)) {
      for (String prefix : prefixes) {
        String[] subPatternParts = splitPattern(prefix, subPattern);
        if (subPatternParts.length == 0) {
          continue;
        }
        List<String> prefixPatternList = prefixPatterns.computeIfAbsent(prefix, k -> new ArrayList<>());
        prefixPatternList.add(subPatternParts[1]);
      }
    }

    for (Entry<String, List<String>> prefixPatternEntry : prefixPatterns.entrySet()) {
      matchingPrefixes.put(prefixPatternEntry.getKey(), OR_JOINER.join(prefixPatternEntry.getValue()));
    }
    return matchingPrefixes;
  }

  public static String removeCatName(String dbPatterns) {
    if(StringUtils.containsIgnoreCase(dbPatterns, DEFAULT_CAT_NAME)) {
      dbPatterns = StringUtils.removeIgnoreCase(dbPatterns, DEFAULT_CAT_NAME);
    }
    if(StringUtils.startsWithIgnoreCase(dbPatterns, String.valueOf(CATALOG_DB_THRIFT_NAME_MARKER))) {
      dbPatterns = StringUtils.removeIgnoreCase(dbPatterns, String.valueOf(CATALOG_DB_THRIFT_NAME_MARKER));
    }
    if(StringUtils.endsWithIgnoreCase(dbPatterns, CATALOG_DB_SEPARATOR)) {
      dbPatterns = StringUtils.removeIgnoreCase(dbPatterns, CATALOG_DB_SEPARATOR);
    }
    return StringUtils.isNotBlank(dbPatterns) ? dbPatterns : DEFAULT_CAT_NAME;
   }

}
