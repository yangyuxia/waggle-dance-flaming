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
package com.hotels.bdp.waggledance.server.security;

import com.hotels.bdp.waggledance.server.WaggleDanceServerException;

/**
 * Thrown when Waggle Dance encounters and operation that is not allowed by configured {@link AccessControlHandler}
 */
public class NotAllowedException extends WaggleDanceServerException {
  private static final long serialVersionUID = 1L;

  public NotAllowedException(String message) {
    super(message);
  }

}
