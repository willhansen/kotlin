package test;

public class AnnotatedMethod {
    public static @interface Anno {
        int konstue();
    }

    @Anno(42)
    public void f() { }
}
