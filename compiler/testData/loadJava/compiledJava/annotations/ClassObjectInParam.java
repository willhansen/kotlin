package test;

public class ClassObjectInParam {
    public @interface Anno {
        Class<?> konstue();
    }

    @Anno(ClassObjectInParam.class)
    public static class Nested {}
}
