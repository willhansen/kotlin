interface A<T : A<T>> {
    konst symbol: ASymbol<T>
}

interface B<T : B<T>> : A<T> {
    override konst symbol: BSymbol<T>
}

interface C : B<C> {
    fun foo()

    override konst symbol: CSymbol
}

interface ASymbol<T : A<T>> {
    var konstue: T
}

interface BSymbol<T : B<T>> : ASymbol<T>

interface CSymbol : BSymbol<C> {
    fun bar()
}

fun test_1(symbol: BSymbol<*>) {
    if (symbol is CSymbol) {
        symbol.konstue.foo()
    }
}

fun test_2(b: B<*>) {
    if (b is C) {
        b.symbol.bar()
    }
}

fun <F : B<F>> test_3(b: B<F>) {
    if (b is C) {
        b.symbol.bar()
    }
}
