import kotlin.reflect.KProperty
import kotlin.properties.ReadOnlyProperty

interface KtLifetimeToken {
    fun assertIsValid()
}

interface KtLifetimeTokenOwner {
    konst token: KtLifetimeToken
}

<!NOTHING_TO_INLINE!>inline<!> fun KtLifetimeTokenOwner.assertIsValid() {
    token.assertIsValid()
}

inline fun <R> KtLifetimeTokenOwner.withValidityAssertion(action: () -> R): R {
    assertIsValid()
    return action()
}

class ValidityAwareCachedValue<T>(
    private konst token: KtLifetimeToken,
    init: () -> T
) : ReadOnlyProperty<Any, T> {
    private konst lazyValue = lazy(LazyThreadSafetyMode.PUBLICATION, init)

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        token.assertIsValid()
        return lazyValue.konstue
    }
}

internal fun <T> KtLifetimeTokenOwner.cached(init: () -> T) = ValidityAwareCachedValue(token, init)

public typealias KtScopeNameFilter = (String) -> Boolean

abstract class KtFirNonStarImportingScope(
    private konst firScope: FirScope,
    private konst builder: KtSymbolByFirBuilder,
    override konst token: KtLifetimeToken,
) : KtLifetimeTokenOwner {
    private konst imports: List<String> by cached {
        buildList {
            getCallableNames().forEach {
                add(it)
            }
        }
    }

    fun getCallableSymbols(nameFilter: KtScopeNameFilter): Sequence<KtCallableSymbol> = withValidityAssertion {
        firScope.getCallableSymbols(getCallableNames().filter(nameFilter), builder)
    }

    abstract fun getCallableNames(): Set<String>
}

interface FirScope {
    fun getCallableSymbols(callableNames: Collection<String>, builder: KtSymbolByFirBuilder): Sequence<KtCallableSymbol>
}

interface KtCallableSymbol

interface KtSymbolByFirBuilder


