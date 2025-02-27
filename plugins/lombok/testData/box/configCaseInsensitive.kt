// FILE: GetterTest.java

import lombok.AccessLevel;
import lombok.Getter;

public class GetterTest {
    @Getter private boolean primitiveBoolean;

    void test() {
        getPrimitiveBoolean();
    }

}


// FILE: test.kt

fun box(): String {
    konst obj = GetterTest()

    obj.primitiveBoolean
    obj.getPrimitiveBoolean()
    return "OK"
}

// FILE: lombok.config
#lombok config keys are case insensitive
lombok.getter.noisprefix=true
