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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.EnabledIfOsTests.onLinux;
import static org.junit.jupiter.api.condition.EnabledIfOsTests.onMac;
import static org.junit.jupiter.api.condition.EnabledIfOsTests.onWindows;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.OTHER;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DisabledIfOs}.
 *
 * @since 5.1
 */
class DisabledIfOsTests {

	@Test
	@DisabledIfOs(is = LINUX)
	void linux() {
		assertFalse(onLinux());
	}

	@Test
	@DisabledIfOs(is = MAC)
	void macOs() {
		assertFalse(onMac());
	}

	@Test
	@DisabledOnMac
	void macOsWithComposedAnnotation() {
		assertFalse(onMac());
	}

	@Test
	@DisabledIfOs(is = WINDOWS)
	void windows() {
		assertFalse(onWindows());
	}

	@Test
	@DisabledIfOs(is = OTHER)
	void other() {
		assertTrue(onLinux() || onMac() || onWindows());
	}

	// -------------------------------------------------------------------------

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@DisabledIfOs(is = MAC)
	@interface DisabledOnMac {
	}

}
