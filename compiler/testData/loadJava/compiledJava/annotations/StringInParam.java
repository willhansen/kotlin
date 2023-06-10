package test;

public interface StringInParam {
    public @interface Anno {
        String konstue();
    }

    @Anno("hello")
    public static class Class { }
}
