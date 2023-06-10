// WITH_STDLIB

@file:[JvmName("Foo") JvmMultifileClass]
package test

fun foo() {
    "".extProp
}

inline konst <reified Z> Z.extProp: String
    get() = "123"
