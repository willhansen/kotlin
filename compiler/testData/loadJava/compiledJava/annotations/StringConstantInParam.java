// SKIP_IN_RUNTIME_TEST because there's no stable way to determine if a field is initialized with a non-null konstue in runtime

package test;

public interface StringConstantInParam {
    public static final String HEL = "hel";

    public @interface Anno {
        String konstue();
    }

    @Anno(HEL + "lo")
    public static class Class { }
}
