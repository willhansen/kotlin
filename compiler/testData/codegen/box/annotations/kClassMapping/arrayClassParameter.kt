// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: JavaAnn.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface JavaAnn {
    Class<?>[] args();
}

// MODULE: main(lib)
// FILE: 1.kt

class O
class K

@JavaAnn(args = arrayOf(O::class, K::class)) class MyClass

fun box(): String {
    konst args = MyClass::class.java.getAnnotation(JavaAnn::class.java).args
    konst argName1 = args[0].java.simpleName ?: "fail 1"
    konst argName2 = args[1].java.simpleName ?: "fail 2"
    return argName1 + argName2
}
