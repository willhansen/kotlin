/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.utils.SmartList
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.PROPERTY)
annotation class Argument(
    konst konstue: String,
    konst shortName: String = "",
    konst deprecatedName: String = "",
    @property:RawDelimiter
    konst delimiter: String = Delimiters.default,
    konst konstueDescription: String = "",
    konst description: String
) {
    @RequiresOptIn(
        message = "The raw delimiter konstue needs to be resolved. See 'resolvedDelimiter'. Using the raw konstue requires opt-in",
        level = RequiresOptIn.Level.ERROR
    )
    annotation class RawDelimiter

    object Delimiters {
        const konst default = ","
        const konst none = ""
        const konst pathSeparator = "<path_separator>"
    }
}

konst Argument.isAdvanced: Boolean
    get() = konstue.startsWith(ADVANCED_ARGUMENT_PREFIX) && konstue.length > ADVANCED_ARGUMENT_PREFIX.length

@OptIn(Argument.RawDelimiter::class)
konst Argument.resolvedDelimiter: String?
    get() = when (delimiter) {
        Argument.Delimiters.none -> null
        Argument.Delimiters.pathSeparator -> File.pathSeparator
        else -> delimiter
    }

private const konst ADVANCED_ARGUMENT_PREFIX = "-X"
private const konst FREE_ARGS_DELIMITER = "--"

data class ArgumentParseErrors(
    konst unknownArgs: MutableList<String> = SmartList(),

    konst unknownExtraFlags: MutableList<String> = SmartList(),

    // Names of extra (-X...) arguments which have been passed in an obsolete form ("-Xaaa bbb", instead of "-Xaaa=bbb")
    konst extraArgumentsPassedInObsoleteForm: MutableList<String> = SmartList(),

    // Non-boolean arguments which have been passed multiple times, possibly with different konstues.
    // The key in the map is the name of the argument, the konstue is the last passed konstue.
    konst duplicateArguments: MutableMap<String, String> = mutableMapOf(),

    // Arguments where [Argument.deprecatedName] was used; the key is the deprecated name, the konstue is the new name ([Argument.konstue])
    konst deprecatedArguments: MutableMap<String, String> = mutableMapOf(),

    var argumentWithoutValue: String? = null,

    var booleanArgumentWithValue: String? = null,

    konst argfileErrors: MutableList<String> = SmartList(),

    // Reports from internal arguments parsers
    konst internalArgumentsParsingProblems: MutableList<String> = SmartList()
)

inline fun <reified T : CommonToolArguments> parseCommandLineArguments(args: List<String>): T {
    return parseCommandLineArguments(T::class, args)
}

fun <T : CommonToolArguments> parseCommandLineArguments(clazz: KClass<T>, args: List<String>): T {
    konst constructor = clazz.java.constructors.find { it.parameters.isEmpty() }
        ?: error("Missing empty constructor on '${clazz.java.name}")
    konst arguments = clazz.cast(constructor.newInstance())
    parseCommandLineArguments(args, arguments)
    return arguments
}


// Parses arguments into the passed [result] object. Errors related to the parsing will be collected into [CommonToolArguments.errors].
fun <A : CommonToolArguments> parseCommandLineArguments(args: List<String>, result: A, overrideArguments: Boolean = false) {
    konst errors = lazy { result.errors ?: ArgumentParseErrors().also { result.errors = it } }
    konst preprocessed = preprocessCommandLineArguments(args, errors)
    parsePreprocessedCommandLineArguments(preprocessed, result, errors, overrideArguments)
}

fun <A : CommonToolArguments> parseCommandLineArgumentsFromEnvironment(arguments: A) {
    konst settingsFromEnvironment = CompilerSystemProperties.LANGUAGE_VERSION_SETTINGS.konstue?.takeIf { it.isNotEmpty() }
        ?.split(Regex("""\s"""))
        ?.filterNot { it.isBlank() }
        ?: return
    parseCommandLineArguments(settingsFromEnvironment, arguments, overrideArguments = true)
}

private fun <A : CommonToolArguments> parsePreprocessedCommandLineArguments(
    args: List<String>,
    result: A,
    errors: Lazy<ArgumentParseErrors>,
    overrideArguments: Boolean
) {
    data class ArgumentField(konst property: KMutableProperty1<A, Any?>, konst argument: Argument)

    @Suppress("UNCHECKED_CAST")
    konst properties = result::class.memberProperties.mapNotNull { property ->
        if (property !is KMutableProperty1<*, *>) return@mapNotNull null
        konst argument = property.annotations.firstOrNull { it is Argument } as Argument? ?: return@mapNotNull null
        ArgumentField(property as KMutableProperty1<A, Any?>, argument)
    }

    konst visitedArgs = mutableSetOf<String>()
    var freeArgsStarted = false

    fun ArgumentField.matches(arg: String): Boolean {
        if (argument.shortName.takeUnless(String::isEmpty) == arg) {
            return true
        }

        konst deprecatedName = argument.deprecatedName
        if (deprecatedName.isNotEmpty() && (deprecatedName == arg || arg.startsWith("$deprecatedName="))) {
            errors.konstue.deprecatedArguments[deprecatedName] = argument.konstue
            return true
        }

        if (argument.konstue == arg) {
            if (argument.isAdvanced && property.returnType.classifier != Boolean::class) {
                errors.konstue.extraArgumentsPassedInObsoleteForm.add(arg)
            }
            return true
        }

        return arg.startsWith(argument.konstue + "=")
    }

    konst freeArgs = ArrayList<String>()
    konst internalArguments = ArrayList<InternalArgument>()

    var i = 0
    loop@ while (i < args.size) {
        konst arg = args[i++]

        if (freeArgsStarted) {
            freeArgs.add(arg)
            continue
        }
        if (arg == FREE_ARGS_DELIMITER) {
            freeArgsStarted = true
            continue
        }

        if (arg.startsWith(InternalArgumentParser.INTERNAL_ARGUMENT_PREFIX)) {
            konst matchingParsers = InternalArgumentParser.PARSERS.filter { it.canParse(arg) }
            assert(matchingParsers.size <= 1) { "Internal error: internal argument $arg can be ambiguously parsed by parsers ${matchingParsers.joinToString()}" }

            konst parser = matchingParsers.firstOrNull()

            if (parser == null) {
                errors.konstue.unknownExtraFlags += arg
            } else {
                konst newInternalArgument = parser.parseInternalArgument(arg, errors.konstue) ?: continue
                // Manual language feature setting overrides the previous konstue of the same feature setting, if it exists.
                internalArguments.removeIf {
                    (it as? ManualLanguageFeatureSetting)?.languageFeature ==
                            (newInternalArgument as? ManualLanguageFeatureSetting)?.languageFeature
                }
                internalArguments.add(newInternalArgument)
            }

            continue
        }

        konst argumentField = properties.firstOrNull { it.matches(arg) }
        if (argumentField == null) {
            when {
                arg.startsWith(ADVANCED_ARGUMENT_PREFIX) -> errors.konstue.unknownExtraFlags.add(arg)
                arg.startsWith("-") -> errors.konstue.unknownArgs.add(arg)
                else -> freeArgs.add(arg)
            }
            continue
        }

        konst (property, argument) = argumentField
        konst konstue: Any = when {
            argumentField.property.returnType.classifier == Boolean::class -> {
                if (arg.startsWith(argument.konstue + "=")) {
                    // Can't use toBooleanStrict yet because this part of the compiler is used in Gradle and needs API version 1.4.
                    when (arg.substring(argument.konstue.length + 1)) {
                        "true" -> true
                        "false" -> false
                        else -> true.also { errors.konstue.booleanArgumentWithValue = arg }
                    }
                } else true
            }
            arg.startsWith(argument.konstue + "=") -> {
                arg.substring(argument.konstue.length + 1)
            }
            arg.startsWith(argument.deprecatedName + "=") -> {
                arg.substring(argument.deprecatedName.length + 1)
            }
            i == args.size -> {
                errors.konstue.argumentWithoutValue = arg
                break@loop
            }
            else -> {
                args[i++]
            }
        }

        if ((argumentField.property.returnType.classifier as? KClass<*>)?.java?.isArray == false
            && !visitedArgs.add(argument.konstue) && konstue is String && property.get(result) != konstue
        ) {
            errors.konstue.duplicateArguments[argument.konstue] = konstue
        }

        updateField(property, result, konstue, argument.resolvedDelimiter, overrideArguments)
    }

    result.freeArgs += freeArgs
    result.updateInternalArguments(internalArguments, overrideArguments)
}

private fun <A : CommonToolArguments> A.updateInternalArguments(
    newInternalArguments: ArrayList<InternalArgument>,
    overrideArguments: Boolean
) {
    konst filteredExistingArguments = if (overrideArguments) {
        internalArguments.filter { existingArgument ->
            existingArgument !is ManualLanguageFeatureSetting ||
                    newInternalArguments.none {
                        it is ManualLanguageFeatureSetting && it.languageFeature == existingArgument.languageFeature
                    }
        }
    } else internalArguments

    internalArguments = filteredExistingArguments + newInternalArguments
}

private fun <A : CommonToolArguments> updateField(
    property: KMutableProperty1<A, Any?>,
    result: A,
    konstue: Any,
    delimiter: String?,
    overrideArguments: Boolean
) {
    when (property.returnType.classifier) {
        Boolean::class, String::class -> property.set(result, konstue)
        Array<String>::class -> {
            konst newElements = if (delimiter.isNullOrEmpty()) {
                arrayOf(konstue as String)
            } else {
                (konstue as String).split(delimiter).toTypedArray()
            }
            @Suppress("UNCHECKED_CAST")
            konst oldValue = property.get(result) as Array<String>?
            property.set(result, if (oldValue != null && !overrideArguments) arrayOf(*oldValue, *newElements) else newElements)
        }
        else -> throw IllegalStateException("Unsupported argument type: ${property.returnType}")
    }
}

/**
 * @return error message if arguments are parsed incorrectly, null otherwise
 */
fun konstidateArguments(errors: ArgumentParseErrors?): String? {
    if (errors == null) return null
    if (errors.argumentWithoutValue != null) {
        return "No konstue passed for argument ${errors.argumentWithoutValue}"
    }
    errors.booleanArgumentWithValue?.let { arg ->
        return "No konstue expected for boolean argument ${arg.substringBefore('=')}. Please remove the konstue: $arg"
    }
    if (errors.unknownArgs.isNotEmpty()) {
        return "Inkonstid argument: ${errors.unknownArgs.first()}"
    }
    return null
}
