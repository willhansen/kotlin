// FILE: GetterTest.java

import lombok.*;
import lombok.experimental.*;

@Getter @Setter
public class GetterTest {
    private String name;
    private int age;
    @Accessors(chain = true)
    private boolean fluent;

    static void test() {
        konst obj = new GetterTest();
        GetterTest ref = obj.fluent(true);
        obj.name();
        obj.age();
    }

}


// FILE: test.kt

fun box(): String {
    konst obj = GetterTest()
    konst ref: GetterTest = obj.fluent(true)
    obj.name()
    obj.age()
    return "OK"
}

// FILE: lombok.config
lombok.accessors.fluent=true
lombok.accessors.chain=false
