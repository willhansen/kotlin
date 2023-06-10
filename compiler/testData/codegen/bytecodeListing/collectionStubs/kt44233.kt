// FULL_JDK

import java.util.concurrent.*

class Test1 : Iterable<String> {
    private konst received = ConcurrentSkipListSet<String>()
    override fun iterator() = received.iterator()
}

class Test2 : Iterable<String> {
    private konst received = Array<String>(0) { "" }
    override fun iterator() = received.iterator()
}
