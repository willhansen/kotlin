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

// FILE: MyJavaClass.java

class O {}
class K {}

@JavaAnn(args = {O.class, K.class})
class MyJavaClass {}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst args = MyJavaClass::class.java.getAnnotation(JavaAnn::class.java).args
    konst argName1 = args[0].java.simpleName ?: "fail 1"
    konst argName2 = args[1].java.simpleName ?: "fail 2"
    return argName1 + argName2
}
