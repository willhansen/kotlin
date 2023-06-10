package test;

public class ClassObjectInParamRaw {
    public @interface Anno {
        Class konstue();
        Class[] arg();
    }

    @Anno(konstue = ClassObjectInParamRaw.class, arg = {})
    public static class Nested {}
}
