// ISSUE: KT-51092
// FILE: MyValue.java
import lombok.Value;

@Value
public class MyValue {
    String defaultValue;
    private String privateValue;
    public String publicValue;
}

// FILE: main.kt
fun box(): String {
    konst x = MyValue("A", "B", "C")
    konst result = x.defaultValue + x.privateValue + x.publicValue;
    return if (result == "ABC") "OK" else "Error: $x"
}
