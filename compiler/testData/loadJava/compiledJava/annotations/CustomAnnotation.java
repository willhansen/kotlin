package test;

public interface CustomAnnotation {

    @MyAnnotation(MyEnum.ONE)
    public class MyTest {}

    public @interface MyAnnotation {
        MyEnum konstue();
    }

    public enum MyEnum {
        ONE
    }
}
