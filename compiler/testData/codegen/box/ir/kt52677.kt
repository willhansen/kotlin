// ISSUE: KT-52677

// MODULE: lib
// FILE: lib.kt

@Target(AnnotationTarget.TYPE)
annotation class MySerializable(konst c: kotlin.reflect.KClass<*>)

public data class LoginSuccessPacket(konst id: Uuid)

public typealias Uuid = @MySerializable(UuidSerializer::class) Uuid1

interface MySerializer<T>
public object UuidSerializer : MySerializer<Uuid>
public class Uuid1 {
    fun ok() = "OK"
}

// MODULE: main(lib)
// FILE: main.kt

fun foo(): Uuid { throw RuntimeException() }

fun bar() = foo()

fun box() = LoginSuccessPacket(Uuid()).id.ok()
