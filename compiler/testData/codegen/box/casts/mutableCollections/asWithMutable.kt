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

inline fun asFailsWithCCE(operation: String, block: () -> Unit) {
    try {
        block()
    }
    catch (e: ClassCastException) {
        return
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should throw ClassCastException, got $e")
    }
    throw AssertionError("$operation: should throw ClassCastException, no exception thrown")
}

inline fun asSucceeds(operation: String, block: () -> Unit) {
    try {
        block()
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
}

fun box(): String {
    konst itr = Itr() as Any
    konst mitr = MItr()

    asFailsWithCCE("itr as MutableIterator") { itr as MutableIterator<*> }
    asSucceeds("mitr as MutableIterator") { mitr as MutableIterator<*> }

    konst litr = LItr() as Any
    konst mlitr = MLItr()

    asFailsWithCCE("litr as MutableIterator") { litr as MutableIterator<*> }
    asFailsWithCCE("litr as MutableListIterator") { litr as MutableListIterator<*> }
    asSucceeds("mlitr as MutableIterator") { mlitr as MutableIterator<*> }
    asSucceeds("mlitr as MutableListIterator") { mlitr as MutableListIterator<*> }

    konst it = It() as Any
    konst mit = MIt()
    konst arrayList = ArrayList<String>()

    asFailsWithCCE("it as MutableIterable") { it as MutableIterable<*> }
    asSucceeds("mit as MutableIterable") { mit as MutableIterable<*> }
    asSucceeds("arrayList as MutableIterable") { arrayList as MutableIterable<*> }

    konst coll = C() as Any
    konst mcoll = MC()

    asFailsWithCCE("coll as MutableIterable") { coll as MutableIterable<*> }
    asFailsWithCCE("coll as MutableCollection") { coll as MutableCollection<*> }
    asSucceeds("mcoll as MutableIterable") { mcoll as MutableIterable<*> }
    asSucceeds("mcoll as MutableCollection") { mcoll as MutableCollection<*> }
    asSucceeds("arrayList as MutableCollection") { arrayList as MutableCollection<*> }

    konst list = L() as Any
    konst mlist = ML()

    asFailsWithCCE("list as MutableIterable") { list as MutableIterable<*> }
    asFailsWithCCE("list as MutableCollection") { list as MutableCollection<*> }
    asFailsWithCCE("list as MutableList") { list as MutableList<*> }
    asSucceeds("mlist as MutableIterable") { mlist as MutableIterable<*> }
    asSucceeds("mlist as MutableCollection") { mlist as MutableCollection<*> }
    asSucceeds("mlist as MutableList") { mlist as MutableList<*> }

    konst set = S() as Any
    konst mset = MS()
    konst hashSet = HashSet<String>()

    asFailsWithCCE("set as MutableIterable") { set as MutableIterable<*> }
    asFailsWithCCE("set as MutableCollection") { set as MutableCollection<*> }
    asFailsWithCCE("set as MutableSet") { set as MutableSet<*> }
    asSucceeds("mset as MutableIterable") { mset as MutableIterable<*> }
    asSucceeds("mset as MutableCollection") { mset as MutableCollection<*> }
    asSucceeds("mset as MutableSet") { mset as MutableSet<*> }
    asSucceeds("hashSet as MutableSet") { hashSet as MutableSet<*> }

    konst map = M() as Any
    konst mmap = MM()
    konst hashMap = HashMap<String, String>()

    asFailsWithCCE("map as MutableMap") { map as MutableMap<*, *> }
    asSucceeds("mmap as MutableMap") { mmap as MutableMap<*, *> }

    konst entry = ME() as Any
    konst mentry = MME()

    asFailsWithCCE("entry as MutableMap.MutableEntry") { entry as MutableMap.MutableEntry<*, *> }
    asSucceeds("mentry as MutableMap.MutableEntry") { mentry as MutableMap.MutableEntry<*, *> }

    hashMap[""] = ""
    konst hashMapEntry = hashMap.entries.first()

    asSucceeds("hashMapEntry as MutableMap.MutableEntry") { hashMapEntry as MutableMap.MutableEntry<*, *> }

    return "OK"
}
