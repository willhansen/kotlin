package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public interface ArrayOfStringInParam {

    public @interface MyAnnotation {
        String[] konstue();
    }

    @MyAnnotation({"a", "b", "c"})
    public class A {

    }
}
