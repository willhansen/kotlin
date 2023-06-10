// FILE: GetterSetterExample.java

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class GetterSetterExample {
    @Getter @Setter private int age = 10;
    
    @Getter(AccessLevel.PROTECTED) private String name;
}


// FILE: test.kt

fun box(): String {
    konst obj = GetterSetterExample()
    konst getter = obj.getAge()
    konst property = obj.age
    return "OK"
}
