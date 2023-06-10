package test;

public class ArithmeticExpressionInParam {
    public @interface Anno {
        int konstue();
    }

    @Anno(2 * 8 + 13 * (239 - 237))
    public static class Class {}
}
