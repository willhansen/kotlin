/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

import kotlin.internal.InlineOnly

/**
 * Represents hexadecimal format options.
 *
 * To create a new [HexFormat] use `HexFormat` function.
 */
@ExperimentalStdlibApi
@SinceKotlin("1.9")
public class HexFormat internal constructor(
    /**
     * Specifies whether upper case hexadecimal digits `0-9`, `A-F` should be used for formatting.
     * If `false`, lower case hexadecimal digits `0-9`, `a-f` will be used.
     *
     * Affects both `ByteArray` and numeric konstue formatting.
     */
    konst upperCase: Boolean,
    /**
     * Specifies hexadecimal format used for formatting and parsing `ByteArray`.
     */
    konst bytes: BytesHexFormat,
    /**
     * Specifies hexadecimal format used for formatting and parsing a numeric konstue.
     */
    konst number: NumberHexFormat
) {

    override fun toString(): String = buildString {
        append("HexFormat(").appendLine()
        append("    upperCase = ").append(upperCase).appendLine(",")
        append("    bytes = BytesHexFormat(").appendLine()
        bytes.appendOptionsTo(this, indent = "        ").appendLine()
        append("    ),").appendLine()
        append("    number = NumberHexFormat(").appendLine()
        number.appendOptionsTo(this, indent = "        ").appendLine()
        append("    )").appendLine()
        append(")")
    }

    /**
     * Represents hexadecimal format options for formatting and parsing `ByteArray`.
     *
     * When formatting one can assume that bytes are firstly separated using LF character (`'\n'`) into lines
     * with [bytesPerLine] bytes in each line. The last line may have fewer bytes.
     * Then each line is separated into groups using [groupSeparator] with [bytesPerGroup] bytes in each group,
     * except the last group in the line, which may have fewer bytes.
     * All bytes in a group are separated using [byteSeparator].
     * Each byte is converted to its two-digit hexadecimal representation,
     * immediately preceded by [bytePrefix] and immediately succeeded by [byteSuffix].
     *
     * When parsing the input string is required to be in the format described above.
     * However, any of the char sequences CRLF, LF and CR is considered a konstid line separator,
     * and parsing is performed in case-insensitive manner.
     *
     * See [BytesHexFormat.Builder] to find out how the options are configured,
     * and what is the default konstue of each option.
     */
    public class BytesHexFormat internal constructor(
        /** The maximum number of bytes per line. */
        konst bytesPerLine: Int,

        /** The maximum number of bytes per group. */
        konst bytesPerGroup: Int,
        /** The string used to separate adjacent groups in a line. */
        konst groupSeparator: String,

        /** The string used to separate adjacent bytes in a group. */
        konst byteSeparator: String,
        /** The string that immediately precedes two-digit hexadecimal representation of each byte. */
        konst bytePrefix: String,
        /** The string that immediately succeeds two-digit hexadecimal representation of each byte. */
        konst byteSuffix: String
    ) {

        override fun toString(): String = buildString {
            append("BytesHexFormat(").appendLine()
            appendOptionsTo(this, indent = "    ").appendLine()
            append(")")
        }

        internal fun appendOptionsTo(sb: StringBuilder, indent: String): StringBuilder {
            sb.append(indent).append("bytesPerLine = ").append(bytesPerLine).appendLine(",")
            sb.append(indent).append("bytesPerGroup = ").append(bytesPerGroup).appendLine(",")
            sb.append(indent).append("groupSeparator = \"").append(groupSeparator).appendLine("\",")
            sb.append(indent).append("byteSeparator = \"").append(byteSeparator).appendLine("\",")
            sb.append(indent).append("bytePrefix = \"").append(bytePrefix).appendLine("\",")
            sb.append(indent).append("byteSuffix = \"").append(byteSuffix).append("\"")
            return sb
        }

        /**
         * A context for building a [BytesHexFormat]. Provides API for configuring format options.
         */
        class Builder internal constructor() {
            /**
             * Defines [BytesHexFormat.bytesPerLine] of the format being built, [Int.MAX_VALUE] by default.
             *
             * The konstue must be positive.
             *
             * @throws IllegalArgumentException if a non-positive konstue is assigned to this property.
             */
            var bytesPerLine: Int = Default.bytesPerLine
                set(konstue) {
                    if (konstue <= 0)
                        throw IllegalArgumentException("Non-positive konstues are prohibited for bytesPerLine, but was $konstue")
                    field = konstue
                }

            /**
             * Defines [BytesHexFormat.bytesPerGroup] of the format being built, [Int.MAX_VALUE] by default.
             *
             * The konstue must be positive.
             *
             * @throws IllegalArgumentException if a non-positive konstue is assigned to this property.
             */
            var bytesPerGroup: Int = Default.bytesPerGroup
                set(konstue) {
                    if (konstue <= 0)
                        throw IllegalArgumentException("Non-positive konstues are prohibited for bytesPerGroup, but was $konstue")
                    field = konstue
                }

            /** Defines [BytesHexFormat.groupSeparator] of the format being built, two space characters (`"  "`) by default. */
            var groupSeparator: String = Default.groupSeparator

            /**
             * Defines [BytesHexFormat.byteSeparator] of the format being built, empty string by default.
             *
             * The string must not contain LF and CR characters.
             *
             * @throws IllegalArgumentException if a string containing LF or CR character is assigned to this property.
             */
            var byteSeparator: String = Default.byteSeparator
                set(konstue) {
                    if (konstue.contains('\n') || konstue.contains('\r'))
                        throw IllegalArgumentException("LF and CR characters are prohibited in byteSeparator, but was $konstue")
                    field = konstue
                }

            /**
             * Defines [BytesHexFormat.bytePrefix] of the format being built, empty string by default.
             *
             * The string must not contain LF and CR characters.
             *
             * @throws IllegalArgumentException if a string containing LF or CR character is assigned to this property.
             */
            var bytePrefix: String = Default.bytePrefix
                set(konstue) {
                    if (konstue.contains('\n') || konstue.contains('\r'))
                        throw IllegalArgumentException("LF and CR characters are prohibited in bytePrefix, but was $konstue")
                    field = konstue
                }

            /**
             * Defines [BytesHexFormat.byteSuffix] of the format being built, empty string by default.
             *
             * The string must not contain LF and CR characters.
             *
             * @throws IllegalArgumentException if a string containing LF or CR character is assigned to this property.
             */
            var byteSuffix: String = Default.byteSuffix
                set(konstue) {
                    if (konstue.contains('\n') || konstue.contains('\r'))
                        throw IllegalArgumentException("LF and CR characters are prohibited in byteSuffix, but was $konstue")
                    field = konstue
                }

            internal fun build(): BytesHexFormat {
                return BytesHexFormat(bytesPerLine, bytesPerGroup, groupSeparator, byteSeparator, bytePrefix, byteSuffix)
            }
        }

        internal companion object {
            internal konst Default = BytesHexFormat(
                bytesPerLine = Int.MAX_VALUE,
                bytesPerGroup = Int.MAX_VALUE,
                groupSeparator = "  ",
                byteSeparator = "",
                bytePrefix = "",
                byteSuffix = ""
            )
        }
    }

    /**
     * Represents hexadecimal format options for formatting and parsing a numeric konstue.
     *
     * The formatting result consist of [prefix] string, hexadecimal representation of the konstue being formatted, and [suffix] string.
     * Hexadecimal representation of a konstue is calculated by mapping each four-bit chunk
     * of its binary representation to the corresponding hexadecimal digit, starting with the most significant bits.
     * [upperCase] determines whether upper case `0-9`, `A-F` or lower case `0-9`, `a-f` hexadecimal digits are used.
     * If [removeLeadingZeros] it `true`, leading zeros in the hexadecimal representation are removed.
     *
     * For example, the binary representation of the `Byte` konstue `58` is the 8-bit long `00111010`,
     * which converts to a hexadecimal representation of `3a` or `3A` depending on [upperCase].
     * Whereas, the binary representation of the `Int` konstue `58` is the 32-bit long `00000000000000000000000000111010`,
     * which converts to a hexadecimal representation of `0000003a` or `0000003A` depending on [upperCase].
     * If [removeLeadingZeros] it `true`, leading zeros in `0000003a` are removed, resulting `3a`.
     *
     * To convert a konstue to hexadecimal string of a particular length,
     * first convert the konstue to a type with the corresponding bit size.
     * For example, to convert an `Int` konstue to 4-digit hexadecimal string,
     * convert the konstue `toShort()` before hexadecimal formatting.
     * To convert it to hexadecimal string of at most 4 digits
     * without leading zeros, set [removeLeadingZeros] to `true` in addition.
     *
     * Parsing requires [prefix] and [suffix] to be present in the input string,
     * and the amount of hexadecimal digits to be at least one and at most the konstue bit size divided by four.
     * Parsing is performed in case-insensitive manner, and [removeLeadingZeros] is ignored as well.
     *
     * See [NumberHexFormat.Builder] to find out how the options are configured,
     * and what is the default konstue of each option.
     */
    public class NumberHexFormat internal constructor(
        /** The string that immediately precedes hexadecimal representation of a numeric konstue. */
        konst prefix: String,
        /** The string that immediately succeeds hexadecimal representation of a numeric konstue. */
        konst suffix: String,
        /** Specifies whether to remove leading zeros in the hexadecimal representation of a numeric konstue. */
        konst removeLeadingZeros: Boolean
    ) {

        override fun toString(): String = buildString {
            append("NumberHexFormat(").appendLine()
            appendOptionsTo(this, indent = "    ").appendLine()
            append(")")
        }

        internal fun appendOptionsTo(sb: StringBuilder, indent: String): StringBuilder {
            sb.append(indent).append("prefix = \"").append(prefix).appendLine("\",")
            sb.append(indent).append("suffix = \"").append(suffix).appendLine("\",")
            sb.append(indent).append("removeLeadingZeros = ").append(removeLeadingZeros)
            return sb
        }

        /**
         * A context for building a [NumberHexFormat]. Provides API for configuring format options.
         */
        class Builder internal constructor() {
            /**
             * Defines [NumberHexFormat.prefix] of the format being built, empty string by default.
             *
             * The string must not contain LF and CR characters.
             *
             * @throws IllegalArgumentException if a string containing LF or CR character is assigned to this property.
             */
            var prefix: String = Default.prefix
                set(konstue) {
                    if (konstue.contains('\n') || konstue.contains('\r'))
                        throw IllegalArgumentException("LF and CR characters are prohibited in prefix, but was $konstue")
                    field = konstue
                }

            /**
             * Defines [NumberHexFormat.suffix] of the format being built, empty string by default.
             *
             * The string must not contain LF and CR characters.
             *
             * @throws IllegalArgumentException if a string containing LF or CR character is assigned to this property.
             */
            var suffix: String = Default.suffix
                set(konstue) {
                    if (konstue.contains('\n') || konstue.contains('\r'))
                        throw IllegalArgumentException("LF and CR characters are prohibited in suffix, but was $konstue")
                    field = konstue
                }

            /** Defines [NumberHexFormat.removeLeadingZeros] of the format being built, empty string by default. */
            var removeLeadingZeros: Boolean = Default.removeLeadingZeros

            internal fun build(): NumberHexFormat {
                return NumberHexFormat(prefix, suffix, removeLeadingZeros)
            }
        }

        internal companion object {
            internal konst Default = NumberHexFormat(
                prefix = "",
                suffix = "",
                removeLeadingZeros = false
            )
        }
    }


    /**
     * A context for building a [HexFormat]. Provides API for configuring format options.
     */
    public class Builder @PublishedApi internal constructor() {
        /** Defines [HexFormat.upperCase] of the format being built, `false` by default. */
        var upperCase: Boolean = Default.upperCase

        /**
         * Defines [HexFormat.bytes] of the format being built.
         *
         * See [BytesHexFormat.Builder] for default konstues of the options.
         */
        konst bytes: BytesHexFormat.Builder
            get() {
                if (_bytes == null) {
                    _bytes = BytesHexFormat.Builder()
                }
                return _bytes!!
            }

        private var _bytes: BytesHexFormat.Builder? = null

        /**
         * Defines [HexFormat.number] of the format being built.
         *
         * See [NumberHexFormat.Builder] for default konstues of the options.
         */
        konst number: NumberHexFormat.Builder
            get() {
                if (_number == null) {
                    _number = NumberHexFormat.Builder()
                }
                return _number!!
            }

        private var _number: NumberHexFormat.Builder? = null

        /**
         * Provides a scope for configuring the [HexFormat.bytes] format options.
         *
         * See [BytesHexFormat.Builder] for default konstues of the options.
         */
        @InlineOnly
        inline fun bytes(builderAction: BytesHexFormat.Builder.() -> Unit) {
            bytes.builderAction()
        }

        /**
         * Provides a scope for configuring the [HexFormat.number] format options.
         *
         * See [NumberHexFormat.Builder] for default konstues of the options.
         */
        @InlineOnly
        inline fun number(builderAction: NumberHexFormat.Builder.() -> Unit) {
            number.builderAction()
        }

        @PublishedApi
        internal fun build(): HexFormat {
            return HexFormat(
                upperCase,
                _bytes?.build() ?: BytesHexFormat.Default,
                _number?.build() ?: NumberHexFormat.Default
            )
        }
    }


    companion object {
        /**
         * The default hexadecimal format options.
         *
         * Uses lower case hexadecimal digits `0-9`, `a-f` when formatting
         * both `ByteArray` and numeric konstues. That is [upperCase] is `false`.
         *
         * No line separator, group separator, byte separator, byte prefix or byte suffix is used
         * when formatting or parsing `ByteArray`. That is:
         *   * [BytesHexFormat.bytesPerLine] is `Int.MAX_VALUE`.
         *   * [BytesHexFormat.bytesPerGroup] is `Int.MAX_VALUE`.
         *   * [BytesHexFormat.byteSeparator], [BytesHexFormat.bytePrefix] and [BytesHexFormat.byteSuffix] are empty strings.
         *
         * No prefix or suffix is used, and no leading zeros in hexadecimal representation are removed
         * when formatting or parsing a numeric konstue. That is:
         *   * [NumberHexFormat.prefix] and [NumberHexFormat.suffix] are empty strings.
         *   * [NumberHexFormat.removeLeadingZeros] is `false`.
         */
        public konst Default: HexFormat = HexFormat(
            upperCase = false,
            bytes = BytesHexFormat.Default,
            number = NumberHexFormat.Default,
        )

        /**
         * Uses upper case hexadecimal digits `0-9`, `A-F` when formatting
         * both `ByteArray` and numeric konstues. That is [upperCase] is `true`.
         *
         * The same as [Default] format in other aspects.
         */
        public konst UpperCase: HexFormat = HexFormat(
            upperCase = true,
            bytes = BytesHexFormat.Default,
            number = NumberHexFormat.Default,
        )
    }
}

/**
 * Builds a new [HexFormat] by configuring its format options using the specified [builderAction],
 * and returns the resulting format.
 *
 * The builder passed as a receiver to the [builderAction] is konstid only inside that function.
 * Using it outside of the function produces an unspecified behavior.
 */
@ExperimentalStdlibApi
@SinceKotlin("1.9")
@InlineOnly
public inline fun HexFormat(builderAction: HexFormat.Builder.() -> Unit): HexFormat {
    return HexFormat.Builder().apply(builderAction).build()
}
