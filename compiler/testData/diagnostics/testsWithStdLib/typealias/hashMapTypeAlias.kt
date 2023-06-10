// FIR_IDENTICAL
import kotlin.test.*
import java.util.*

typealias HM<Kt, Vt> = HashMap<Kt, Vt>

fun <K, V, M : MutableMap<in K, MutableList<V>>> updateMap(map: M, k: K, v: V): M {
    map[k]!!.add(v)
    return map
}

konst nameToTeam = listOf("Alice" to "Marketing", "Bob" to "Sales", "Carol" to "Marketing")
konst namesByTeam = nameToTeam.groupBy({ it.second }, { it.first })

konst mutableNamesByTeam1 = updateMap(HM(), "", "")
konst mutableNamesByTeam2 = updateMap(HashMap(), "", "")

fun test() {
    assertEquals(namesByTeam, mutableNamesByTeam1)
    assertEquals(namesByTeam, mutableNamesByTeam2)
}
