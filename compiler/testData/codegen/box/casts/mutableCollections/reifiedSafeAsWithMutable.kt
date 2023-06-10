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

inline fun <reified T> reifiedSafeAsReturnsNonNull(x: Any?, operation: String) {
    konst y = try {
        x as? T
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
    if (y == null) {
        throw AssertionError("$operation: should return non-null, got null")
    }
}

inline fun <reified T> reifiedSafeAsReturnsNull(x: Any?, operation: String) {
    konst y = try {
        x as? T
    }
    catch (e: Throwable) {
        throw AssertionError("$operation: should not throw exceptions, got $e")
    }
    if (y != null) {
        throw AssertionError("$operation: should return null, got $y")
    }
}

fun box(): String {
    konst itr = Itr() as Any
    konst mitr = MItr()

    reifiedSafeAsReturnsNull<MutableIterator<*>>(itr, "reifiedSafeAs<MutableIterator<*>>(itr)")
    reifiedSafeAsReturnsNonNull<MutableIterator<*>>(mitr, "reifiedSafeAs<MutableIterator<*>>(mitr)")

    konst litr = LItr() as Any
    konst mlitr = MLItr()

    reifiedSafeAsReturnsNull<MutableIterator<*>>(litr, "reifiedSafeAs<MutableIterator<*>>(litr)")
    reifiedSafeAsReturnsNull<MutableListIterator<*>>(litr, "reifiedSafeAs<MutableListIterator<*>>(litr)")
    reifiedSafeAsReturnsNonNull<MutableListIterator<*>>(mlitr, "reifiedSafeAs<MutableListIterator<*>>(mlitr)")

    konst it = It() as Any
    konst mit = MIt()
    konst arrayList = ArrayList<String>()

    reifiedSafeAsReturnsNull<MutableIterable<*>>(it, "reifiedSafeAs<MutableIterable<*>>(it)")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(mit, "reifiedSafeAs<MutableIterable<*>>(mit)")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(arrayList, "reifiedSafeAs<MutableIterable<*>>(arrayList)")

    konst coll = C() as Any
    konst mcoll = MC()

    reifiedSafeAsReturnsNull<MutableCollection<*>>(coll, "reifiedSafeAs<MutableCollection<*>>(coll)")
    reifiedSafeAsReturnsNull<MutableIterable<*>>(coll, "reifiedSafeAs<MutableIterable<*>>(coll)")
    reifiedSafeAsReturnsNonNull<MutableCollection<*>>(mcoll, "reifiedSafeAs<MutableCollection<*>>(mcoll)")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(mcoll, "reifiedSafeAs<MutableIterable<*>>(mcoll)")
    reifiedSafeAsReturnsNonNull<MutableCollection<*>>(arrayList, "reifiedSafeAs<MutableCollection<*>>(arrayList)")

    konst list = L() as Any
    konst mlist = ML()

    reifiedSafeAsReturnsNull<MutableList<*>>(list, "reifiedSafeAs<MutableList<*>>(list)")
    reifiedSafeAsReturnsNull<MutableCollection<*>>(list, "reifiedSafeAs<MutableCollection<*>>(list)")
    reifiedSafeAsReturnsNull<MutableIterable<*>>(list, "reifiedSafeAs<MutableIterable<*>>(list)")
    reifiedSafeAsReturnsNonNull<MutableList<*>>(mlist, "reifiedSafeAs<MutableList<*>>(mlist)")
    reifiedSafeAsReturnsNonNull<MutableCollection<*>>(mlist, "reifiedSafeAs<MutableCollection<*>>(mlist)")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(mlist, "reifiedSafeAs<MutableIterable<*>>(mlist)")
    reifiedSafeAsReturnsNonNull<MutableList<*>>(arrayList, "reifiedSafeAs<MutableList<*>>(arrayList)")

    konst set = S() as Any
    konst mset = MS()
    konst hashSet = HashSet<String>()

    reifiedSafeAsReturnsNull<MutableSet<*>>(set, "reifiedSafeAs<MutableSet<*>>(set)")
    reifiedSafeAsReturnsNull<MutableCollection<*>>(set, "reifiedSafeAs<MutableCollection<*>>(set)")
    reifiedSafeAsReturnsNull<MutableIterable<*>>(set, "reifiedSafeAs<MutableIterable<*>>(set)")
    reifiedSafeAsReturnsNonNull<MutableSet<*>>(mset, "reifiedSafeAs<MutableSet<*>>(mset)")
    reifiedSafeAsReturnsNonNull<MutableCollection<*>>(mset, "reifiedSafeAs<MutableCollection<*>>(mset)")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(mset, "reifiedSafeAs<MutableIterable<*>>(mset)")
    reifiedSafeAsReturnsNonNull<MutableSet<*>>(hashSet, "reifiedSafeAs<MutableSet<*>>(hashSet)")
    reifiedSafeAsReturnsNonNull<MutableCollection<*>>(hashSet, "reifiedSafeAs<MutableCollection<*>>(hashSet)")
    reifiedSafeAsReturnsNonNull<MutableIterable<*>>(hashSet, "reifiedSafeAs<MutableIterable<*>>(hashSet)")

    konst map = M() as Any
    konst mmap = MM()
    konst hashMap = HashMap<String, String>()

    reifiedSafeAsReturnsNull<MutableMap<*, *>>(map, "reifiedSafeAs<MutableMap<*, *>>(map)")
    reifiedSafeAsReturnsNonNull<MutableMap<*, *>>(mmap, "reifiedSafeAs<MutableMap<*, *>>(mmap)")
    reifiedSafeAsReturnsNonNull<MutableMap<*, *>>(hashMap, "reifiedSafeAs<MutableMap<*, *>>(hashMap)")

    konst entry = ME() as Any
    konst mentry = MME()

    hashMap[""] = ""
    konst hashMapEntry = hashMap.entries.first()

    reifiedSafeAsReturnsNull<MutableMap.MutableEntry<*, *>>(entry, "reifiedSafeAs<MutableMap.MutableEntry<*, *>>(entry)")
    reifiedSafeAsReturnsNonNull<MutableMap.MutableEntry<*, *>>(mentry, "reifiedSafeAs<MutableMap.MutableEntry<*, *>>(mentry)")
    reifiedSafeAsReturnsNonNull<MutableMap.MutableEntry<*, *>>(hashMapEntry, "reifiedSafeAs<MutableMap.MutableEntry<*, *>>(hashMapEntry)")

    reifiedSafeAsReturnsNull<MutableIterator<*>>(null, "reifiedSafeAs<MutableIterator<*>>(null)")
    reifiedSafeAsReturnsNull<MutableListIterator<*>>(null, "reifiedSafeAs<MutableListIterator<*>>(null)")
    reifiedSafeAsReturnsNull<MutableIterable<*>>(null, "reifiedSafeAs<MutableIterable<*>>(null)")
    reifiedSafeAsReturnsNull<MutableCollection<*>>(null, "reifiedSafeAs<MutableCollection<*>>(null)")
    reifiedSafeAsReturnsNull<MutableList<*>>(null, "reifiedSafeAs<MutableList<*>>(null)")
    reifiedSafeAsReturnsNull<MutableSet<*>>(null, "reifiedSafeAs<MutableSet<*>>(null)")
    reifiedSafeAsReturnsNull<MutableMap<*, *>>(null, "reifiedSafeAs<MutableMap<*, *>>(null)")
    reifiedSafeAsReturnsNull<MutableMap.MutableEntry<*, *>>(null, "reifiedSafeAs<MutableMap.MutableEntry<*, *>>(null)")

    return "OK"
}
