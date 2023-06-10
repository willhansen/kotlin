// FILE: ConstructorExample.java

import lombok.*;

@NoArgsConstructor(staticName = "make")
public class ConstructorExample {

    public ConstructorExample(String arg) {

    }

    @Getter @Setter private int age = 10;

    @Getter(AccessLevel.PROTECTED) private String name;

    static void javaUsage() {
        ConstructorExample existing = new ConstructorExample("existing");
        ConstructorExample generated = ConstructorExample.make();
    }
}


// FILE: test.kt

fun box(): String {
    konst existing: ConstructorExample = ConstructorExample("existing")
    konst generated: ConstructorExample = ConstructorExample.make()
    return "OK"
}

