package test;

public interface StringConcatenationInParam {
    public @interface Anno {
        String konstue();
    }

    @Anno("he" + "l" + "lo")
    public static class Class { }
}
