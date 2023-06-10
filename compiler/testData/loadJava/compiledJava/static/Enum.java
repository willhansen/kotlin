package test;

public enum Enum {
    A,
    B,
    C;

    public static class Nested {
        void foo() {}
        void konstues() {}
    }

    public class Inner {
        void bar() {}
        void konstueOf(String s) {}
    }
}