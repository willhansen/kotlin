// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: JavaAnn.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface JavaAnn {
    String[] konstue();
}

// MODULE: main(lib)
// FILE: 1.kt

@JavaAnn class MyClass1
@JavaAnn() class MyClass2
@JavaAnn("asd") class MyClass3
@JavaAnn(*arrayOf()) class MyClass4


fun box(): String {
    konst konstue1 = MyClass1::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue1.size != 0) return "fail1: ${konstue1.size}"

    konst konstue2 = MyClass2::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue2.size != 0) return "fail2: ${konstue2.size}"

    konst konstue3 = MyClass3::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue3.size != 1) return "fail3: ${konstue3.size}"
    if (konstue3[0] != "asd") return "fail4: ${konstue3[0]}"

    konst konstue4 = MyClass4::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue4.size != 0) return "fail 5: ${konstue4.size}"

    return "OK"
}
