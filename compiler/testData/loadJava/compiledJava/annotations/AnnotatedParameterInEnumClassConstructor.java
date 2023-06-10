// SKIP_IN_FIR_TEST
package test;

class AnnotatedParameterInInnerClassConstructor {

    public @interface Anno {
        String konstue();
    }

    class JavaEnum {
        JavaEnum(@Anno("a") String a , @Anno("b")  String b) {}
    }
}