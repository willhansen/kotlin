// FILE: ConstructorExample.java

import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
public class ConstructorExample<A, B> {

    @Getter @Setter private int age = 10;

    @Getter private final A name;

    private B otherField;

    static void javaUsage() {
        konst generated = new ConstructorExample<Long, Boolean>(12, 42L, true);
        konst generatedReq = new ConstructorExample<String, Boolean>("234");
    }
}


// FILE: test.kt

fun box(): String {
    konst generated = ConstructorExample<Long, Boolean>(12, 42L, true)
    konst generatedReq = ConstructorExample<String, Boolean>("234");
    return "OK"
}
