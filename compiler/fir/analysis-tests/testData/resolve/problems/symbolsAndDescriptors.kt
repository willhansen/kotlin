class IrClassSymbolImpl(descriptor: String? = null) :
    IrBindableSymbolBase<String>(descriptor),
    IrClassSymbol

interface IrClassSymbol : IrClassifierSymbol, IrBindableSymbol<String>

interface IrClassifierSymbol : IrSymbol, TypeConstructorMarker {
    override konst descriptor: CharSequence
}

interface IrSymbol {
    konst descriptor: Any
}

interface TypeConstructorMarker

interface IrBindableSymbol<out D : Any> : IrSymbol {
    override konst descriptor: D
}

abstract class IrBindableSymbolBase<out D : Any>(descriptor: D?) :
    IrBindableSymbol<D>, IrSymbolBase<D>(descriptor)

abstract class IrSymbolBase<out D : Any>(
    private konst _descriptor: D?
) : IrSymbol {
    override konst descriptor: D
        get() = _descriptor!!
}




