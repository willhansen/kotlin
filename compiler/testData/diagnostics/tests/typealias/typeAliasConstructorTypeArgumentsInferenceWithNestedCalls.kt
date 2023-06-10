// !LANGUAGE: -TypeEnhancementImprovementsInStrictMode
// FULL_JDK

// FILE: MapLike.java
import java.util.Map;

public class MapLike<@org.jetbrains.annotations.NotNull K> {
    MapLike(K x) {  }
}

// FILE: main.kt
class Cons<T : Number>(konst head: T, konst tail: Cons<T>?)
typealias C<T> = Cons<T>
typealias C2<T> = MapLike<T>

konst test1 = C(1, C(2, null))
konst test2 = C(1, <!TYPE_MISMATCH!>C(<!TYPE_MISMATCH!>""<!>, null)<!>)
konst test23 = <!UPPER_BOUND_VIOLATED_IN_TYPEALIAS_EXPANSION_BASED_ON_JAVA_ANNOTATIONS!>C2(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>if (true) 1 else null<!>)<!>
konst test234 = C2(<!UPPER_BOUND_VIOLATED_IN_TYPEALIAS_EXPANSION_BASED_ON_JAVA_ANNOTATIONS!>C2(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>if (true) 1 else null<!>)<!>)
