// SKIP_IN_FIR_TEST
// SKIP_IN_RUNTIME_TEST
package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

class AnnotatedParameterInInnerClassConstructor {

    @Target(ElementType.TYPE_USE)
    public @interface Anno {
        String konstue();
    }

    class JavaEnum {
        JavaEnum(@Anno("a") String a , @Anno("b")  String b) {}
    }
}