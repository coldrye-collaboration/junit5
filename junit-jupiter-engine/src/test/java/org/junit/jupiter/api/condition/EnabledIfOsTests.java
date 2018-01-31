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
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.OTHER;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnabledIfOs}.
 *
 * @since 5.1
 */
@EnabledIfOs(is = { LINUX, MAC, WINDOWS, OTHER })
class EnabledIfOsTests {

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

	@Test
	@EnabledIfOs(is = LINUX)
	void linux() {
		assertTrue(onLinux());
	}

	@Test
	@EnabledIfOs(is = MAC)
	void macOs() {
		assertTrue(onMac());
	}

	@Test
	@EnabledOnMac
	void macOsWithComposedAnnotation() {
		assertTrue(onMac());
	}

	@Test
	@EnabledIfOs(is = WINDOWS)
	void windows() {
		assertTrue(onWindows());
	}

	@Test
	@EnabledIfOs(is = OTHER)
	void other() {
		assertFalse(onLinux() || onMac() || onWindows());
	}

	static boolean onLinux() {
		return OS_NAME.contains("linux");
	}

	static boolean onMac() {
		return OS_NAME.contains("mac");
	}

	static boolean onWindows() {
		return OS_NAME.contains("windows");
	}

	// -------------------------------------------------------------------------

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@EnabledIfOs(is = MAC)
	@interface EnabledOnMac {
	}

}
