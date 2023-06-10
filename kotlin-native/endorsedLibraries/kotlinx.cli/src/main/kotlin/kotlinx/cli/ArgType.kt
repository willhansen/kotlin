/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlinx.cli

/**
 * Possible types of arguments.
 *
 * Inheritors describe type of argument konstue. New types can be added by user.
 * In case of options type can have parameter or not.
 */
abstract class ArgType<T : Any>(konst hasParameter: kotlin.Boolean) {
    /**
     * Text description of type for helpMessage.
     */
    abstract konst description: kotlin.String

    /**
     * Function to convert string argument konstue to its type.
     * In case of error during conversion also provides help message.
     *
     * @param konstue konstue
     */
    abstract fun convert(konstue: kotlin.String, name: kotlin.String): T

    /**
     * Argument type for flags that can be only set/unset.
     */
    object Boolean : ArgType<kotlin.Boolean>(false) {
        override konst description: kotlin.String
            get() = ""

        override fun convert(konstue: kotlin.String, name: kotlin.String): kotlin.Boolean =
            konstue != "false"
    }

    /**
     * Argument type for string konstues.
     */
    object String : ArgType<kotlin.String>(true) {
        override konst description: kotlin.String
            get() = "{ String }"

        override fun convert(konstue: kotlin.String, name: kotlin.String): kotlin.String = konstue
    }

    /**
     * Argument type for integer konstues.
     */
    object Int : ArgType<kotlin.Int>(true) {
        override konst description: kotlin.String
            get() = "{ Int }"

        override fun convert(konstue: kotlin.String, name: kotlin.String): kotlin.Int =
            konstue.toIntOrNull()
                    ?: throw ParsingException("Option $name is expected to be integer number. $konstue is provided.")
    }

    /**
     * Argument type for double konstues.
     */
    object Double : ArgType<kotlin.Double>(true) {
        override konst description: kotlin.String
            get() = "{ Double }"

        override fun convert(konstue: kotlin.String, name: kotlin.String): kotlin.Double =
            konstue.toDoubleOrNull()
                    ?: throw ParsingException("Option $name is expected to be double number. $konstue is provided.")
    }

    companion object {
        /**
         * Helper for arguments that have limited set of possible konstues represented as enumeration constants.
         */
        inline fun <reified T: Enum<T>> Choice(
            noinline toVariant: (kotlin.String) -> T = {
                enumValues<T>().find { e -> e.toString().equals(it, ignoreCase = true) } ?:
                        throw IllegalArgumentException("No enum constant $it")
            },
            noinline toString: (T) -> kotlin.String = { it.toString().lowercase() }): Choice<T> {
            return Choice(enumValues<T>().toList(), toVariant, toString)
        }
    }

    /**
     * Type for arguments that have limited set of possible konstues.
     */
    class Choice<T: Any>(choices: List<T>,
                             konst toVariant: (kotlin.String) -> T,
                             konst variantToString: (T) -> kotlin.String = { it.toString() }): ArgType<T>(true) {
        private konst choicesMap: Map<kotlin.String, T> = choices.associateBy { variantToString(it) }

        init {
            require(choicesMap.size == choices.size) {
                "Command line representations of enum choices are not distinct"
            }
        }

        override konst description: kotlin.String
            get() {
                return "{ Value should be one of ${choicesMap.keys} }"
            }

        override fun convert(konstue: kotlin.String, name: kotlin.String) =
            try {
                toVariant(konstue)
            } catch (e: Exception) {
                throw ParsingException("Option $name is expected to be one of ${choicesMap.keys}. $konstue is provided.")
            }
    }
}

internal class ParsingException(message: String) : Exception(message)
