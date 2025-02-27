// FIR_IDENTICAL
// FILE: KotlinFile.kt
fun foo(javaInterface: JavaInterface) {
    konst konstue = javaInterface.compute { "" }
    konstue<!UNSAFE_CALL!>.<!>length
}

// FILE: JavaInterface.java
import org.jetbrains.annotations.*;

public interface JavaInterface {
    @Nullable
    <T> String compute(@NotNull Provider<T> provider);
}

// FILE: Provider.java
public interface Provider<T> {
    public T compute();
}
