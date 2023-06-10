// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: JavaAnn.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface JavaAnn {
    String konstue();
}

// MODULE: main(lib)
// FILE: 1.kt

@JavaAnn("konstue") class MyClass

fun box(): String {
    konst ann = MyClass::class.java.getAnnotation(JavaAnn::class.java)
    if (ann == null) return "fail: cannot find Ann on MyClass}"
    if (ann.konstue != "konstue") return "fail: annotation parameter i should be 'konstue', but was ${ann.konstue}"
    return "OK"
}
