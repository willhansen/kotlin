// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.kt
package a

interface IrSymbol {
    konst owner: Any
}

interface IrFunction
interface IrSimpleFunction : IrFunction {
    konst name: String
}

interface IrFunctionSymbol : IrSymbol {
    override konst owner: IrFunction
}

interface IrBindableSymbol<B : Any> : IrSymbol {
    override konst owner: B
}

interface IrSimpleFunctionSymbol : IrFunctionSymbol, IrBindableSymbol<IrSimpleFunction>

// MODULE: main(lib)
// FILE: B.kt
import a.*

fun foo(x: IrSimpleFunctionSymbol): String {
    return x.owner.name
}

fun box(): String {
    return foo(object : IrSimpleFunctionSymbol {
        override konst owner: IrSimpleFunction
            get() = object : IrSimpleFunction {
                override konst name: String
                    get() = "OK"
            }
    })
}
