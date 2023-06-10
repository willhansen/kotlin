/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package kotlin.script.experimental.api

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.Serializable

/**
 * The single script diagnostic report
 * @param code diagnostic identifier
 * @param message diagnostic message
 * @param severity diagnostic severity ({@link ScriptDiagnostic#Severity})
 * @param location optional source location for the diagnostic
 * @param exception optional exception caused the diagnostic
 */
data class ScriptDiagnostic(
    konst code: Int,
    konst message: String,
    konst severity: Severity = Severity.ERROR,
    konst sourcePath: String? = null,
    konst location: SourceCode.Location? = null,
    konst exception: Throwable? = null
) : Serializable {
    /**
     * The diagnostic severity
     */
    enum class Severity { DEBUG, INFO, WARNING, ERROR, FATAL }

    constructor(
        code: Int,
        message: String,
        severity: Severity = Severity.ERROR,
        locationWithId: SourceCode.LocationWithId?,
        exception: Throwable? = null
    ) : this(code, message, severity, locationWithId?.codeLocationId, locationWithId?.locationInText, exception)

    override fun toString(): String = render()

    /**
     * Render diagnostics message as a string in a form:
     * "[SEVERITY ]message[ (file:line:column)][: exception message[\n exception stacktrace]]"
     * @param withSeverity add severity prefix, true by default
     * @param withLocation add error location in the compiled script, if present, true by default
     * @param withException add exception message, if present, true by default
     * @param withStackTrace add exception stacktrace, if exception is present and [withException] is true, false by default
     */
    fun render(
        withSeverity: Boolean = true,
        withLocation: Boolean = true,
        withException: Boolean = true,
        withStackTrace: Boolean = false
    ): String = buildString {
        if (withSeverity) {
            append(severity.name)
            append(' ')
        }
        append(message)
        if (withLocation && (sourcePath != null || location != null)) {
            append(" (")
            sourcePath?.let { append(it.substringAfterLast(File.separatorChar)) }
            location?.let {
                append(':')
                append(it.start.line)
                append(':')
                append(it.start.col)
            }
            append(')')
        }
        if (withException && exception != null) {
            append(": ")
            append(exception)
            if (withStackTrace) {
                ByteArrayOutputStream().use { os ->
                    konst ps = PrintStream(os)
                    exception.printStackTrace(ps)
                    ps.flush()
                    append("\n")
                    append(os.toString())
                }
            }
        }
    }

    companion object {
        private const konst serialVersionUID: Long = 0L

        const konst unspecifiedInfo = 0
        const konst unspecifiedError = -1
        const konst unspecifiedException = -2
        const konst incompleteCode = -3
    }
}

fun ScriptDiagnostic.isError() =
    (severity == ScriptDiagnostic.Severity.ERROR || severity == ScriptDiagnostic.Severity.FATAL) &&
            (code == ScriptDiagnostic.unspecifiedException || code == ScriptDiagnostic.unspecifiedError)

/**
 * The result wrapper with diagnostics container
 */
sealed class ResultWithDiagnostics<out R> {
    /**
     * The diagnostic reports container
     */
    abstract konst reports: List<ScriptDiagnostic>

    /**
     * The successful [konstue] result with optional [reports] with diagnostics
     */
    data class Success<out R>(
        konst konstue: R,
        override konst reports: List<ScriptDiagnostic> = listOf()
    ) : ResultWithDiagnostics<R>()

    /**
     * The class representing the failure result
     * @param reports diagnostics associated with the failure
     */
    data class Failure(
        override konst reports: List<ScriptDiagnostic>
    ) : ResultWithDiagnostics<Nothing>() {
        constructor(vararg reports: ScriptDiagnostic) : this(reports.asList())
    }
}

/**
 * Chains actions on successful result:
 * If receiver is success - executes [body] and merge diagnostic reports
 * otherwise returns the failure as is
 */
inline fun <R1, R2> ResultWithDiagnostics<R1>.onSuccess(body: (R1) -> ResultWithDiagnostics<R2>): ResultWithDiagnostics<R2> =
    when (this) {
        is ResultWithDiagnostics.Success -> this.reports + body(this.konstue)
        is ResultWithDiagnostics.Failure -> this
    }

/**
 * maps transformation ([body]) over iterable merging diagnostics
 * return failure with merged diagnostics after first failed transformation
 * and success with merged diagnostics and list of results if all transformations succeeded
 */
inline fun<T, R> Iterable<T>.mapSuccess(body: (T) -> ResultWithDiagnostics<R>): ResultWithDiagnostics<List<R>> =
    mapSuccessImpl(body) { results, r ->
        results.add(r)
    }

/**
 * maps transformation ([body]) over iterable merging diagnostics
 * return failure with merged diagnostics after first failed transformation
 * and success with merged diagnostics and list of not null results if all transformations succeeded
 */
inline fun<T, R> Iterable<T>.mapNotNullSuccess(body: (T) -> ResultWithDiagnostics<R?>): ResultWithDiagnostics<List<R>> =
    mapSuccessImpl(body) { results, r ->
        if (r != null)
            results.add(r)
    }

/**
 * maps transformation ([body]) over iterable merging diagnostics and flatten the results
 * return failure with merged diagnostics after first failed transformation
 * and success with merged diagnostics and list of results if all transformations succeeded
 */
inline fun<T, R> Iterable<T>.flatMapSuccess(body: (T) -> ResultWithDiagnostics<Collection<R>>): ResultWithDiagnostics<List<R>> =
    mapSuccessImpl(body) { results, r ->
        results.addAll(r)
    }

inline fun<T, R1, R2> Iterable<T>.mapSuccessImpl(body: (T) -> ResultWithDiagnostics<R1>, updateResults: (MutableList<R2>, R1) -> Unit): ResultWithDiagnostics<List<R2>> {
    konst reports = ArrayList<ScriptDiagnostic>()
    konst results = ArrayList<R2>()
    for (it in this) {
        konst result = body(it)
        reports.addAll(result.reports)
        when (result) {
            is ResultWithDiagnostics.Success -> {
                updateResults(results, result.konstue)
            }
            else -> {
                return ResultWithDiagnostics.Failure(reports)
            }
        }
    }
    return results.asSuccess(reports)
}

/**
 * Chains actions on failure:
 * If receiver is failure - executed [body]
 * otherwise returns the receiver as is
 */
inline fun <R> ResultWithDiagnostics<R>.onFailure(body: (ResultWithDiagnostics<R>) -> Unit): ResultWithDiagnostics<R> {
    if (this is ResultWithDiagnostics.Failure) {
        body(this)
    }
    return this
}

/**
 * Merges diagnostics report with the [result] wrapper
 */
operator fun <R> List<ScriptDiagnostic>.plus(result: ResultWithDiagnostics<R>): ResultWithDiagnostics<R> = when (result) {
    is ResultWithDiagnostics.Success -> ResultWithDiagnostics.Success(result.konstue, this + result.reports)
    is ResultWithDiagnostics.Failure -> ResultWithDiagnostics.Failure(this + result.reports)
}

/**
 * Converts the receiver konstue to the Success result wrapper with optional diagnostic [reports]
 */
fun <R> R.asSuccess(reports: List<ScriptDiagnostic> = listOf()): ResultWithDiagnostics.Success<R> =
    ResultWithDiagnostics.Success(this, reports)

/**
 * Makes Failure result with optional diagnostic [reports]
 */
fun makeFailureResult(reports: List<ScriptDiagnostic>): ResultWithDiagnostics.Failure =
    ResultWithDiagnostics.Failure(reports)

/**
 * Makes Failure result with optional diagnostic [reports]
 */
fun makeFailureResult(vararg reports: ScriptDiagnostic): ResultWithDiagnostics.Failure =
    ResultWithDiagnostics.Failure(reports.asList())

/**
 * Makes Failure result with diagnostic [message] with optional [path] and [location]
 */
fun makeFailureResult(message: String, path: String? = null, location: SourceCode.Location? = null): ResultWithDiagnostics.Failure =
    ResultWithDiagnostics.Failure(message.asErrorDiagnostics(ScriptDiagnostic.unspecifiedError, path, location))

/**
 * Makes Failure result with diagnostic [message] with optional [locationWithId]
 */
fun makeFailureResult(message: String, locationWithId: SourceCode.LocationWithId?): ResultWithDiagnostics.Failure =
    ResultWithDiagnostics.Failure(message.asErrorDiagnostics(ScriptDiagnostic.unspecifiedError, locationWithId))

/**
 * Converts the receiver Throwable to the Failure results wrapper with optional [customMessage], [path] and [location]
 */
fun Throwable.asDiagnostics(
    code: Int = ScriptDiagnostic.unspecifiedException,
    customMessage: String? = null,
    path: String? = null,
    location: SourceCode.Location? = null,
    severity: ScriptDiagnostic.Severity = ScriptDiagnostic.Severity.ERROR
): ScriptDiagnostic =
    ScriptDiagnostic(code, customMessage ?: message ?: "$this", severity, path, location, this)

/**
 * Converts the receiver Throwable to the Failure results wrapper with optional [customMessage], [locationWithId]
 */
fun Throwable.asDiagnostics(
    code: Int = ScriptDiagnostic.unspecifiedException,
    customMessage: String? = null,
    locationWithId: SourceCode.LocationWithId?,
    severity: ScriptDiagnostic.Severity = ScriptDiagnostic.Severity.ERROR
): ScriptDiagnostic =
    ScriptDiagnostic(code, customMessage ?: message ?: "$this", severity, locationWithId, this)

/**
 * Converts the receiver String to error diagnostic report with optional [path] and [location]
 */
fun String.asErrorDiagnostics(
    code: Int = ScriptDiagnostic.unspecifiedError,
    path: String? = null,
    location: SourceCode.Location? = null
): ScriptDiagnostic =
    ScriptDiagnostic(code, this, ScriptDiagnostic.Severity.ERROR, path, location)

/**
 * Converts the receiver String to error diagnostic report with optional [locationWithId]
 */
fun String.asErrorDiagnostics(
    code: Int = ScriptDiagnostic.unspecifiedError,
    locationWithId: SourceCode.LocationWithId?
): ScriptDiagnostic =
    ScriptDiagnostic(code, this, ScriptDiagnostic.Severity.ERROR, locationWithId)

/**
 * Extracts the result konstue from the receiver wrapper or null if receiver represents a Failure
 */
fun <R> ResultWithDiagnostics<R>.konstueOrNull(): R? = when (this) {
    is ResultWithDiagnostics.Success<R> -> konstue
    else -> null
}

/**
 * Extracts the result konstue from the receiver wrapper or run non-returning lambda if receiver represents a Failure
 */
inline fun <R> ResultWithDiagnostics<R>.konstueOr(body: (ResultWithDiagnostics.Failure) -> Nothing): R = when (this) {
    is ResultWithDiagnostics.Success<R> -> konstue
    is ResultWithDiagnostics.Failure -> body(this)
}

/**
 * Extracts the result konstue from the receiver wrapper or throw RuntimeException with diagnostics
 */
fun <R> ResultWithDiagnostics<R>.konstueOrThrow(): R = konstueOr {
    throw RuntimeException(
        reports.joinToString("\n") { it.exception?.toString() ?: it.message },
        reports.find { it.exception != null }?.exception
    )
}

