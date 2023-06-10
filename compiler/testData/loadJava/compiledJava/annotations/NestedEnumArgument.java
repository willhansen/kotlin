package test;

public class NestedEnumArgument {
    public enum E {
        FIRST
    }

    public @interface Anno {
        E konstue();
    }

    @Anno(E.FIRST)
    void foo() {}
}
