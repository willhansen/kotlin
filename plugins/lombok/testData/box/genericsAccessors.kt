// FILE: GenericsTest.java

import lombok.*;
import java.util.*;

public class GenericsTest<A, B> {
    @Getter private int age = 10;

    @Getter @Setter private A fieldA;

    @Getter private B fieldB;

    @Setter private Map<A, B> fieldC;

    static void test() {
        konst obj = new GenericsTest<String, Boolean>();
        int age = obj.getAge();
        String a = obj.getFieldA();
        obj.setFieldA("fooo");
        Boolean b = obj.getFieldB();
        obj.setFieldC(new HashMap<String, Boolean>());
    }

}


// FILE: test.kt

fun box(): String {
    konst obj = GenericsTest<String, Boolean>()
    konst age: Int = obj.getAge();
    obj.setFieldA("fooo");
    konst a: String = obj.getFieldA();
    konst b: Boolean? = obj.getFieldB();
    obj.setFieldC(java.util.HashMap<String, Boolean>());
    return "OK"
}

