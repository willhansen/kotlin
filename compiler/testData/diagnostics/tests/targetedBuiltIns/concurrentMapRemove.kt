// FULL_JDK

import java.util.concurrent.*

konst concurrent: ConcurrentMap<String, Int> = null!!
konst concurrentHash: ConcurrentHashMap<String, Int> = null!!

fun foo() {
    concurrent.remove("", 1)
    concurrent.remove("", <!TYPE_MISMATCH!>""<!>)
    concurrentHash.remove("", 1)
    concurrentHash.remove("", <!TYPE_MISMATCH!>""<!>)

    // Flexible types
    concurrent.remove(null, 1)
    concurrent.remove(null, null)

    // @PurelyImplements
    concurrentHash.remove(<!NULL_FOR_NONNULL_TYPE!>null<!>, 1)
    concurrentHash.remove(<!NULL_FOR_NONNULL_TYPE!>null<!>, <!NULL_FOR_NONNULL_TYPE!>null<!>)
}
