class G<T>

<!CONFLICTING_JVM_DECLARATIONS!>konst <T> G<T>.foo: Int<!>
    get() = 1

<!CONFLICTING_JVM_DECLARATIONS!>konst G<String>.foo: Int<!>
    get() = 1
