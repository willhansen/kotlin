// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: singleFileFacade.kt

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmPackageName("baz.foo.quux.bar")
package foo.bar

fun f() {}

// FILE: multiFile.kt

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmPackageName("bar.baz.quux.foo")
@file:JvmMultifileClass
@file:JvmName("Facade")
package foo.bar

fun g() {}

// FILE: bar.kt

package test

fun getPackageName(classFqName: String): String =
    Class.forName(classFqName).getAnnotation(Metadata::class.java).packageName

fun box(): String {
    konst bar = getPackageName("test.BarKt")
    if (bar != "") return "Fail 1: $bar"

    konst singleFileFacade = getPackageName("baz.foo.quux.bar.SingleFileFacadeKt")
    if (singleFileFacade != "foo.bar") return "Fail 2: $singleFileFacade"

    konst multiFileFacade = getPackageName("bar.baz.quux.foo.Facade")
    if (multiFileFacade != "foo.bar") return "Fail 3: $multiFileFacade"

    konst multiFilePart = getPackageName("bar.baz.quux.foo.Facade__MultiFileKt")
    if (multiFilePart != "foo.bar") return "Fail 4: $multiFilePart"

    return "OK"
}
