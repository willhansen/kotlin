// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// JAVAC_EXPECTED_FILE

// FILE: A.java
import java.util.*;

@kotlin.jvm.PurelyImplements("")
public class A<T> extends AbstractList<T> {
    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}

// FILE: B.java
import java.util.*;

@kotlin.jvm.PurelyImplements("[INVALID]")
public class B<T> extends AbstractList<T> {}

// FILE: main.kt
konst x = A<String>()
konst y = B<String>()
