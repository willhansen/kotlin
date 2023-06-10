package collections

fun <T> testCollection(c: Collection<T>, t: T) {
    c.size
    c.isEmpty()
    c.contains(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    konst iterator: Iterator<T> = c.iterator()
    c.containsAll(c)

    konst mutableIterator: MutableIterator<T> = <!TYPE_MISMATCH!>c.iterator()<!>
    c.<!UNRESOLVED_REFERENCE!>add<!>(t)
    c.<!UNRESOLVED_REFERENCE!>remove<!>(1)
    c.<!UNRESOLVED_REFERENCE!>addAll<!>(c)
    c.<!UNRESOLVED_REFERENCE!>removeAll<!>(c)
    c.<!UNRESOLVED_REFERENCE!>retainAll<!>(c)
    c.<!UNRESOLVED_REFERENCE!>clear<!>()

}
fun <T> testMutableCollection(c: MutableCollection<T>, t: T) {
    c.size
    c.isEmpty()
    c.contains(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    konst iterator: Iterator<T> = c.iterator()
    c.containsAll(c)


    konst mutableIterator: MutableIterator<T> = c.iterator()
    c.add(t)
    c.remove(1 <!UNCHECKED_CAST!>as T<!>)
    c.addAll(c)
    c.removeAll(c)
    c.retainAll(c)
    c.clear()
}

fun <T> testList(l: List<T>, t: T) {
    konst <!NAME_SHADOWING!>t<!>: T = l.get(1)
    konst i: Int = l.indexOf(t)
    konst i1: Int = l.lastIndexOf(t)
    konst listIterator: ListIterator<T> = l.listIterator()
    konst listIterator1: ListIterator<T> = l.listIterator(1)
    konst list: List<T> = l.subList(1, 2)

    konst konstue: T = l.<!UNRESOLVED_REFERENCE!>set<!>(1, t)
    l.<!UNRESOLVED_REFERENCE!>add<!>(1, t)
    l.<!UNRESOLVED_REFERENCE!>remove<!>(1)
    konst mutableListIterator: MutableListIterator<T> = <!TYPE_MISMATCH!>l.listIterator()<!>
    konst mutableListIterator1: MutableListIterator<T> = <!TYPE_MISMATCH!>l.listIterator(1)<!>
    konst mutableList: MutableList<T> = <!TYPE_MISMATCH!>l.subList(1, 2)<!>
}

fun <T> testMutableList(l: MutableList<T>, t: T) {
    konst konstue: T = l.set(1, t)
    l.add(1, t)
    l.removeAt(1)
    konst mutableListIterator: MutableListIterator<T> = l.listIterator()
    konst mutableListIterator1: MutableListIterator<T> = l.listIterator(1)
    konst mutableList: MutableList<T> = l.subList(1, 2)
}

fun <T> testSet(s: Set<T>, t: T) {
    s.size
    s.isEmpty()
    s.contains(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    konst iterator: Iterator<T> = s.iterator()
    s.containsAll(s)

    konst mutableIterator: MutableIterator<T> = <!TYPE_MISMATCH!>s.iterator()<!>
    s.<!UNRESOLVED_REFERENCE!>add<!>(t)
    s.<!UNRESOLVED_REFERENCE!>remove<!>(1)
    s.<!UNRESOLVED_REFERENCE!>addAll<!>(s)
    s.<!UNRESOLVED_REFERENCE!>removeAll<!>(s)
    s.<!UNRESOLVED_REFERENCE!>retainAll<!>(s)
    s.<!UNRESOLVED_REFERENCE!>clear<!>()

}
fun <T> testMutableSet(s: MutableSet<T>, t: T) {
    s.size
    s.isEmpty()
    s.contains(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    konst iterator: Iterator<T> = s.iterator()
    s.containsAll(s)


    konst mutableIterator: MutableIterator<T> = s.iterator()
    s.add(t)
    s.remove(1 <!UNCHECKED_CAST!>as T<!>)
    s.addAll(s)
    s.removeAll(s)
    s.retainAll(s)
    s.clear()
}

fun <K, V> testMap(m: Map<K, V>) {
    konst set: Set<K> = m.keys
    konst collection: Collection<V> = m.konstues
    konst set1: Set<Map.Entry<K, V>> = m.entries

    konst mutableSet: MutableSet<K> = <!TYPE_MISMATCH!>m.keys<!>
    konst mutableCollection: MutableCollection<V> = <!TYPE_MISMATCH!>m.konstues<!>
    konst mutableSet1: MutableSet<MutableMap.MutableEntry<K, V>> = <!TYPE_MISMATCH!>m.entries<!>
}

fun <K, V> testMutableMap(m: MutableMap<K, V>) {
    konst mutableSet: MutableSet<K> = m.keys
    konst mutableCollection: MutableCollection<V> = m.konstues
    konst mutableSet1: MutableSet<MutableMap.MutableEntry<K, V>> = m.entries
}

fun <T> array(vararg t: T): Array<T> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
