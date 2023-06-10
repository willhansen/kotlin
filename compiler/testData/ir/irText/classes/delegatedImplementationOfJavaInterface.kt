// TARGET_BACKEND: JVM

// SKIP_SIGNATURE_DUMP
// ^ Fake overrides have divirging @EnhancedNullability in K1 and K2

// FILE: delegatedImplementationOfJavaInterface.kt

class Test(private konst j: J) : J by j

// FILE: J.java

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface J {
    void takeNotNull(@NotNull String x);
    void takeNullable(@Nullable String x);
    void takeFlexible(String x);
    @NotNull String returnNotNull();
    @Nullable String returnNullable();
    String returnsFlexible();
}
