// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: JavaAnn.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface JavaAnn {
    String[] konstue() default {"d1", "d2"};
}

// MODULE: main(lib)
// FILE: 1.kt

@JavaAnn class MyClass1
@JavaAnn() class MyClass2
@JavaAnn("asd") class MyClass3
@JavaAnn(*arrayOf()) class MyClass4


fun box(): String {
    konst konstue1 = MyClass1::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue1.size != 2) return "fail1: ${konstue1.size}"
    if (konstue1[0] != "d1") return "fail2: ${konstue1[0]}"
    if (konstue1[1] != "d2") return "fail3: ${konstue1[1]}"

    konst konstue2 = MyClass2::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue2.size != 2) return "fail4: ${konstue2.size}"
    if (konstue2[0] != "d1") return "fail5: ${konstue2[0]}"
    if (konstue2[1] != "d2") return "fail6: ${konstue2[1]}"

    konst konstue3 = MyClass3::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue3.size != 1) return "fail7: ${konstue3.size}"
    if (konstue3[0] != "asd") return "fail8: ${konstue3[0]}"

    konst konstue4 = MyClass4::class.java.getAnnotation(JavaAnn::class.java).konstue
    if (konstue4.size != 0) return "fail 9: ${konstue4.size}"

    return "OK"
}
