class Candidate(konst symbol: AbstractFirBasedSymbol<*>)

abstract class AbstractFirBasedSymbol<E> where E : FirSymbolOwner<E>, E : FirDeclaration {
    lateinit var fir: E
}

interface FirDeclaration

interface FirSymbolOwner<E> where E : FirSymbolOwner<E>, E : FirDeclaration {
    konst symbol: AbstractFirBasedSymbol<E>
}

interface FirCallableMemberDeclaration<F : FirCallableMemberDeclaration<F>> : FirSymbolOwner<F>, FirDeclaration {
    override konst symbol: AbstractFirBasedSymbol<F>
}

fun foo(candidate: Candidate) {
    konst me = candidate.symbol.fir
    if (me is FirCallableMemberDeclaration<*> && me.symbol != null) {}
}