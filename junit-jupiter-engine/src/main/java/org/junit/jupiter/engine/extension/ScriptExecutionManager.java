/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.jupiter.engine.Constants.Script.Bind.SYSTEM_ENVIRONMENT;
import static org.junit.jupiter.engine.Constants.Script.Bind.SYSTEM_PROPERTY;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.DisabledIf;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

/**
 * Entry point for script execution support.
 *
 * @since 5.1
 */
class ScriptExecutionManager implements CloseableResource {

	private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	private final ConcurrentMap<String, ScriptEngine> scriptEngines = new ConcurrentHashMap<>();
	private final ConcurrentMap<Script, CompiledScript> compiledScripts = new ConcurrentHashMap<>();

	private final ScriptAccessor systemPropertyAccessor = new ScriptAccessor.SystemPropertyAccessor();
	private final ScriptAccessor environmentVariableAccessor = new ScriptAccessor.EnvironmentVariableAccessor();

	// package-private for testing purposes -- make it configurable?
	boolean forceScriptEvaluation = false;

	@Override
	public void close() {
		compiledScripts.clear();
		scriptEngines.clear();
	}

	/**
	 * Evaluate the script using the given bindings.
	 *
	 * @param script the script to evaluate
	 * @param bindings the context-aware bindings
	 * @return the computed condition evaluation result
	 * @throws ScriptException if an error occurs in script.
	 */
	ConditionEvaluationResult evaluate(Script script, Bindings bindings) throws ScriptException {
		// Always look for a compiled script in our cache.
		CompiledScript compiledScript = compiledScripts.get(script);

		// No compiled script found?
		if (compiledScript == null) {
			String source = script.getSource();
			ScriptEngine scriptEngine = scriptEngines.computeIfAbsent(script.getEngine(), this::createScriptEngine);
			if (!(scriptEngine instanceof Compilable) || forceScriptEvaluation) {
				Object result = scriptEngine.eval(source, bindings);
				return computeConditionEvaluationResult(script, result);
			}
			// Compile and store it in our cache. Fall-through for execution
			compiledScript = ((Compilable) scriptEngine).compile(source);
			compiledScripts.putIfAbsent(script, compiledScript);
		}

		// Let the cached compiled script do it's work.
		Object result = compiledScript.eval(bindings);
		return computeConditionEvaluationResult(script, result);
	}

	ConditionEvaluationResult computeConditionEvaluationResult(Script script, Object result) {
		// Trivial case: script returned a custom ConditionEvaluationResult instance.
		if (result instanceof ConditionEvaluationResult) {
			return (ConditionEvaluationResult) result;
		}

		String resultAsString = String.valueOf(result);
		String reason = script.toReasonString(resultAsString);

		// Cast or parse result to a boolean value.
		boolean isTrue;
		if (result instanceof Boolean) {
			isTrue = (Boolean) result;
		}
		else {
			isTrue = Boolean.parseBoolean(resultAsString);
		}

		// Flip enabled/disabled result based on the associated annotation type.
		if (script.getAnnotationType() == EnabledIf.class) {
			return isTrue ? enabled(reason) : disabled(reason);
		}
		if (script.getAnnotationType() == DisabledIf.class) {
			return isTrue ? disabled(reason) : enabled(reason);
		}

		// Still here? Not so good.
		throw new JUnitException("Unsupported annotation type: " + script.getAnnotationType());
	}

	ScriptEngine createScriptEngine(String engine) {
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(engine);
		if (scriptEngine == null) {
			scriptEngine = scriptEngineManager.getEngineByExtension(engine);
		}
		if (scriptEngine == null) {
			scriptEngine = scriptEngineManager.getEngineByMimeType(engine);
		}
		Preconditions.notNull(scriptEngine, () -> "Script engine not found: " + engine);

		Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
		bindings.put(SYSTEM_PROPERTY, systemPropertyAccessor);
		bindings.put(SYSTEM_ENVIRONMENT, environmentVariableAccessor);
		return scriptEngine;
	}

}
