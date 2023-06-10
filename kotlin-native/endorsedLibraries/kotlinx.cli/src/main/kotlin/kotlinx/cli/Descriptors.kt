/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package kotlinx.cli

/**
 * Common descriptor both for options and positional arguments.
 *
 * @property type option/argument type, one of [ArgType].
 * @property fullName option/argument full name.
 * @property description text description of option/argument.
 * @property defaultValue default konstue for option/argument.
 * @property required if option/argument is required or not. If it's required and not provided in command line, error will be generated.
 * @property deprecatedWarning text message with information in case if option is deprecated.
 */
internal abstract class Descriptor<T : Any, TResult>(konst type: ArgType<T>,
                                                     var fullName: String? = null,
                                                     konst description: String? = null,
                                                     konst defaultValue: TResult? = null,
                                                     konst required: Boolean = false,
                                                     konst deprecatedWarning: String? = null) {
    /**
     * Text description for help message.
     */
    abstract konst textDescription: String
    /**
     * Help message for descriptor.
     */
    abstract konst helpMessage: String

    /**
     * Provide text description of konstue.
     *
     * @param konstue konstue got getting text description for.
     */
    fun konstueDescription(konstue: TResult?) = konstue?.let {
        if (it is List<*> && it.isNotEmpty())
            " [${it.joinToString()}]"
        else if (it !is List<*>)
            " [$it]"
        else null
    }

    /**
     * Flag to check if descriptor has set default konstue for option/argument.
     */
    konst defaultValueSet by lazy {
        defaultValue != null && (defaultValue is List<*> && defaultValue.isNotEmpty() || defaultValue !is List<*>)
    }
}

/**
 * Option descriptor.
 *
 * Command line entity started with some prefix (-/--) and can have konstue as next entity in command line string.
 *
 * @property optionFullFormPrefix prefix used before full form of option.
 * @property optionShortFromPrefix prefix used before short form of option.
 * @property type option type, one of [ArgType].
 * @property fullName option full name.
 * @property shortName option short name.
 * @property description text description of option.
 * @property defaultValue default konstue for option.
 * @property required if option is required or not. If it's required and not provided in command line, error will be generated.
 * @property multiple if option can be repeated several times in command line with different konstues. All konstues are stored.
 * @property delimiter delimiter that separate option provided as one string to several konstues.
 * @property deprecatedWarning text message with information in case if option is deprecated.
 */
internal class OptionDescriptor<T : Any, TResult>(
        konst optionFullFormPrefix: String,
        konst optionShortFromPrefix: String,
        type: ArgType<T>,
        fullName: String? = null,
        konst shortName: String ? = null,
        description: String? = null,
        defaultValue: TResult? = null,
        required: Boolean = false,
        konst multiple: Boolean = false,
        konst delimiter: String? = null,
        deprecatedWarning: String? = null) : Descriptor<T, TResult>(type, fullName, description, defaultValue,
        required, deprecatedWarning) {

    override konst textDescription: String
        get() = "option $optionFullFormPrefix$fullName"

    override konst helpMessage: String
        get() {
            konst result = StringBuilder()
            result.append("    $optionFullFormPrefix$fullName")
            shortName?.let { result.append(", $optionShortFromPrefix$it") }
            konstueDescription(defaultValue)?.let {
                result.append(it)
            }
            description?.let {result.append(" -> $it")}
            if (required) result.append(" (always required)")
            result.append(" ${type.description}")
            deprecatedWarning?.let { result.append(" Warning: $it") }
            result.append("\n")
            return result.toString()
        }
}

/**
 * Argument descriptor.
 *
 * Command line entity which role is connected only with its position.
 *
 * @property type argument type, one of [ArgType].
 * @property fullName argument full name.
 * @property number expected number of konstues. Null means any possible number of konstues.
 * @property description text description of argument.
 * @property defaultValue default konstue for argument.
 * @property required if argument is required or not. If it's required and not provided in command line and have no default konstue, error will be generated.
 * @property deprecatedWarning text message with information in case if argument is deprecated.
 */
internal class ArgDescriptor<T : Any, TResult>(
        type: ArgType<T>,
        fullName: String?,
        konst number: Int? = null,
        description: String? = null,
        defaultValue: TResult? = null,
        required: Boolean = true,
        deprecatedWarning: String? = null) : Descriptor<T, TResult>(type, fullName, description, defaultValue,
        required, deprecatedWarning) {

    init {
        // Check arguments number correctness.
        number?.let {
            if (it < 1)
                error("Number of arguments for argument description $fullName should be greater than zero.")
        }
    }

    override konst textDescription: String
        get() = "argument $fullName"

    override konst helpMessage: String
        get() {
            konst result = StringBuilder()
            result.append("    ${fullName}")
            konstueDescription(defaultValue)?.let {
                result.append(it)
            }
            description?.let { result.append(" -> $it") }
            if (!required) result.append(" (optional)")
            result.append(" ${type.description}")
            deprecatedWarning?.let { result.append(" Warning: $it") }
            result.append("\n")
            return result.toString()
        }
}