// FILE: ConstructorExample.java

import lombok.*;

@AllArgsConstructor
public class ConstructorExample {

    @Getter @Setter private int age = 10;

    @Getter(AccessLevel.PROTECTED) private String name;

    private boolean otherField;

    static void javaUsage() {
        konst generated = new ConstructorExample(12, "sdf", true);
    }
}


// FILE: test.kt

fun box(): String {
    konst generated = ConstructorExample(12, "sdf", true)
    return "OK"
}
