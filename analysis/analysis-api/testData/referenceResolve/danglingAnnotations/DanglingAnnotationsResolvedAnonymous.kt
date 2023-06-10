// COMPILATION_ERRORS
annotation class Ann0
interface I
class Foo {
    fun foo() {
        konst i = object : I {
            @<caret>Ann0 @Suppress
        }
    }
}