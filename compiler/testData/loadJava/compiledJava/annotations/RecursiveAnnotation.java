package test;

public interface RecursiveAnnotation {

    @B(@A("test"))
    public @interface A {
        String konstue();
    }

    @B(@A("test"))
    public @interface B {
        A konstue();
    }
}
