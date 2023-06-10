// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

interface Foo

fun test() {
    var nullable: Foo? = null
    konst foo: Collection<Foo> = <!TYPE_MISMATCH!>java.util.Collections.singleton(nullable)<!>
    konst foo1: Collection<Foo> = java.util.Collections.singleton(nullable!!)
}
