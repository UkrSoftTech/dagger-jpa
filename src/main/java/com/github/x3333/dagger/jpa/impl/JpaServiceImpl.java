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

package com.github.x3333.dagger.jpa.impl;

import static com.google.common.base.Preconditions.checkState;

import com.github.x3333.dagger.jpa.JpaService;

import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Default implementation of {@link JpaService}.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public class JpaServiceImpl implements JpaService {

  protected boolean started = false;

  protected final String persistenceUnitName;
  protected final Map<?, ?> persistenceProperties;

  protected volatile EntityManagerFactory emFactory;
  protected final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();

  @Inject
  public JpaServiceImpl(final String persistenceUnitName, final Map<?, ?> persistenceProperties) {
    this.persistenceUnitName = persistenceUnitName;
    this.persistenceProperties = persistenceProperties;
  }

  @Override
  public void start() {
    if (started) {
      return;
    }

    if (null != persistenceProperties) {
      emFactory = Persistence.createEntityManagerFactory(persistenceUnitName, persistenceProperties);
    } else {
      emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    }

    started = true;
  }

  @Override
  public boolean isStarted() {
    return started;
  }

  @Override
  public void stop() {
    if (!started) {
      return;
    }

    checkState(emFactory.isOpen(), "Persistence service is already shut down!");

    try {
      emFactory.close();
    } finally {
      started = false;
    }
  }

  @Override
  public EntityManager get() {
    if (!hasBegun()) {
      begin();
    }

    return entityManager.get();
  }

  @Override
  public void begin() {
    if (entityManager.get() != null) {
      return;
    }

    entityManager.set(emFactory.createEntityManager());
  }

  @Override
  public void end() {
    final EntityManager em = entityManager.get();
    if (em == null) {
      return;
    }

    try {
      em.close();
    } finally {
      entityManager.remove();
    }
  }

  @Override
  public boolean hasBegun() {
    return entityManager.get() != null;
  }

}
