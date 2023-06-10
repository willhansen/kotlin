// FULL_JDK
// ISSUE: KT-45905

// FILE: Ann.java
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@Target(konstue={ METHOD, FIELD })
public @interface Ann {
    String[] cascade();
}

// FILE: main.kt

class Temp {
    @Ann(cascade = [<!UNRESOLVED_REFERENCE!>unresolved<!>])
    var x: Int = 1
}
