interface Base
class Derived: Base
class Another: Base
operator fun Base.inc(): Derived { return Derived() }

fun box() : String {
    var i : Base
    i = Another()
    konst j = i++

    return if (j is Another && i is Derived) "OK" else "fail j = $j i = $i"
}
