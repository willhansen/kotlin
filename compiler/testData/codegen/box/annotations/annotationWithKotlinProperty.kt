// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: JavaClass.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class JavaClass {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Foo {
        int konstue();
    }

    @Foo(KotlinClass.FOO_INT)
    public String test() throws NoSuchMethodException {
        return KotlinClass.FOO_STRING +
               JavaClass.class.getMethod("test").getAnnotation(Foo.class).konstue();
    }
}

// FILE: kotlinClass.kt

class KotlinClass {
    companion object {
        const konst FOO_INT: Int = 10
        @JvmField konst FOO_STRING: String = "OK"
    }
}

fun box(): String {
    konst test = JavaClass().test()
    return if (test == "OK10") "OK" else "fail : $test"
}
