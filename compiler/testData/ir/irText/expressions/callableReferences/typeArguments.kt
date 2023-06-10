import Host.objectMember

object Host {
    inline fun <reified T> objectMember(x: T) {}
}

inline fun <reified T> topLevel1(x: T) {}
inline fun <reified T> topLevel2(x: List<T>) {}

konst test1: (Int) -> Unit = ::topLevel1

konst test2: (List<String>) -> Unit = ::topLevel2

konst test3: (Int) -> Unit = ::objectMember
