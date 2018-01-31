/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @since 5.1
 * @see DisabledIfOs
 * @see org.junit.jupiter.api.Disabled
 * @see org.junit.jupiter.api.EnabledIf
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(EnabledIfOs.Condition.class)
@API(status = STABLE, since = "5.1")
public @interface EnabledIfOs {

	OS[] is();

	class Condition implements ExecutionCondition {

		private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@EnabledIfOs is not present");

		static final ConditionEvaluationResult ENABLED_ON_CURRENT_OS = //
			enabled("Enabled on operating system: " + System.getProperty("os.name"));

		static final ConditionEvaluationResult DISABLED_ON_CURRENT_OS = //
			disabled("Disabled on operating system: " + System.getProperty("os.name"));

		@Override
		public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
			Optional<EnabledIfOs> optional = findAnnotation(context.getElement(), EnabledIfOs.class);
			if (optional.isPresent()) {
				return (Arrays.stream(optional.get().is()).anyMatch(OS::isCurrentOs)) ? ENABLED_ON_CURRENT_OS
						: DISABLED_ON_CURRENT_OS;
			}
			return ENABLED_BY_DEFAULT;
		}

	}

}
