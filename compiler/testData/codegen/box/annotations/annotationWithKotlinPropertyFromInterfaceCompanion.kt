// TARGET_BACKEND: JVM

// FILE: JavaClass.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class JavaClass {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Foo {
        int konstue();
    }

    @Foo(KotlinInterface.FOO_INT)
    public String test() throws NoSuchMethodException {
        return KotlinInterface.FOO_STRING +
               JavaClass.class.getMethod("test").getAnnotation(Foo.class).konstue();
    }
}

// FILE: KotlinInterface.kt

interface KotlinInterface {
    companion object {
        const konst FOO_INT: Int = 10
        const  konst FOO_STRING: String = "OK"
    }
}

fun box(): String {
    konst test = JavaClass().test()
    return if (test == "OK10") "OK" else "fail : $test"
}
