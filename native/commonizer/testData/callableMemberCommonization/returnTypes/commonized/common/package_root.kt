expect class Planet(name: String, diameter: Double) {
    konst name: String
    konst diameter: Double
}

expect konst propertyWithInferredType1: Int
expect konst propertyWithInferredType2: String
expect konst propertyWithInferredType3: String
expect konst propertyWithInferredType4: Nothing?
expect konst propertyWithInferredType5: Planet

typealias C = Planet

expect konst property1: Int
expect konst property2: String
expect konst property3: Planet
expect konst property4: Planet
expect konst property5: Planet
expect konst property6: Planet
expect konst property7: C

expect fun function1(): Int
expect fun function2(): String
expect fun function3(): Planet
expect fun function4(): Planet
expect fun function5(): Planet
expect fun function6(): Planet
expect fun function7(): C

// Optimistic Number Commonization: KT-48455, KT-48568
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Int", "jvm: kotlin.Long"])
expect konst propertyWithMismatchedType1: Int
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Int", "jvm: kotlin.Short"])
expect konst propertyWithMismatchedType2: Short
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Int", "jvm: kotlin.Long"])
expect fun functionWithMismatchedType1(): Int
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Int", "jvm: kotlin.Short"])
expect fun functionWithMismatchedType2(): Short

expect class Box<T>(konstue: T) {
    konst konstue: T
}

expect class Fox()

expect fun functionWithTypeParametersInReturnType1(): Array<Int>
expect fun functionWithTypeParametersInReturnType3(): Array<String>
expect fun functionWithTypeParametersInReturnType4(): List<Int>
expect fun functionWithTypeParametersInReturnType6(): List<String>
expect fun functionWithTypeParametersInReturnType7(): Box<Int>
expect fun functionWithTypeParametersInReturnType9(): Box<String>
expect fun functionWithTypeParametersInReturnType10(): Box<Planet>
expect fun functionWithTypeParametersInReturnType12(): Box<Fox>

expect fun <T> functionWithUnsubstitutedTypeParametersInReturnType1(): T
expect fun <T> functionWithUnsubstitutedTypeParametersInReturnType2(): T
expect fun <T> functionWithUnsubstitutedTypeParametersInReturnType8(): Box<T>

expect class Outer<A>() {
    class Nested<B>() {
        class Nested<C>()
        inner class Inner<D>()
    }

    inner class Inner<E>() {
        inner class Inner<F>()
    }
}

expect fun <T> returnOuter(): Outer<T>
expect fun <T> returnOuterNested(): Outer.Nested<T>
expect fun <T> returnOuterNestedNested(): Outer.Nested.Nested<T>
expect fun <T, R> returnOuterInner(): Outer<T>.Inner<R>
expect fun <T, R, S> returnOuterInnerInner(): Outer<T>.Inner<R>.Inner<S>
