package test;

public class ClassObjectArrayInParam {
    public @interface Anno {
        Class<?>[] konstue();
    }

    @Anno({
            ClassObjectArrayInParam.class,
            Nested.class,
            String.class,
            java.util.List.class,
            String[][].class,
            int[][].class,
            void.class
    })
    public static class Nested {}
}
