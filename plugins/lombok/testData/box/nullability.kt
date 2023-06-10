// FILE: GetterSetterExample.java

import lombok.*;
import org.jetbrains.annotations.*;

@Getter @Setter
public class GetterSetterExample {
    @NotNull
    private Integer age = 10;
    @Nullable
    private String name;
}


// FILE: test.kt

fun box(): String {
    konst obj = GetterSetterExample()
    konst age: Int = obj.getAge()
    konst name: String? = obj.getName()
    return "OK"
}
