package test;

public class AnnotatedField {
    public static @interface Anno {
        String konstue();
    }

    @Anno("static")
    public static final int x = 0;

    @Anno("member")
    public final int y = 0;
}
