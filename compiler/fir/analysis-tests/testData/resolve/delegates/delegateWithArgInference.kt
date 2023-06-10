import kotlin.reflect.KProperty

class Delegate<T>(konst data: T) {
    operator fun getValue(thisRef: Nothing?, prop: KProperty<*>): T = data
}

fun makeIntDelegate(t: Int): Delegate<Int> = Delegate(t)
fun <TT> makeDelegate(t: TT): Delegate<TT> = Delegate(t)
fun <M> materialize(): M = null!!
fun <M2> materialize2(): M2 = null!!
fun <Q> id(v: Q): Q = v

konst x by makeIntDelegate(run {
    konst x: String = materialize()
    materialize2()
})
