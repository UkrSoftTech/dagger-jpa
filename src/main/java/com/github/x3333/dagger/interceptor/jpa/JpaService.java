/*
 * Copyright (C) 2016 Tercio Gaudencio Filho
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.github.x3333.dagger.interceptor.jpa;

import javax.inject.Provider;
import javax.persistence.EntityManager;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public interface JpaService extends Provider<EntityManager>, JpaWork {

  /**
   * Starts the underlying persistence engine and makes JpaService ready for use. For instance, it creates an EntityManagerFactory and may
   * open connection pools. This method must be called by your code prior to using any JPA artifacts. If already started, calling this
   * method does nothing, if already stopped, it starts it again.
   */
  void start();

  /**
   * Tells if this JpaService is already started.
   * 
   * @return true if already started, false otherwise.
   */
  boolean isStarted();

  /**
   * Stops the underlying persistence engine. For instance, it closes the {@code EntityManagerFactory}. If already stopped or not yet
   * started, calling this method does nothing.
   */
  void stop();

  /**
   * Provides a EntityManager instance.
   */
  @Override
  EntityManager get();

}