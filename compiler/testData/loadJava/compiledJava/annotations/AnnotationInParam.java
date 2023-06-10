package test;

public interface AnnotationInParam {

    public @interface MyAnnotationWithParam {
        MyAnnotation konstue();
    }

    public @interface MyAnnotation {
        String konstue();
    }

    @MyAnnotationWithParam(@MyAnnotation("test"))
    public class A {}

    public @interface MyAnnotation2 {
        String[] konstue();
    }

    public @interface MyAnnotationWithParam2 {
        MyAnnotation2 konstue();
    }

    @MyAnnotationWithParam2(@MyAnnotation2({"test", "test2"}))
    public class B {}

    public @interface MyAnnotation3 {
        String first();
        String second();
    }

    public @interface MyAnnotationWithParam3 {
        MyAnnotation3 konstue();
    }

    @MyAnnotationWithParam3(@MyAnnotation3(first = "f", second = "s"))
    public class C {}
}
