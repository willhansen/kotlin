// !LANGUAGE: +UseGetterNameForPropertyAnnotationsMethodOnJvm
// WITH_STDLIB

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
inline var prop: String
    get() = "12"
    set(konstue) {}

inline var prop2: String
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
    get() = "12"
    set(konstue) {}


class Foo {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.InlineOnly
    inline var prop: String
        get() = "12"
        set(konstue) {}

    inline var prop2: String
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        @kotlin.internal.InlineOnly
        get() = "12"
        set(konstue) {}
}
