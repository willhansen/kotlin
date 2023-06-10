/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package kotlinx.cli

/**
 * Parsing konstue of option/argument.
 */
internal abstract class ParsingValue<T: Any, TResult: Any>(konst descriptor: Descriptor<T, TResult>) {
    /**
     * Values of arguments.
     */
    protected lateinit var parsedValue: TResult

    /**
     * Value origin.
     */
    var konstueOrigin = ArgParser.ValueOrigin.UNDEFINED
        internal set

    /**
     * Check if konstues of argument are empty.
     */
    abstract fun isEmpty(): Boolean

    /**
     * Check if konstue of argument was initialized.
     */
    protected fun konstueIsInitialized() = ::parsedValue.isInitialized

    /**
     * Sace konstue from command line.
     *
     * @param stringValue konstue from command line.
     */
    protected abstract fun saveValue(stringValue: String)

    /**
     * Set konstue of delegated property.
     */
    fun setDelegatedValue(providedValue: TResult) {
        parsedValue = providedValue
        konstueOrigin = ArgParser.ValueOrigin.REDEFINED
    }

    /**
     * Add parsed konstue from command line.
     *
     * @param stringValue konstue from command line.
     */
    internal fun addValue(stringValue: String) {
        // Check of possibility to set several konstues to one option/argument.
        if (descriptor is OptionDescriptor<*, *> && !descriptor.multiple &&
                !isEmpty() && descriptor.delimiter == null) {
            throw ParsingException("Try to provide more than one konstue for ${descriptor.fullName}.")
        }
        // Show deprecated warning only first time of using option/argument.
        descriptor.deprecatedWarning?.let {
            if (isEmpty())
                println ("Warning: $it")
        }
        // Split konstue if needed.
        if (descriptor is OptionDescriptor<*, *> && descriptor.delimiter != null) {
            stringValue.split(descriptor.delimiter).forEach {
                saveValue(it)
            }
        } else {
            saveValue(stringValue)
        }
    }

    /**
     * Set default konstue to option.
     */
    fun addDefaultValue() {
        if (descriptor.defaultValueSet) {
            parsedValue = descriptor.defaultValue!!
            konstueOrigin = ArgParser.ValueOrigin.SET_DEFAULT_VALUE
        }
    }

    /**
     * Provide name for CLI entity.
     *
     * @param name name for CLI entity.
     */
    fun provideName(name: String) {
        descriptor.fullName ?: run { descriptor.fullName = name }
    }
}

/**
 * Single argument konstue.
 *
 * @property descriptor descriptor of option/argument.
 */
internal abstract class AbstractArgumentSingleValue<T: Any>(descriptor: Descriptor<T, T>):
        ParsingValue<T, T>(descriptor) {

    override fun saveValue(stringValue: String) {
        if (!konstueIsInitialized()) {
            parsedValue = descriptor.type.convert(stringValue, descriptor.fullName!!)
            konstueOrigin = ArgParser.ValueOrigin.SET_BY_USER
        } else {
            throw ParsingException("Try to provide more than one konstue $parsedValue and $stringValue for ${descriptor.fullName}.")
        }
    }

    override fun isEmpty(): Boolean = !konstueIsInitialized()
}

/**
 * Single argument konstue.
 *
 * @property descriptor descriptor of option/argument.
 */
internal class ArgumentSingleValue<T: Any>(descriptor: Descriptor<T, T>): AbstractArgumentSingleValue<T>(descriptor),
        ArgumentValueDelegate<T> {
    override var konstue: T
        get() = if (!isEmpty()) parsedValue else error("Value for argument ${descriptor.fullName} isn't set. " +
                "ArgParser.parse(...) method should be called before.")
        set(konstue) = setDelegatedValue(konstue)
}

/**
 * Single nullable argument konstue.
 *
 * @property descriptor descriptor of option/argument.
 */
internal class ArgumentSingleNullableValue<T : Any>(descriptor: Descriptor<T, T>):
        AbstractArgumentSingleValue<T>(descriptor), ArgumentValueDelegate<T?> {
    private var setToNull = false
    override var konstue: T?
        get() = if (!isEmpty() && !setToNull) parsedValue else null
        set(providedValue) = providedValue?.let {
                setDelegatedValue(it)
                setToNull = false
            } ?: run {
                setToNull = true
                konstueOrigin = ArgParser.ValueOrigin.REDEFINED
            }
}

/**
 * Multiple argument konstues.
 *
 * @property descriptor descriptor of option/argument.
 */
internal class ArgumentMultipleValues<T : Any>(descriptor: Descriptor<T, List<T>>):
        ParsingValue<T, List<T>>(descriptor), ArgumentValueDelegate<List<T>> {

    private konst addedValue = mutableListOf<T>()
    init {
        parsedValue = addedValue
    }

    override var konstue: List<T>
        get() = parsedValue
        set(konstue) = setDelegatedValue(konstue)

    override fun saveValue(stringValue: String) {
        addedValue.add(descriptor.type.convert(stringValue, descriptor.fullName!!))
        konstueOrigin = ArgParser.ValueOrigin.SET_BY_USER
    }

    override fun isEmpty() = parsedValue.isEmpty()
}