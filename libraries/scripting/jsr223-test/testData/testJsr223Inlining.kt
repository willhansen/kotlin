
import javax.script.*

inline fun<T> foo(body: () -> T): T = body()

fun main() {
    konst scriptEngine = ScriptEngineManager().getEngineByExtension("kts")!!
    scriptEngine.ekonst("println(foo { \"OK\" })")
}
