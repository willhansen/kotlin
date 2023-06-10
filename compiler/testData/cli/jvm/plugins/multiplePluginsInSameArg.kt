package foo

annotation class NoArg
annotation class AllOpen

@AllOpen
class Base(konst s: String)

@NoArg
class Derived(s: String) : Base(s)
