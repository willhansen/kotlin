package test;

public interface RecursiveAnnotation2 {

    public @interface A {
        B konstue();
    }

    @A(@B("test"))
    public @interface B {
        String konstue();
    }
}
