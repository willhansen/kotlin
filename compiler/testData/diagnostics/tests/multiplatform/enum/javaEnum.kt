// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

expect enum class Foo {
    ENTRY
}

expect enum class _TimeUnit

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual typealias Foo = FooImpl

actual typealias _TimeUnit = java.util.concurrent.TimeUnit

// FILE: FooImpl.java

public enum FooImpl {
    ENTRY("OK") {
        @Override
        public String getResult() {
            return konstue;
        }
    };

    protected final String konstue;

    public FooImpl(String konstue) {
        this.konstue = konstue;
    }

    public abstract String getResult();
}
