/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*

/**
 * Marks the annotated declaration as deprecated.
 *
 * A deprecated API element is not recommended to use, typically because it's being phased out or a better alternative exists.
 *
 * To help removing deprecated API gradually, the property [level] could be used.
 * Usually a gradual phase-out goes through the "warning", then "error", then "hidden" or "removed" stages:
 * - First and by default, [DeprecationLevel.WARNING] is used to notify API consumers, but not to break their compilation or runtime usages.
 * - Then, some time later the deprecation level is raised to [DeprecationLevel.ERROR], so that no new Kotlin code can be compiled
 *   using the deprecated API.
 * - Finally, the API is either removed entirely, or hidden ([DeprecationLevel.HIDDEN]) from code,
 * so its usages look like unresolved references, while the API remains in the compiled code
 * preserving binary compatibility with previously compiled code.
 *
 * @property message The message explaining the deprecation and recommending an alternative API to use.
 * @property replaceWith If present, specifies a code fragment which should be used as a replacement for
 *  the deprecated API usage.
 * @property level Specifies how the deprecated element usages are reported in code.
 *  See the [DeprecationLevel] enum for the possible konstues.
 */
@Target(CLASS, FUNCTION, PROPERTY, ANNOTATION_CLASS, CONSTRUCTOR, PROPERTY_SETTER, PROPERTY_GETTER, TYPEALIAS)
@MustBeDocumented
public annotation class Deprecated(
    konst message: String,
    konst replaceWith: ReplaceWith = ReplaceWith(""),
    konst level: DeprecationLevel = DeprecationLevel.WARNING
)

/**
 * Marks the annotated declaration as deprecated. In contrast to [Deprecated], severity of the reported diagnostic is not a constant konstue,
 * but differs depending on the API version of the usage (the konstue of the `-api-version` argument when compiling the module where
 * the usage is located). If the API version is greater or equal than [hiddenSince], the declaration will not be accessible from the code
 * (as if it was deprecated with level [DeprecationLevel.HIDDEN]), otherwise if the API version is greater or equal than [errorSince],
 * the usage will be marked as an error (as with [DeprecationLevel.ERROR]), otherwise if the API version is greater or equal
 * than [warningSince], the usage will be marked as a warning (as with [DeprecationLevel.WARNING]), otherwise the annotation is ignored.
 *
 * @property warningSince the version, since which this deprecation should be reported as a warning.
 * @property errorSince the version, since which this deprecation should be reported as a error.
 * @property hiddenSince the version, since which the annotated declaration should not be available in code.
 */
@Target(CLASS, FUNCTION, PROPERTY, ANNOTATION_CLASS, CONSTRUCTOR, PROPERTY_SETTER, PROPERTY_GETTER, TYPEALIAS)
@MustBeDocumented
@SinceKotlin("1.4")
public annotation class DeprecatedSinceKotlin(
    konst warningSince: String = "",
    konst errorSince: String = "",
    konst hiddenSince: String = ""
)

/**
 * Specifies a code fragment that can be used to replace a deprecated function, property or class. Tools such
 * as IDEs can automatically apply the replacements specified through this annotation.
 *
 * @property expression the replacement expression. The replacement expression is interpreted in the context
 *     of the symbol being used, and can reference members of enclosing classes etc.
 *     For function calls, the replacement expression may contain argument names of the deprecated function,
 *     which will be substituted with actual parameters used in the call being updated. The imports used in the file
 *     containing the deprecated function or property are NOT accessible; if the replacement expression refers
 *     on any of those imports, they need to be specified explicitly in the [imports] parameter.
 * @property imports the qualified names that need to be imported in order for the references in the
 *     replacement expression to be resolved correctly.
 */
@Target()
@Retention(BINARY)
@MustBeDocumented
public annotation class ReplaceWith(konst expression: String, vararg konst imports: String)

/**
 * Possible levels of a deprecation. The level specifies how the deprecated element usages are reported in code.
 *
 * @see Deprecated
 */
public enum class DeprecationLevel {
    /** Usage of the deprecated element will be reported as a warning. */
    WARNING,
    /** Usage of the deprecated element will be reported as an error. */
    ERROR,
    /** Deprecated element will not be accessible from code. */
    HIDDEN
}

/**
 * Signifies that the annotated functional type represents an extension function.
 */
@Target(TYPE)
@MustBeDocumented
public annotation class ExtensionFunctionType

/**
 * Signifies that the annotated functional type has the prefix of size `count` for context receivers.
 * Thus, `@ContextFunctionTypeParams(2) @ExtensionFunctionType Function4<String, Int, Double, Byte, Unit>` is a normalized representation of
 * `context(String, Int) Double.(Byte) -> Unit`.
 *
 * Just the same as @ExtensionFunctionType, this annotation is not assumed to be used in source code, preferring the explicit function type
 * syntax, like in the example above.
 *
 * There's no need in any additional opt-in limitations because this annotation might only be referenced by users
 * who turned on an experimental `-Xcontext-receivers` compiler flag, for which there are no backward/forward compatibilities guarantees.
 */
@Target(TYPE)
@MustBeDocumented
@SinceKotlin("1.7")
public annotation class ContextFunctionTypeParams(konst count: Int)

/**
 * Annotates type arguments of functional type and holds corresponding parameter name specified by the user in type declaration (if any).
 */
@Target(TYPE)
@MustBeDocumented
@SinceKotlin("1.1")
public annotation class ParameterName(konst name: String)

/**
 * Suppresses the given compilation warnings in the annotated element.
 * @property names names of the compiler diagnostics to suppress.
 */
@Target(CLASS, ANNOTATION_CLASS, TYPE_PARAMETER, PROPERTY, FIELD, LOCAL_VARIABLE, VALUE_PARAMETER,
        CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPE, EXPRESSION, FILE, TYPEALIAS)
@Retention(SOURCE)
public annotation class Suppress(vararg konst names: String)

/**
 * Suppresses errors about variance conflict
 */
@Target(TYPE)
@Retention(SOURCE)
@MustBeDocumented
public annotation class UnsafeVariance

/**
 * Specifies the first version of Kotlin where a declaration has appeared.
 * Using the declaration and specifying an older API version (via the `-api-version` command line option) will result in an error.
 *
 * @property version the version in the following formats: `<major>.<minor>` or `<major>.<minor>.<patch>`, where major, minor and patch
 * are non-negative integer numbers without leading zeros.
 */
@Target(CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class SinceKotlin(konst version: String)

/**
 * When applied to annotation class X specifies that X defines a DSL language
 *
 * The general rule:
 * - an implicit receiver may *belong to a DSL @X* if marked with a corresponding DSL marker annotation
 * - two implicit receivers of the same DSL are not accessible in the same scope
 * - the closest one wins
 * - other available receivers are resolved as usual, but if the resulting resolved call binds to such a receiver, it's a compilation error
 *
 * Marking rules: an implicit receiver is considered marked with @Ann if
 * - its type is marked, or
 * - its type's classifier is marked
 * - or any of its superclasses/superinterfaces
 */
@Target(ANNOTATION_CLASS)
@Retention(BINARY)
@MustBeDocumented
@SinceKotlin("1.1")
public annotation class DslMarker


/**
 * When applied to a class or a member with internal visibility allows to use it from public inline functions and
 * makes it effectively public.
 *
 * Public inline functions cannot use non-public API, since if they are inlined, those non-public API references
 * would violate access restrictions at a call site (https://kotlinlang.org/docs/reference/inline-functions.html#public-inline-restrictions).
 *
 * To overcome this restriction an `internal` declaration can be annotated with the `@PublishedApi` annotation:
 * - this allows to call that declaration from public inline functions;
 * - the declaration becomes effectively public, and this should be considered with respect to binary compatibility maintaining.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@SinceKotlin("1.1")
public annotation class PublishedApi
