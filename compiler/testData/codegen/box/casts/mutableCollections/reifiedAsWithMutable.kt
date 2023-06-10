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

inline fun <reified T> reifiedAsSucceeds(x: Any, operation: String) {
    try {
        x as T        
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }    
}

inline fun <reified T> reifiedAsFailsWithCCE(x: Any, operation: String) {
    try {
        x as T
    }
    catch (e: ClassCastException) {
        return
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should throw ClassCastException, got $e")
    }
    throw AssertionError("$operation: should fail with CCE, no exception thrown")
}

fun box(): String {
    konst itr = Itr() as Any
    konst mitr = MItr()

    reifiedAsFailsWithCCE<MutableIterator<*>>(itr, "reifiedAs<MutableIterator<*>>(itr)")
    reifiedAsSucceeds<MutableIterator<*>>(mitr, "reifiedAs<MutableIterator<*>>(mitr)")

    konst litr = LItr() as Any
    konst mlitr = MLItr()

    reifiedAsFailsWithCCE<MutableIterator<*>>(litr, "reifiedAs<MutableIterator<*>>(litr)")
    reifiedAsFailsWithCCE<MutableListIterator<*>>(litr, "reifiedAs<MutableListIterator<*>>(litr)")
    reifiedAsSucceeds<MutableListIterator<*>>(mlitr, "reifiedAs<MutableListIterator<*>>(mlitr)")

    konst it = It() as Any
    konst mit = MIt()
    konst arrayList = ArrayList<String>()

    reifiedAsFailsWithCCE<MutableIterable<*>>(it, "reifiedAs<MutableIterable<*>>(it)")
    reifiedAsSucceeds<MutableIterable<*>>(mit, "reifiedAs<MutableIterable<*>>(mit)")
    reifiedAsSucceeds<MutableIterable<*>>(arrayList, "reifiedAs<MutableIterable<*>>(arrayList)")

    konst coll = C() as Any
    konst mcoll = MC()

    reifiedAsFailsWithCCE<MutableCollection<*>>(coll, "reifiedAs<MutableCollection<*>>(coll)")
    reifiedAsFailsWithCCE<MutableIterable<*>>(coll, "reifiedAs<MutableIterable<*>>(coll)")
    reifiedAsSucceeds<MutableCollection<*>>(mcoll, "reifiedAs<MutableCollection<*>>(mcoll)")
    reifiedAsSucceeds<MutableIterable<*>>(mcoll, "reifiedAs<MutableIterable<*>>(mcoll)")
    reifiedAsSucceeds<MutableCollection<*>>(arrayList, "reifiedAs<MutableCollection<*>>(arrayList)")

    konst list = L() as Any
    konst mlist = ML()

    reifiedAsFailsWithCCE<MutableList<*>>(list, "reifiedAs<MutableList<*>>(list)")
    reifiedAsFailsWithCCE<MutableCollection<*>>(list, "reifiedAs<MutableCollection<*>>(list)")
    reifiedAsFailsWithCCE<MutableIterable<*>>(list, "reifiedAs<MutableIterable<*>>(list)")
    reifiedAsSucceeds<MutableList<*>>(mlist, "reifiedAs<MutableList<*>>(mlist)")
    reifiedAsSucceeds<MutableCollection<*>>(mlist, "reifiedAs<MutableCollection<*>>(mlist)")
    reifiedAsSucceeds<MutableIterable<*>>(mlist, "reifiedAs<MutableIterable<*>>(mlist)")
    reifiedAsSucceeds<MutableList<*>>(arrayList, "reifiedAs<MutableList<*>>(arrayList)")

    konst set = S() as Any
    konst mset = MS()
    konst hashSet = HashSet<String>()

    reifiedAsFailsWithCCE<MutableSet<*>>(set, "reifiedAs<MutableSet<*>>(set)")
    reifiedAsFailsWithCCE<MutableCollection<*>>(set, "reifiedAs<MutableCollection<*>>(set)")
    reifiedAsFailsWithCCE<MutableIterable<*>>(set, "reifiedAs<MutableIterable<*>>(set)")
    reifiedAsSucceeds<MutableSet<*>>(mset, "reifiedAs<MutableSet<*>>(mset)")
    reifiedAsSucceeds<MutableCollection<*>>(mset, "reifiedAs<MutableCollection<*>>(mset)")
    reifiedAsSucceeds<MutableIterable<*>>(mset, "reifiedAs<MutableIterable<*>>(mset)")
    reifiedAsSucceeds<MutableSet<*>>(hashSet, "reifiedAs<MutableSet<*>>(hashSet)")
    reifiedAsSucceeds<MutableCollection<*>>(hashSet, "reifiedAs<MutableCollection<*>>(hashSet)")
    reifiedAsSucceeds<MutableIterable<*>>(hashSet, "reifiedAs<MutableIterable<*>>(hashSet)")

    konst map = M() as Any
    konst mmap = MM()
    konst hashMap = HashMap<String, String>()

    reifiedAsFailsWithCCE<MutableMap<*, *>>(map, "reifiedAs<MutableMap<*, *>>(map)")
    reifiedAsSucceeds<MutableMap<*, *>>(mmap, "reifiedAs<MutableMap<*, *>>(mmap)")
    reifiedAsSucceeds<MutableMap<*, *>>(hashMap, "reifiedAs<MutableMap<*, *>>(hashMap)")

    konst entry = ME() as Any
    konst mentry = MME()

    hashMap[""] = ""
    konst hashMapEntry = hashMap.entries.first()

    reifiedAsFailsWithCCE<MutableMap.MutableEntry<*, *>>(entry, "reifiedAs<MutableMap.MutableEntry<*, *>>(entry)")
    reifiedAsSucceeds<MutableMap.MutableEntry<*, *>>(mentry, "reifiedAs<MutableMap.MutableEntry<*, *>>(mentry)")
    reifiedAsSucceeds<MutableMap.MutableEntry<*, *>>(hashMapEntry, "reifiedAs<MutableMap.MutableEntry<*, *>>(hashMapEntry)")

    return "OK"
}
