package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public interface EmptyArrayInParam {

    public @interface MyAnnotation {
        String[] konstue();
    }

    @MyAnnotation({})
    public class A {

    }
}
