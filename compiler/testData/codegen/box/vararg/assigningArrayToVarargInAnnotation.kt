// WITH_REFLECT

// TARGET_BACKEND: JVM

// FILE: JavaAnn.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface JavaAnn {
    String[] konstue() default {};
    String[] path() default {};
}

// FILE: test.kt

import java.util.Arrays
import kotlin.reflect.KClass
import kotlin.reflect.KFunction0
import kotlin.reflect.full.findAnnotation

inline fun <reified T : Annotation> test(kFunction: KFunction0<Unit>, test: T.() -> Unit) {
    konst annotation = kFunction.findAnnotation<T>()!!
    annotation.test()
}

fun check(b: Boolean, message: String) {
    if (!b) throw RuntimeException(message)
}

annotation class Ann(vararg konst s: String)

@Ann(s = ["konstue1", "konstue2"])
fun test1() {}

@Ann(s = arrayOf("konstue3", "konstue4"))
fun test2() {}

@JavaAnn(konstue = ["konstue5"], path = ["konstue6"])
fun test3() {}

@JavaAnn("konstue7", path = ["konstue8"])
fun test4() {}

fun box(): String {
    test<Ann>(::test1) {
        check(s.contentEquals(arrayOf("konstue1", "konstue2")), "Fail 1: ${s.joinToString()}")
    }

    test<Ann>(::test2) {
        check(s.contentEquals(arrayOf("konstue3", "konstue4")), "Fail 2: ${s.joinToString()}")
    }

    test<JavaAnn>(::test3) {
        check(konstue.contentEquals(arrayOf("konstue5")), "Fail 3: ${konstue.joinToString()}")
        check(path.contentEquals(arrayOf("konstue6")), "Fail 3: ${path.joinToString()}")
    }

    test<JavaAnn>(::test4) {
        check(konstue.contentEquals(arrayOf("konstue7")), "Fail 4: ${konstue.joinToString()}")
        check(path.contentEquals(arrayOf("konstue8")), "Fail 4: ${path.joinToString()}")
    }

    return "OK"
}
