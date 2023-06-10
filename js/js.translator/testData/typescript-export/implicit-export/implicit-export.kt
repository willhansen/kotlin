// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// KJS_WITH_FULL_RUNTIME
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: qualified.kt
@file:JsQualifier("WebAssembly")
package qualified

external interface CompileError

// FILE: notQualified.kt
package notQualified

external interface Console

// FILE: member-properties.kt

package foo

import notQualified.Console
import qualified.CompileError

interface NonExportedInterface
interface NonExportedGenericInterface<T>
open class NonExportedType(konst konstue: Int)
open class NonExportedGenericType<T>(konst konstue: T)

@JsExport
interface ExportedInterface

@JsExport
fun producer(konstue: Int): NonExportedType {
    return NonExportedType(konstue)
}

@JsExport
fun consumer(konstue: NonExportedType): Int {
    return konstue.konstue
}

@JsExport
open class A(var konstue: NonExportedType) {
    fun <T: NonExportedType> increment(t: T): NonExportedType {
        return NonExportedType(konstue = t.konstue + 1)
    }
}

@JsExport
class B(v: Int) : NonExportedType(v)

@JsExport
class C : NonExportedInterface

@JsExport
class D : NonExportedInterface, ExportedInterface

@JsExport
class E : NonExportedType(42), ExportedInterface

@JsExport
class F : A(NonExportedType(42)), NonExportedInterface

@JsExport
class G : NonExportedGenericInterface<NonExportedType>

@JsExport
class H : NonExportedGenericType<NonExportedType>(NonExportedType(42))

@JsExport
fun baz(a: Int): kotlin.js.Promise<Int> {
    return kotlin.js.Promise<Int> { res, rej -> res(a) }
}

@JsExport
fun bar(): Throwable {
    return Throwable("Test Error")
}

@JsExport
konst console: Console
    get() = js("console")

@JsExport
konst error: CompileError
    get() = js("{}")

typealias NotExportedTypeAlias = NonExportedGenericInterface<NonExportedType>

@JsExport
fun functionWithTypeAliasInside(x: NotExportedTypeAlias): NotExportedTypeAlias {
    return x
}

@JsExport
class TheNewException: Throwable()

// Recursive definition KT-57356
@JsExport
interface Service<Self : Service<Self, TEvent>, in TEvent : Event<Self>>

@JsExport
interface Event<out TService : Service<out TService, *>>

class SomeService : Service<SomeService, SomeEvent>
class SomeEvent : Event<SomeService>

@JsExport
class SomeServiceRequest : Service<SomeService, SomeEvent>
