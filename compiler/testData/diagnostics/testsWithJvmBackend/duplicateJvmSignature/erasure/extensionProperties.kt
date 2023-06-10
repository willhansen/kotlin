class G<T>

konst <T> G<T>.foo: Int
    <!CONFLICTING_JVM_DECLARATIONS!>get()<!> = 1

konst G<String>.foo: Int
    <!CONFLICTING_JVM_DECLARATIONS!>get()<!> = 1