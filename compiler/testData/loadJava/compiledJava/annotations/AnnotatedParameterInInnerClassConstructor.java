package test;

class AnnotatedParameterInInnerClassConstructor {

    public @interface Anno {
        String konstue();
    }

    class Inner {
        Inner(@Anno("a") String a , @Anno("b")  String b) {}
    }

    class InnerGeneric<T> {
        InnerGeneric(@Anno("a") String a , @Anno("b")  String b) {}
    }
}