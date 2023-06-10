package foo

annotation class NoArg
annotation class AllOpen

@AllOpen
class Base(konst s: String)

class Derived(s: String) : Base(s) {
    @NoArg
    inner class Inner(konst s: String)
}
