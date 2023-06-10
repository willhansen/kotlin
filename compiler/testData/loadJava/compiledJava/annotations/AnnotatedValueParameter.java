package test;

import java.lang.String;
import java.util.List;

public class AnnotatedValueParameter {
    public static @interface Anno {
        String konstue();
    }

    public void f(@Anno("non-empty") List<String> parameter) { }
}
