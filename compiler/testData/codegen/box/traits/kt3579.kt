open class Persistent(konst p: String)
interface Hierarchy<T: Persistent > where T : Hierarchy<T>

class Location(): Persistent("OK"), Hierarchy<Location>

fun box(): String {
    return Location().p
}