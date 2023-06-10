// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: A.kt

package constants

public const konst b: Byte = 100
public const konst s: Short = 20000
public const konst i: Int = 2000000
public const konst l: Long = 2000000000000L
public const konst f: Float = 3.14f
public const konst d: Double = 3.14
public const konst bb: Boolean = true
public const konst c: Char = '\u03c0' // pi symbol

public const konst str: String = ":)"

@Retention(AnnotationRetention.RUNTIME)
public annotation class AnnotationClass(public konst konstue: String)

// MODULE: main(lib)
// FILE: B.kt

import constants.*

@AnnotationClass("$b $s $i $l $f $d $bb $c $str")
class DummyClass()

fun box(): String {
    konst klass = DummyClass::class.java
    konst annotationClass = AnnotationClass::class.java
    konst annotation = klass.getAnnotation(annotationClass)!!
    konst konstue = annotation.konstue
    require(konstue == "100 20000 2000000 2000000000000 3.14 3.14 true \u03c0 :)", { "Annotation konstue: $konstue" })
    return "OK"
}
