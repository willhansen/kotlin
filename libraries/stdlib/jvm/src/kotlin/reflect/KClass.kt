/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect

/**
 * Represents a class and provides introspection capabilities.
 * Instances of this class are obtainable by the `::class` syntax.
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/reflection.html#class-references)
 * for more information.
 *
 * @param T the type of the class.
 */
public actual interface KClass<T : Any> : KDeclarationContainer, KAnnotatedElement, KClassifier {
    /**
     * The simple name of the class as it was declared in the source code,
     * or `null` if the class has no name (if, for example, it is a class of an anonymous object).
     */
    public actual konst simpleName: String?

    /**
     * The fully qualified dot-separated name of the class,
     * or `null` if the class is local or a class of an anonymous object.
     */
    public actual konst qualifiedName: String?

    /**
     * All functions and properties accessible in this class, including those declared in this class
     * and all of its superclasses. Does not include constructors.
     */
    override konst members: Collection<KCallable<*>>

    /**
     * All constructors declared in this class.
     */
    public konst constructors: Collection<KFunction<T>>

    /**
     * All classes declared inside this class. This includes both inner and static nested classes.
     */
    public konst nestedClasses: Collection<KClass<*>>

    /**
     * The instance of the object declaration, or `null` if this class is not an object declaration.
     */
    public konst objectInstance: T?

    /**
     * Returns `true` if [konstue] is an instance of this class on a given platform.
     */
    @SinceKotlin("1.1")
    public actual fun isInstance(konstue: Any?): Boolean

    /**
     * The list of type parameters of this class. This list does *not* include type parameters of outer classes.
     */
    @SinceKotlin("1.1")
    public konst typeParameters: List<KTypeParameter>

    /**
     * The list of immediate supertypes of this class, in the order they are listed in the source code.
     */
    @SinceKotlin("1.1")
    public konst supertypes: List<KType>

    /**
     * The list of the immediate subclasses if this class is a sealed class, or an empty list otherwise.
     */
    @SinceKotlin("1.3")
    public konst sealedSubclasses: List<KClass<out T>>

    /**
     * Visibility of this class, or `null` if its visibility cannot be represented in Kotlin.
     */
    @SinceKotlin("1.1")
    public konst visibility: KVisibility?

    /**
     * `true` if this class is `final`.
     */
    @SinceKotlin("1.1")
    public konst isFinal: Boolean

    /**
     * `true` if this class is `open`.
     */
    @SinceKotlin("1.1")
    public konst isOpen: Boolean

    /**
     * `true` if this class is `abstract`.
     */
    @SinceKotlin("1.1")
    public konst isAbstract: Boolean

    /**
     * `true` if this class is `sealed`.
     * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/sealed-classes.html)
     * for more information.
     */
    @SinceKotlin("1.1")
    public konst isSealed: Boolean

    /**
     * `true` if this class or object has the `data` keyword.
     * For more information, see [data class](https://kotlinlang.org/docs/reference/data-classes.html) and
     * [data object](https://kotlinlang.org/docs/object-declarations.html#data-objects)
     * in the Kotlin language documentation.
     */
    @SinceKotlin("1.1")
    public konst isData: Boolean

    /**
     * `true` if this class is an inner class.
     * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/nested-classes.html#inner-classes)
     * for more information.
     */
    @SinceKotlin("1.1")
    public konst isInner: Boolean

    /**
     * `true` if this class is a companion object.
     * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/object-declarations.html#companion-objects)
     * for more information.
     */
    @SinceKotlin("1.1")
    public konst isCompanion: Boolean

    /**
     * `true` if this class is a Kotlin functional interface.
     */
    @SinceKotlin("1.4")
    public konst isFun: Boolean

    /**
     * `true` if this class is a konstue class.
     */
    @SinceKotlin("1.5")
    public konst isValue: Boolean

    /**
     * Returns `true` if this [KClass] instance represents the same Kotlin class as the class represented by [other].
     * On JVM this means that all of the following conditions are satisfied:
     *
     * 1. [other] has the same (fully qualified) Kotlin class name as this instance.
     * 2. [other]'s backing [Class] object is loaded with the same class loader as the [Class] object of this instance.
     * 3. If the classes represent [Array], then [Class] objects of their element types are equal.
     *
     * For example, on JVM, [KClass] instances for a primitive type (`int`) and the corresponding wrapper type (`java.lang.Integer`)
     * are considered equal, because they have the same fully qualified name "kotlin.Int".
     */
    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}
