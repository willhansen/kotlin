// FIR_IDENTICAL
// !DIAGNOSTICS: -REDUNDANT_PROJECTION -CONFLICTING_PROJECTION

interface G

konst <T> T.a: Int
    get() = 3

konst <T1, T2> Map<T1, T2>.b: String
    get() = "asds"

konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T : G<!>> G.c: Int get() = 5

konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T1<!>, T2, T3> List<Map<T2, T3>>.d: Int get() = 6

konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T: Any<!>> G.e: T?
    get() = null

konst <T> List<Map<Int, Map<String, T>>>.f: Int get() = 7

konst <T> List<Map<Int, Map<String, out T>>>.g: Int get() = 7
konst <T> List<Map<Int, Map<String, in T>>>.h: Int get() = 7

konst <T> List<Map<T, Map<T, T>>>.i: Int get() = 7

var <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T1<!>, <!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T2<!>, <!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T3<!>, <!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T4<!>> p = 1

class C<T1, T2> {
    konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>E<!>> T1.a: Int get() = 3
    konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>E<!>> T2.b: Int get() = 3
    konst <E> E.c: Int get() = 3
    konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>E<!>> Map<T1, T2>.d: Int get() = 3
    konst <E> Map<T1, E>.e: Int get() = 3
}

konst <T : Enum<T>> T.z1: Int
    get() = 4

interface D<T : Enum<T>>

konst <X: D<*>> X.z2: Int
    get() = 4

konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>Y<!>> D<*>.z3: Int
    get() = 4