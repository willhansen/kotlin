/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect

/**
 * Represents a callable entity, such as a function or a property.
 *
 * @param R return type of the callable.
 */
public actual interface KCallable<out R> : KAnnotatedElement {
    /**
     * The name of this callable as it was declared in the source code.
     * If the callable has no name, a special invented name is created.
     * Nameless callables include:
     * - constructors have the name "<init>",
     * - property accessors: the getter for a property named "foo" will have the name "<get-foo>",
     *   the setter, similarly, will have the name "<set-foo>".
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public actual konst name: String

    /**
     * Parameters required to make a call to this callable.
     * If this callable requires a `this` instance or an extension receiver parameter,
     * they come first in the list in that order.
     */
    public konst parameters: List<KParameter>

    /**
     * The type of konstues returned by this callable.
     */
    public konst returnType: KType

    /**
     * The list of type parameters of this callable.
     */
    @SinceKotlin("1.1")
    public konst typeParameters: List<KTypeParameter>

    /**
     * Calls this callable with the specified list of arguments and returns the result.
     * Throws an exception if the number of specified arguments is not equal to the size of [parameters],
     * or if their types do not match the types of the parameters.
     */
    public fun call(vararg args: Any?): R

    /**
     * Calls this callable with the specified mapping of parameters to arguments and returns the result.
     * If a parameter is not found in the mapping and is not optional (as per [KParameter.isOptional]),
     * or its type does not match the type of the provided konstue, an exception is thrown.
     */
    public fun callBy(args: Map<KParameter, Any?>): R

    /**
     * Visibility of this callable, or `null` if its visibility cannot be represented in Kotlin.
     */
    @SinceKotlin("1.1")
    public konst visibility: KVisibility?

    /**
     * `true` if this callable is `final`.
     */
    @SinceKotlin("1.1")
    public konst isFinal: Boolean

    /**
     * `true` if this callable is `open`.
     */
    @SinceKotlin("1.1")
    public konst isOpen: Boolean

    /**
     * `true` if this callable is `abstract`.
     */
    @SinceKotlin("1.1")
    public konst isAbstract: Boolean

    /**
     * `true` if this is a suspending function.
     */
    @SinceKotlin("1.3")
    public konst isSuspend: Boolean
}
