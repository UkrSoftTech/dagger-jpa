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

package com.github.x3333.dagger.jpa;

import static java.lang.Boolean.TRUE;

import com.github.x3333.dagger.aop.MethodInterceptor;
import com.github.x3333.dagger.aop.MethodInvocation;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make a intercepted method transactional using a {@link JpaService}.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public final class TransactionalInterceptor implements MethodInterceptor {

  private final Logger logger = LoggerFactory.getLogger(TransactionalInterceptor.class);

  private final JpaWork work;
  private final ThreadLocal<Boolean> shouldClose = new ThreadLocal<>();

  //

  @Inject
  public TransactionalInterceptor(final JpaWork work) {
    this.work = work;
  }

  //

  @Override
  @SuppressWarnings("unchecked")
  public <T> T invoke(final MethodInvocation invocation) throws Throwable {
    if (!work.hasBegun()) {
      work.begin();
      shouldClose.set(true);
    }

    final EntityManager em = work.getEntityManager();
    final EntityTransaction transaction = em.getTransaction();

    // If there is an active transaction, join.
    if (transaction.isActive()) {
      logger.trace("Active transaction in place");
      return (T) invocation.proceed();
    }

    transaction.begin();
    logger.trace("Transaction begun");

    final T result;
    try {
      logger.trace("Invoking");
      result = (T) invocation.proceed();
    } catch (final Exception e) {
      final boolean rollback = doRollback(transaction, e, invocation.annotation(Transactional.class));
      if (rollback) {
        logger.trace("Reverting", e);
        transaction.rollback();
      } else {
        logger.trace("Committing", e);
        transaction.commit();
      }
      throw e; // Continue exception flow
    } finally {
      // Close the EM in case we started work and transaction is not active anymore.
      if (TRUE.equals(shouldClose.get()) && !transaction.isActive()) {
        shouldClose.remove();
        work.end();
      }
    }

    try {
      logger.trace("Committing");
      transaction.commit();
    } finally {
      // Close the EM if we begin the work
      if (TRUE.equals(shouldClose.get())) {
        shouldClose.remove();
        work.end();
      }
    }

    return result;
  }

  private boolean doRollback(//
      final EntityTransaction transaction, //
      final Exception e, //
      final Transactional transactional) {
    for (final Class<? extends Exception> rollbackException : transactional.rollbackOn()) {
      if (rollbackException.isInstance(e)) {
        return true;
      }
    }
    return false;
  }

}
