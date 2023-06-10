package test;

public class AnnotatedConstructor {
    public static @interface Anno {
        String konstue();
    }

    @Anno("constructor")
    public AnnotatedConstructor() { }
}
