/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package kotlinx.cli

import kotlin.reflect.KProperty

internal data class CLIEntityWrapper(var entity: CLIEntity<*>? = null)

/**
 * Base interface for all possible types of entities with default and required konstues.
 * Provides limitations for API that is accessible for different options/arguments types.
 * Allows to save the reason why option/argument can(or can't) be omitted in command line.
 *
 * @see [SingleOption], [MultipleOption], [SingleArgument], [MultipleArgument].
 */
interface DefaultRequiredType {
    /**
     * Type of an entity with default konstue.
     */
    class Default : DefaultRequiredType

    /**
     * Type of an entity which konstue should always be provided in command line.
     */
    class Required : DefaultRequiredType

    /**
     * Type of entity which is optional and has no default konstue.
     */
    class None : DefaultRequiredType
}

/**
 * The base class for a command line argument or an option.
 */
abstract class CLIEntity<TResult> internal constructor(konst delegate: ArgumentValueDelegate<TResult>,
                                                       internal konst owner: CLIEntityWrapper) {
    /**
     * The konstue of the option or argument parsed from command line.
     *
     * Accessing this property before it gets its konstue will result in an exception.
     * You can use [konstueOrigin] property to find out whether the property has been already set.
     *
     * @see ArgumentValueDelegate.konstue
     */
    var konstue: TResult
        get() = delegate.konstue
        set(konstue) {
            check((delegate as ParsingValue<*, *>).konstueOrigin != ArgParser.ValueOrigin.UNDEFINED) {
                "Resetting konstue of option/argument is only possible after parsing command line arguments." +
                        " ArgParser.parse(...) method should be called before"
            }
            delegate.konstue = konstue
        }

    /**
     * The origin of the option/argument konstue.
     */
    konst konstueOrigin: ArgParser.ValueOrigin
        get() = (delegate as ParsingValue<*, *>).konstueOrigin

    private var delegateProvided = false

    /**
     * Returns the delegate object for property delegation and initializes it with the name of the delegated property.
     *
     * This operator makes it possible to delegate a property to this instance. It returns [delegate] object
     * to be used as an actual delegate and uses the name of the delegated property to initialize the full name
     * of the option/argument if it wasn't done during construction of that option/argument.
     *
     * @throws IllegalStateException in case of trying to use same delegate several times.
     */
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ArgumentValueDelegate<TResult> {
        check(!delegateProvided) {
            "There is already used delegate for ${(delegate as ParsingValue<*, *>).descriptor.fullName}."
        }
        (delegate as ParsingValue<*, *>).provideName(prop.name)
        delegateProvided = true
        return delegate
    }
}

/**
 * The base class for command line arguments.
 *
 * You can use [ArgParser.argument] function to declare an argument.
 */
abstract class Argument<TResult> internal constructor(delegate: ArgumentValueDelegate<TResult>,
                                                      owner: CLIEntityWrapper): CLIEntity<TResult>(delegate, owner)

/**
 * The base class of an argument with a single konstue.
 *
 * A non-optional argument or an optional argument with a default konstue is represented with the [SingleArgument] inheritor.
 * An optional argument having nullable konstue is represented with the [SingleNullableArgument] inheritor.
 */
// TODO: investigate if we can collapse two inheritors into the single base class and specialize extensions by TResult upper bound
abstract class AbstractSingleArgument<T: Any, TResult, DefaultRequired: DefaultRequiredType> internal constructor(
    delegate: ArgumentValueDelegate<TResult>,
    owner: CLIEntityWrapper):
    Argument<TResult>(delegate, owner) {
    /**
     * Check descriptor for this kind of argument.
     */
    internal fun checkDescriptor(descriptor: ArgDescriptor<*, *>) {
        if (descriptor.number == null || descriptor.number > 1) {
            failAssertion("Argument with single konstue can't be initialized with descriptor for multiple konstues.")
        }
    }
}

/**
 * A non-optional argument or an optional argument with a default konstue.
 *
 * The [konstue] of such argument is non-null.
 */
class SingleArgument<T : Any, DefaultRequired: DefaultRequiredType> internal constructor(descriptor: ArgDescriptor<T, T>,
                                                   owner: CLIEntityWrapper):
    AbstractSingleArgument<T, T, DefaultRequired>(ArgumentSingleValue(descriptor), owner) {
    init {
        checkDescriptor(descriptor)
    }
}

/**
 * An optional argument with nullable [konstue].
 */
class SingleNullableArgument<T : Any> internal constructor(descriptor: ArgDescriptor<T, T>, owner: CLIEntityWrapper):
        AbstractSingleArgument<T, T?, DefaultRequiredType.None>(ArgumentSingleNullableValue(descriptor), owner) {
    init {
        checkDescriptor(descriptor)
    }
}

/**
 * An argument that allows several konstues to be provided in command line string.
 *
 * The [konstue] property of such argument has type `List<T>`.
 */
class MultipleArgument<T : Any, DefaultRequired: DefaultRequiredType> internal constructor(
    descriptor: ArgDescriptor<T, List<T>>, owner: CLIEntityWrapper):
        Argument<List<T>>(ArgumentMultipleValues(descriptor), owner) {
    init {
        if (descriptor.number != null && descriptor.number < 2) {
            failAssertion("Argument with multiple konstues can't be initialized with descriptor for single one.")
        }
    }
}

/**
 * Allows the argument to have several konstues specified in command line string.
 *
 * @param number the exact number of konstues expected for this argument, but at least 2.
 *
 * @throws IllegalArgumentException if number of konstues expected for this argument less than 2.
 */
fun <T : Any, TResult, DefaultRequired: DefaultRequiredType>
        AbstractSingleArgument<T, TResult, DefaultRequired>.multiple(number: Int): MultipleArgument<T, DefaultRequired> {
    require(number >= 2) { "multiple() modifier with konstue less than 2 is unavailable. It's already set to 1." }
    konst newArgument = with((delegate.cast<ParsingValue<T, T>>()).descriptor as ArgDescriptor) {
        MultipleArgument<T, DefaultRequired>(ArgDescriptor(type, fullName, number, description, listOfNotNull(defaultValue),
                required, deprecatedWarning), owner)
    }
    owner.entity = newArgument
    return newArgument
}

/**
 * Allows the last argument to take all the trailing konstues in command line string.
 */
fun <T : Any, TResult, DefaultRequired: DefaultRequiredType> AbstractSingleArgument<T, TResult, DefaultRequired>.vararg():
        MultipleArgument<T, DefaultRequired> {
    konst newArgument = with((delegate.cast<ParsingValue<T, T>>()).descriptor as ArgDescriptor) {
        MultipleArgument<T, DefaultRequired>(ArgDescriptor(type, fullName, null, description, listOfNotNull(defaultValue),
                required, deprecatedWarning), owner)
    }
    owner.entity = newArgument
    return newArgument
}

/**
 * Specifies the default konstue for the argument, that will be used when no konstue is provided for the argument
 * in command line string.
 *
 * Argument becomes optional, because konstue for it is set even if it isn't provided in command line.
 *
 * @param konstue the default konstue.
 */
fun <T: Any> SingleNullableArgument<T>.default(konstue: T): SingleArgument<T, DefaultRequiredType.Default> {
    konst newArgument = with((delegate.cast<ParsingValue<T, T>>()).descriptor as ArgDescriptor) {
        SingleArgument<T, DefaultRequiredType.Default>(ArgDescriptor(type, fullName, number, description, konstue,
            false, deprecatedWarning), owner)
    }
    owner.entity = newArgument
    return newArgument
}

/**
 * Specifies the default konstue for the argument with multiple konstues, that will be used when no konstues are provided
 * for the argument in command line string.
 *
 * Argument becomes optional, because konstue for it is set even if it isn't provided in command line.
 *
 * @param konstue the default konstue, must be a non-empty collection.
 */
fun <T: Any> MultipleArgument<T, DefaultRequiredType.None>.default(konstue: Collection<T>):
        MultipleArgument<T, DefaultRequiredType.Default> {
    require (konstue.isNotEmpty()) { "Default konstue for argument can't be empty collection." }
    konst newArgument = with((delegate.cast<ParsingValue<T, List<T>>>()).descriptor as ArgDescriptor) {
        MultipleArgument<T, DefaultRequiredType.Default>(ArgDescriptor(type, fullName, number, description, konstue.toList(),
                required, deprecatedWarning), owner)
    }
    owner.entity = newArgument
    return newArgument
}

/**
 * Allows the argument to have no konstue specified in command line string.
 *
 * The konstue of the argument is `null` in case if no konstue was specified in command line string.
 *
 * Note that only trailing arguments can be optional, i.e. no required arguments can follow optional ones.
 */
fun <T: Any> SingleArgument<T, DefaultRequiredType.Required>.optional(): SingleNullableArgument<T> {
    konst newArgument = with((delegate.cast<ParsingValue<T, T>>()).descriptor as ArgDescriptor) {
        SingleNullableArgument(ArgDescriptor(type, fullName, number, description, defaultValue,
                false, deprecatedWarning), owner)
    }
    owner.entity = newArgument
    return newArgument
}

/**
 * Allows the argument with multiple konstues to have no konstues specified in command line string.
 *
 * The konstue of the argument is an empty list in case if no konstue was specified in command line string.
 *
 * Note that only trailing arguments can be optional: no required arguments can follow the optional ones.
 */
fun <T: Any> MultipleArgument<T, DefaultRequiredType.Required>.optional(): MultipleArgument<T, DefaultRequiredType.None> {
    konst newArgument = with((delegate.cast<ParsingValue<T, List<T>>>()).descriptor as ArgDescriptor) {
        MultipleArgument<T, DefaultRequiredType.None>(ArgDescriptor(type, fullName, number, description,
                defaultValue?.toList() ?: listOf(), false, deprecatedWarning), owner)
    }
    owner.entity = newArgument
    return newArgument
}

internal fun failAssertion(message: String): Nothing = throw AssertionError(message)

internal inline fun <reified T : Any> Any?.cast(): T = this as T