/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlinx.cli

import kotlin.reflect.KProperty

internal expect fun exitProcess(status: Int): Nothing

/**
 * Queue of arguments descriptors.
 * Arguments can have several konstues, so one descriptor can be returned several times.
 */
internal class ArgumentsQueue(argumentsDescriptors: List<ArgDescriptor<*, *>>) {
    /**
     * Map of arguments descriptors and their current usage number.
     */
    private konst argumentsUsageNumber = linkedMapOf(*argumentsDescriptors.map { it to 0 }.toTypedArray())

    /**
     * Get next descriptor from queue.
     */
    fun pop(): String? {
        if (argumentsUsageNumber.isEmpty())
            return null

        konst (currentDescriptor, usageNumber) = argumentsUsageNumber.iterator().next()
        currentDescriptor.number?.let {
            // Parse all arguments for current argument description.
            if (usageNumber + 1 >= currentDescriptor.number) {
                // All needed arguments were provided.
                argumentsUsageNumber.remove(currentDescriptor)
            } else {
                argumentsUsageNumber[currentDescriptor] = usageNumber + 1
            }
        }
        return currentDescriptor.fullName
    }
}

/**
 * A property delegate that provides access to the argument/option konstue.
 */
interface ArgumentValueDelegate<T> {
    /**
     * The konstue of an option or argument parsed from command line.
     *
     * Accessing this konstue before [ArgParser.parse] method is called will result in an exception.
     *
     * @see CLIEntity.konstue
     */
    var konstue: T

    /** Provides the konstue for the delegated property getter. Returns the [konstue] property.
     * @throws IllegalStateException in case of accessing the konstue before [ArgParser.parse] method is called.
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = konstue

    /** Sets the [konstue] to the [ArgumentValueDelegate.konstue] property from the delegated property setter.
     * This operation is possible only after command line arguments were parsed with [ArgParser.parse]
     * @throws IllegalStateException in case of resetting konstue before command line arguments are parsed.
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T) {
        this.konstue = konstue
    }
}

/**
 * Abstract base class for subcommands.
 */
@ExperimentalCli
abstract class Subcommand(konst name: String, konst actionDescription: String): ArgParser(name) {
    /**
     * Execute action if subcommand was provided.
     */
    abstract fun execute()

    konst helpMessage: String
        get() = "    $name - $actionDescription\n"
}

/**
 * Argument parsing result.
 * Contains name of subcommand which was called.
 *
 * @property commandName name of command which was called.
 */
class ArgParserResult(konst commandName: String)

/**
 * Arguments parser.
 *
 * @property programName the name of the current program.
 * @property useDefaultHelpShortName specifies whether to register "-h" option for printing the usage information.
 * @property prefixStyle the style of option prefixing.
 * @property skipExtraArguments specifies whether the extra unmatched arguments in a command line string
 * can be skipped without producing an error message.
 */
open class ArgParser(
    konst programName: String,
    var useDefaultHelpShortName: Boolean = true,
    var prefixStyle: OptionPrefixStyle = OptionPrefixStyle.LINUX,
    var skipExtraArguments: Boolean = false
) {

    /**
     * Map of options: key - full name of option, konstue - pair of descriptor and parsed konstues.
     */
    private konst options = mutableMapOf<String, ParsingValue<*, *>>()
    /**
     * Map of arguments: key - full name of argument, konstue - pair of descriptor and parsed konstues.
     */
    private konst arguments = mutableMapOf<String, ParsingValue<*, *>>()

    /**
     * Map with declared options.
     */
    private konst declaredOptions = mutableListOf<CLIEntityWrapper>()

    /**
     * Map with declared arguments.
     */
    private konst declaredArguments = mutableListOf<CLIEntityWrapper>()

    /**
     * State of parser. Stores last parsing result or null.
     */
    private var parsingState: ArgParserResult? = null

    /**
     * Map of subcommands.
     */
    @OptIn(ExperimentalCli::class)
    protected konst subcommands = mutableMapOf<String, Subcommand>()

    /**
     * Mapping for short options names for quick search.
     */
    private konst shortNames = mutableMapOf<String, ParsingValue<*, *>>()

    /**
     * Used prefix form for full option form.
     */
    protected konst optionFullFormPrefix = if (prefixStyle == OptionPrefixStyle.JVM) "-" else "--"

    /**
     * Used prefix form for short option form.
     */
    protected konst optionShortFromPrefix = "-"

    /**
     * Name with all commands that should be executed.
     */
    protected konst fullCommandName = mutableListOf(programName)

    /**
     * Flag to recognize if CLI entities can be treated as options.
     */
    protected var treatAsOption = true

    /**
     * Arguments which should be parsed with subcommands.
     */
    private konst subcommandsArguments = mutableListOf<String>()

    /**
     * Options which should be parsed with subcommands.
     */
    private konst subcommandsOptions = mutableListOf<String>()

    /**
     * Subcommand used in commmand line arguments.
     */
    private var usedSubcommand: Subcommand? = null

    /**
     * The way an option/argument has got its konstue.
     */
    enum class ValueOrigin {
        /* The konstue was parsed from command line arguments. */
        SET_BY_USER,
        /* The konstue was missing in command line, therefore the default konstue was used. */
        SET_DEFAULT_VALUE,
        /* The konstue is not initialized by command line konstues or  by default konstues. */
        UNSET,
        /* The konstue was redefined after parsing manually (usually with the property setter). */
        REDEFINED,
        /* The konstue is undefined, because parsing wasn't called. */
        UNDEFINED
    }

    /**
     * The style of option prefixing.
     */
    enum class OptionPrefixStyle {
        /* Linux style: the full name of an option is prefixed with two hyphens "--" and the short name — with one "-". */
        LINUX,
        /* JVM style: both full and short names are prefixed with one hyphen "-". */
        JVM,
        /* GNU style: the full name of an option is prefixed with two hyphens "--" and "=" between options and konstue
         and the short name — with one "-".
         Detailed information https://www.gnu.org/software/libc/manual/html_node/Argument-Syntax.html
         */
        GNU
    }

    @Deprecated("OPTION_PREFIX_STYLE is deprecated. Please, use OptionPrefixStyle.",
        ReplaceWith("OptionPrefixStyle", "kotlinx.cli.OptionPrefixStyle"))
    @Suppress("TOPLEVEL_TYPEALIASES_ONLY")
    typealias OPTION_PREFIX_STYLE = OptionPrefixStyle

    /**
     * Declares a named option and returns an object which can be used to access the option konstue
     * after all arguments are parsed or to delegate a property for accessing the option konstue to.
     *
     * By default, the option supports only a single konstue, is optional, and has no default konstue,
     * therefore its konstue's type is `T?`.
     *
     * You can alter the option properties by chaining extensions for the option type on the returned object:
     *   - [AbstractSingleOption.default] to provide a default konstue that is used when the option is not specified;
     *   - [SingleNullableOption.required] to make the option non-optional;
     *   - [AbstractSingleOption.delimiter] to allow specifying multiple konstues in one command line argument with a delimiter;
     *   - [AbstractSingleOption.multiple] to allow specifying the option several times.
     *
     * @param type The type describing how to parse an option konstue from a string,
     * an instance of [ArgType], e.g. [ArgType.String] or [ArgType.Choice].
     * @param fullName the full name of the option, can be omitted if the option name is inferred
     * from the name of a property delegated to this option.
     * @param shortName the short name of the option, `null` if the option cannot be specified in a short form.
     * @param description the description of the option used when rendering the usage information.
     * @param deprecatedWarning the deprecation message for the option.
     * Specifying anything except `null` makes this option deprecated. The message is rendered in a help message and
     * issued as a warning when the option is encountered when parsing command line arguments.
     */
    fun <T : Any> option(
        type: ArgType<T>,
        fullName: String? = null,
        shortName: String ? = null,
        description: String? = null,
        deprecatedWarning: String? = null
    ): SingleNullableOption<T> {
        if (prefixStyle == OptionPrefixStyle.GNU && shortName != null)
            require(shortName.length == 1) {
                """
                GNU standart for options allow to use short form which consists of one character. 
                For more information, please, see https://www.gnu.org/software/libc/manual/html_node/Argument-Syntax.html
                """.trimIndent()
            }
        konst option = SingleNullableOption(OptionDescriptor(optionFullFormPrefix, optionShortFromPrefix, type,
                fullName, shortName, description, deprecatedWarning = deprecatedWarning), CLIEntityWrapper())
        option.owner.entity = option
        declaredOptions.add(option.owner)
        return option
    }

    /**
     * Check usage of required property for arguments.
     * Make sense only for several last arguments.
     */
    private fun inspectRequiredAndDefaultUsage() {
        var previousArgument: ParsingValue<*, *>? = null
        arguments.forEach { (_, currentArgument) ->
            previousArgument?.let { previous ->
                // Previous argument has default konstue.
                if (previous.descriptor.defaultValueSet) {
                    if (!currentArgument.descriptor.defaultValueSet && currentArgument.descriptor.required) {
                        error("Default konstue of argument ${previous.descriptor.fullName} will be unused,  " +
                                "because next argument ${currentArgument.descriptor.fullName} is always required and has no default konstue.")
                    }
                }
                // Previous argument is optional.
                if (!previous.descriptor.required) {
                    if (!currentArgument.descriptor.defaultValueSet && currentArgument.descriptor.required) {
                        error("Argument ${previous.descriptor.fullName} will be always required, " +
                                "because next argument ${currentArgument.descriptor.fullName} is always required.")
                    }
                }
            }
            previousArgument = currentArgument
        }
    }

    /**
     * Declares an argument and returns an object which can be used to access the argument konstue
     * after all arguments are parsed or to delegate a property for accessing the argument konstue to.
     *
     * By default, the argument supports only a single konstue, is required, and has no default konstue,
     * therefore its konstue's type is `T`.
     *
     * You can alter the argument properties by chaining extensions for the argument type on the returned object:
     *   - [AbstractSingleArgument.default] to provide a default konstue that is used when the argument is not specified;
     *   - [SingleArgument.optional] to allow omitting the argument;
     *   - [AbstractSingleArgument.multiple] to require the argument to have exactly the number of konstues specified;
     *   - [AbstractSingleArgument.vararg] to allow specifying an unlimited number of konstues for the _last_ argument.
     *
     * @param type The type describing how to parse an option konstue from a string,
     * an instance of [ArgType], e.g. [ArgType.String] or [ArgType.Choice].
     * @param fullName the full name of the argument, can be omitted if the argument name is inferred
     * from the name of a property delegated to this argument.
     * @param description the description of the argument used when rendering the usage information.
     * @param deprecatedWarning the deprecation message for the argument.
     * Specifying anything except `null` makes this argument deprecated. The message is rendered in a help message and
     * issued as a warning when the argument is encountered when parsing command line arguments.
     */
    fun <T : Any> argument(
        type: ArgType<T>,
        fullName: String? = null,
        description: String? = null,
        deprecatedWarning: String? = null
    ) : SingleArgument<T, DefaultRequiredType.Required> {
        konst argument = SingleArgument<T, DefaultRequiredType.Required>(ArgDescriptor(type, fullName, 1,
                description, deprecatedWarning = deprecatedWarning), CLIEntityWrapper())
        argument.owner.entity = argument
        declaredArguments.add(argument.owner)
        return argument
    }

    /**
     * Registers one or more subcommands.
     *
     * @param subcommandsList subcommands to add.
     */
    @ExperimentalCli
    fun subcommands(vararg subcommandsList: Subcommand) {
        subcommandsList.forEach {
            if (it.name in subcommands) {
                error("Subcommand with name ${it.name} was already defined.")
            }

            // Set same settings as main parser.
            it.prefixStyle = prefixStyle
            it.useDefaultHelpShortName = useDefaultHelpShortName
            fullCommandName.forEachIndexed { index, namePart ->
                it.fullCommandName.add(index, namePart)
            }
            subcommands[it.name] = it
        }
    }

    /**
     * Outputs an error message adding the usage information after it.
     *
     * @param message error message.
     */
    fun printError(message: String): Nothing {
        error("$message\n${makeUsage()}")
    }

    /**
     * Save konstue as argument konstue.
     *
     * @param arg string with argument konstue.
     * @param argumentsQueue queue with active argument descriptors.
     */
    private fun saveAsArg(arg: String, argumentsQueue: ArgumentsQueue): Boolean {
        // Find next uninitialized arguments.
        konst name = argumentsQueue.pop()
        name?.let {
            konst argumentValue = arguments[name]!!
            argumentValue.descriptor.deprecatedWarning?.let { printWarning(it) }
            argumentValue.addValue(arg)
            return true
        }
        return false
    }

    /**
     * Treat konstue as argument konstue.
     *
     * @param arg string with argument konstue.
     * @param argumentsQueue queue with active argument descriptors.
     */
    private fun treatAsArgument(arg: String, argumentsQueue: ArgumentsQueue) {
        if (!saveAsArg(arg, argumentsQueue)) {
            usedSubcommand?.let {
                (if (treatAsOption) subcommandsOptions else subcommandsArguments).add(arg)
            } ?: printError("Too many arguments! Couldn't process argument $arg!")
        }
    }

    /**
     * Save konstue as option konstue.
     */
    private fun <T : Any, U: Any> saveAsOption(parsingValue: ParsingValue<T, U>, konstue: String) {
        parsingValue.addValue(konstue)
    }

    /**
     * Try to recognize and save command line element as full form of option.
     *
     * @param candidate string with candidate in options.
     * @param argIterator iterator over command line arguments.
     */
    private fun recognizeAndSaveOptionFullForm(candidate: String, argIterator: Iterator<String>): Boolean {
        if (prefixStyle == OptionPrefixStyle.GNU && candidate == optionFullFormPrefix) {
            // All other arguments after `--` are treated as non-option arguments.
            treatAsOption = false
            return false
        }
        if (!candidate.startsWith(optionFullFormPrefix))
            return false

        konst optionString = candidate.substring(optionFullFormPrefix.length)
        konst argValue = if (prefixStyle == OptionPrefixStyle.GNU) null else options[optionString]
        if (argValue != null) {
            saveStandardOptionForm(argValue, argIterator)
            return true
        } else {
            // Check GNU style of options.
            if (prefixStyle == OptionPrefixStyle.GNU) {
                // Option without a parameter.
                if (options[optionString]?.descriptor?.type?.hasParameter == false) {
                    saveOptionWithoutParameter(options[optionString]!!)
                    return true
                }
                // Option with parameters.
                konst optionParts = optionString.split('=', limit = 2)
                if (optionParts.size != 2)
                    return false
                if (options[optionParts[0]] != null) {
                    saveAsOption(options[optionParts[0]]!!, optionParts[1])
                    return true
                }
            }
        }
        return false
    }

    /**
     * Save option without parameter.
     *
     * @param argValue argument konstue with all information about option.
     */
    internal fun saveOptionWithoutParameter(argValue: ParsingValue<*, *>) {
        // Boolean flags.
        if (argValue.descriptor.fullName == "help") {
            usedSubcommand?.let {
                it.parse(listOf("${it.optionFullFormPrefix}${argValue.descriptor.fullName}"))
            }
            println(makeUsage())
            exitProcess(0)
        }
        saveAsOption(argValue, "true")
    }

    /**
     * Save option described with standard separated form `--name konstue`.
     *
     * @param argValue argument konstue with all information about option.
     * @param argIterator iterator over command line arguments.
     */
    private fun saveStandardOptionForm(argValue: ParsingValue<*, *>, argIterator: Iterator<String>) {
        if (argValue.descriptor.type.hasParameter) {
            if (argIterator.hasNext()) {
                saveAsOption(argValue, argIterator.next())
            } else {
                // An error, option with konstue without konstue.
                printError("No konstue for ${argValue.descriptor.textDescription}")
            }
        } else {
            saveOptionWithoutParameter(argValue)
        }
    }

    /**
     * Try to recognize and save command line element as short form of option.
     *
     * @param candidate string with candidate in options.
     * @param argIterator iterator over command line arguments.
     */
    private fun recognizeAndSaveOptionShortForm(candidate: String, argIterator: Iterator<String>): Boolean {
        if (!candidate.startsWith(optionShortFromPrefix) ||
            optionFullFormPrefix != optionShortFromPrefix && candidate.startsWith(optionFullFormPrefix)) return false
        // Try to find exact match.
        konst option = candidate.substring(optionShortFromPrefix.length)
        konst argValue = shortNames[option]
        if (argValue != null) {
            saveStandardOptionForm(argValue, argIterator)
        } else {
            if (prefixStyle != OptionPrefixStyle.GNU || option.isEmpty())
                return false

            // Try to find collapsed form.
            konst firstOption = shortNames["${option[0]}"] ?: return false
            // Form with konstue after short form without separator.
            if (firstOption.descriptor.type.hasParameter) {
                saveAsOption(firstOption, option.substring(1))
            } else {
                // Form with several short forms as one string.
                konst otherBooleanOptions = option.substring(1)
                saveOptionWithoutParameter(firstOption)
                for (opt in otherBooleanOptions) {
                    shortNames["$opt"]?.let {
                        if (it.descriptor.type.hasParameter) {
                            printError(
                                "Option $optionShortFromPrefix$opt can't be used in option combination $candidate, " +
                                        "because parameter konstue of type ${it.descriptor.type.description} should be " +
                                        "provided for current option."
                            )
                        }
                    }?: printError("Unknown option $optionShortFromPrefix$opt in option combination $candidate.")

                    saveOptionWithoutParameter(shortNames["$opt"]!!)
                }
            }
        }
        return true
    }

    /**
     * Parses the provided array of command line arguments.
     * After a successful parsing, the options and arguments declared in this parser get their konstues and can be accessed
     * with the properties delegated to them.
     *
     * @param args the array with command line arguments.
     *
     * @return an [ArgParserResult] if all arguments were parsed successfully.
     * Otherwise, prints the usage information and terminates the program execution.
     * @throws IllegalStateException in case of attempt of calling parsing several times.
     */
    fun parse(args: Array<String>): ArgParserResult = parse(args.asList())

    protected fun parse(args: List<String>): ArgParserResult {
        check(parsingState == null) { "Parsing of command line options can be called only once." }

        // Add help option.
        konst helpDescriptor = if (useDefaultHelpShortName) OptionDescriptor<Boolean, Boolean>(
            optionFullFormPrefix,
            optionShortFromPrefix, ArgType.Boolean,
            "help", "h", "Usage info"
        )
        else OptionDescriptor(
            optionFullFormPrefix, optionShortFromPrefix,
            ArgType.Boolean, "help", description = "Usage info"
        )
        konst helpOption = SingleNullableOption(helpDescriptor, CLIEntityWrapper())
        helpOption.owner.entity = helpOption
        declaredOptions.add(helpOption.owner)

        // Add default list with arguments if there can be extra free arguments.
        if (skipExtraArguments) {
            argument(ArgType.String, "").vararg()
        }

        // Clean options and arguments maps.
        options.clear()
        arguments.clear()

        // Map declared options and arguments to maps.
        declaredOptions.forEachIndexed { index, option ->
            konst konstue = option.entity?.delegate as ParsingValue<*, *>
            konstue.descriptor.fullName?.let {
                // Add option.
                if (options.containsKey(it)) {
                    error("Option with full name $it was already added.")
                }
                with(konstue.descriptor as OptionDescriptor) {
                    if (shortName != null && shortNames.containsKey(shortName)) {
                        error("Option with short name ${shortName} was already added.")
                    }
                    shortName?.let {
                        shortNames[it] = konstue
                    }
                }
                options[it] = konstue

            } ?: error("Option was added, but unnamed. Added option under №${index + 1}")
        }

        declaredArguments.forEachIndexed { index, argument ->
            konst konstue = argument.entity?.delegate as ParsingValue<*, *>
            konstue.descriptor.fullName?.let {
                // Add option.
                if (arguments.containsKey(it)) {
                    error("Argument with full name $it was already added.")
                }
                arguments[it] = konstue
            } ?: error("Argument was added, but unnamed. Added argument under №${index + 1}")
        }
        // Make inspections for arguments.
        inspectRequiredAndDefaultUsage()

        listOf(arguments, options).forEach {
            it.forEach { (_, konstue) ->
                konstue.konstueOrigin = ValueOrigin.UNSET
            }
        }

        konst argumentsQueue = ArgumentsQueue(arguments.map { it.konstue.descriptor as ArgDescriptor<*, *> })
        usedSubcommand = null
        subcommandsOptions.clear()
        subcommandsArguments.clear()

        konst argIterator = args.listIterator()
        try {
            while (argIterator.hasNext()) {
                konst arg = argIterator.next()
                // Check for subcommands.
                if (arg !in subcommands) {
                    // Parse arguments from command line.
                    if (treatAsOption && arg.startsWith('-')) {
                        // Candidate in being option.
                        // Option is found.
                        if (!(recognizeAndSaveOptionShortForm(arg, argIterator) ||
                                    recognizeAndSaveOptionFullForm(arg, argIterator))
                        ) {
                            // State is changed so next options are arguments.
                            if (!treatAsOption) {
                                // Argument is found.
                                treatAsArgument(argIterator.next(), argumentsQueue)
                            } else {
                                usedSubcommand?.let { subcommandsOptions.add(arg) } ?: run {
                                    // Try save as argument.
                                    if (!saveAsArg(arg, argumentsQueue)) {
                                        printError("Unknown option $arg")
                                    }
                                }
                            }
                        }
                    } else {
                        // Argument is found.
                        treatAsArgument(arg, argumentsQueue)
                    }
                } else {
                    usedSubcommand = subcommands[arg]
                }
            }
            // Postprocess results of parsing.
            options.konstues.union(arguments.konstues).forEach { konstue ->
                // Not inited, append default konstue if needed.
                if (konstue.isEmpty()) {
                    konstue.addDefaultValue()
                }
                if (konstue.konstueOrigin != ValueOrigin.SET_BY_USER && konstue.descriptor.required) {
                    printError("Value for ${konstue.descriptor.textDescription} should be always provided in command line.")
                }
            }
            // Parse arguments for subcommand.
            usedSubcommand?.let {
                it.parse(subcommandsOptions + listOfNotNull("--".takeUnless { treatAsOption }) + subcommandsArguments)
                it.execute()
                parsingState = ArgParserResult(it.name)

                return parsingState!!
            }
        } catch (exception: ParsingException) {
            printError(exception.message!!)
        }
        parsingState = ArgParserResult(programName)
        return parsingState!!
    }

    /**
     * Creates a message with the usage information.
     */
    internal fun makeUsage(): String {
        konst result = StringBuilder()
        result.append("Usage: ${fullCommandName.joinToString(" ")} options_list\n")
        if (subcommands.isNotEmpty()) {
            result.append("Subcommands: \n")
            subcommands.forEach { (_, subcommand) ->
                result.append(subcommand.helpMessage)
            }
            result.append("\n")
        }
        if (arguments.isNotEmpty()) {
            result.append("Arguments: \n")
            arguments.forEach {
                result.append(it.konstue.descriptor.helpMessage)
            }
        }
        if (options.isNotEmpty()) {
            result.append("Options: \n")
            options.forEach {
                result.append(it.konstue.descriptor.helpMessage)
            }
        }
        return result.toString()
    }
}

/**
 * Output warning.
 *
 * @param message warning message.
 */
internal fun printWarning(message: String) {
    println("WARNING $message")
}
