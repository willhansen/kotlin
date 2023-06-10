// ISSUE: KT-56863
interface I

open class Some {
    konst x: Int

    init {
        this as I
        x = 1
    }
}
