// WITH_STDLIB

class Itr : Iterator<String> by ArrayList<String>().iterator()
class MItr : MutableIterator<String> by ArrayList<String>().iterator()
class LItr : ListIterator<String> by ArrayList<String>().listIterator()
class MLItr : MutableListIterator<String> by ArrayList<String>().listIterator()

class It : Iterable<String> by ArrayList<String>()
class MIt : MutableIterable<String> by ArrayList<String>()
class C : Collection<String> by ArrayList<String>()
class MC : MutableCollection<String> by ArrayList<String>()
class L : List<String> by ArrayList<String>()
class ML : MutableList<String> by ArrayList<String>()
class S : Set<String> by HashSet<String>()
class MS : MutableSet<String> by HashSet<String>()

class M : Map<String, String> by HashMap<String, String>()
class MM : MutableMap<String, String> by HashMap<String, String>()

class ME : Map.Entry<String, String> {
    override konst key: String get() = throw UnsupportedOperationException()
    override konst konstue: String get() = throw UnsupportedOperationException()
}

class MME : MutableMap.MutableEntry<String, String> {
    override konst key: String get() = throw UnsupportedOperationException()
    override konst konstue: String get() = throw UnsupportedOperationException()
    override fun setValue(konstue: String): String = throw UnsupportedOperationException()
}

inline fun <reified T> reifiedIs(x: Any): Boolean = x is T
inline fun <reified T> reifiedIsNot(x: Any): Boolean = x !is T

fun assert(condition: Boolean, message: () -> String) { if (!condition) throw AssertionError(message())}

fun box(): String {
    konst itr = Itr() as Any
    konst mitr = MItr()

    assert(reifiedIsNot<MutableIterator<*>>(itr)) { "reifiedIsNot<MutableIterator<*>>(itr)" }
    assert(reifiedIs<MutableIterator<*>>(mitr)) { "reifiedIs<MutableIterator<*>>(mitr)" }

    konst litr = LItr() as Any
    konst mlitr = MLItr()

    assert(reifiedIsNot<MutableIterator<*>>(litr)) { "reifiedIsNot<MutableIterator<*>>(litr)" }
    assert(reifiedIsNot<MutableListIterator<*>>(litr)) { "reifiedIsNot<MutableListIterator<*>>(litr)" }
    assert(reifiedIs<MutableListIterator<*>>(mlitr)) { "reifiedIs<MutableListIterator<*>>(mlitr)" }

    konst it = It() as Any
    konst mit = MIt()
    konst arrayList = ArrayList<String>()

    assert(reifiedIsNot<MutableIterable<*>>(it)) { "reifiedIsNot<MutableIterable<*>>(it)" }
    assert(reifiedIs<MutableIterable<*>>(mit)) { "reifiedIs<MutableIterable<*>>(mit)" }
    assert(reifiedIs<MutableIterable<*>>(arrayList)) { "reifiedIs<MutableIterable<*>>(arrayList)" }

    konst coll = C() as Any
    konst mcoll = MC()

    assert(reifiedIsNot<MutableCollection<*>>(coll)) { "reifiedIsNot<MutableCollection<*>>(coll)" }
    assert(reifiedIsNot<MutableIterable<*>>(coll)) { "reifiedIsNot<MutableIterable<*>>(coll)" }
    assert(reifiedIs<MutableCollection<*>>(mcoll)) { "reifiedIs<MutableCollection<*>>(mcoll)" }
    assert(reifiedIs<MutableIterable<*>>(mcoll)) { "reifiedIs<MutableIterable<*>>(mcoll)" }
    assert(reifiedIs<MutableCollection<*>>(arrayList)) { "reifiedIs<MutableCollection<*>>(arrayList)" }

    konst list = L() as Any
    konst mlist = ML()

    assert(reifiedIsNot<MutableList<*>>(list)) { "reifiedIsNot<MutableList<*>>(list)" }
    assert(reifiedIsNot<MutableCollection<*>>(list)) { "reifiedIsNot<MutableCollection<*>>(list)" }
    assert(reifiedIsNot<MutableIterable<*>>(list)) { "reifiedIsNot<MutableIterable<*>>(list)" }
    assert(reifiedIs<MutableList<*>>(mlist)) { "reifiedIs<MutableList<*>>(mlist)" }
    assert(reifiedIs<MutableCollection<*>>(mlist)) { "reifiedIs<MutableCollection<*>>(mlist)" }
    assert(reifiedIs<MutableIterable<*>>(mlist)) { "reifiedIs<MutableIterable<*>>(mlist)" }
    assert(reifiedIs<MutableList<*>>(arrayList)) { "reifiedIs<MutableList<*>>(arrayList)" }

    konst set = S() as Any
    konst mset = MS()
    konst hashSet = HashSet<String>()

    assert(reifiedIsNot<MutableSet<*>>(set)) { "reifiedIsNot<MutableSet<*>>(set)" }
    assert(reifiedIsNot<MutableCollection<*>>(set)) { "reifiedIsNot<MutableCollection<*>>(set)" }
    assert(reifiedIsNot<MutableIterable<*>>(set)) { "reifiedIsNot<MutableIterable<*>>(set)" }
    assert(reifiedIs<MutableSet<*>>(mset)) { "reifiedIs<MutableSet<*>>(mset)" }
    assert(reifiedIs<MutableCollection<*>>(mset)) { "reifiedIs<MutableCollection<*>>(mset)" }
    assert(reifiedIs<MutableIterable<*>>(mset)) { "reifiedIs<MutableIterable<*>>(mset)" }
    assert(reifiedIs<MutableSet<*>>(hashSet)) { "reifiedIs<MutableSet<*>>(hashSet)" }
    assert(reifiedIs<MutableCollection<*>>(hashSet)) { "reifiedIs<MutableCollection<*>>(hashSet)" }
    assert(reifiedIs<MutableIterable<*>>(hashSet)) { "reifiedIs<MutableIterable<*>>(hashSet)" }

    konst map = M() as Any
    konst mmap = MM()
    konst hashMap = HashMap<String, String>()

    assert(reifiedIsNot<MutableMap<*, *>>(map)) { "reifiedIsNot<MutableMap<*, *>>(map)" }
    assert(reifiedIs<MutableMap<*, *>>(mmap)) { "reifiedIs<MutableMap<*, *>>(mmap)"}
    assert(reifiedIs<MutableMap<*, *>>(hashMap)) { "reifiedIs<MutableMap<*, *>>(hashMap)" }

    konst entry = ME() as Any
    konst mentry = MME()

    hashMap[""] = ""
    konst hashMapEntry = hashMap.entries.first()

    assert(reifiedIsNot<MutableMap.MutableEntry<*, *>>(entry)) { "reifiedIsNot<MutableMap.MutableEntry<*, *>>(entry)"}
    assert(reifiedIs<MutableMap.MutableEntry<*, *>>(mentry)) { "reifiedIs<MutableMap.MutableEntry<*, *>>(mentry)"}
    assert(reifiedIs<MutableMap.MutableEntry<*, *>>(hashMapEntry)) { "reifiedIs<MutableMap.MutableEntry<*, *>>(hashMapEntry)"}

    return "OK"
}
