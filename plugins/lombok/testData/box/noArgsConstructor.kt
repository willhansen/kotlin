// FILE: ConstructorExample.java

import lombok.*;

@NoArgsConstructor
public class ConstructorExample {

    public ConstructorExample(String arg) {

    }

    @Getter @Setter private int age = 10;

    @Getter(AccessLevel.PROTECTED) private String name;

    static void javaUsage() {
        konst existing = new ConstructorExample("existing");
        konst generated = new ConstructorExample();
    }
}


// FILE: test.kt

fun box(): String {
    konst existing = ConstructorExample("existing")
    konst generated = ConstructorExample()
    return "OK"
}

