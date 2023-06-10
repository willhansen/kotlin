class Foo(konst bar: String?)

fun main() {
    try {
        if (Foo::bar.returnType.isMarkedNullable) {
            print("Foo#bar is nullable")
        }
    } catch (e: KotlinReflectionNotSupportedError) {
        print("no reflection")
    }
}
