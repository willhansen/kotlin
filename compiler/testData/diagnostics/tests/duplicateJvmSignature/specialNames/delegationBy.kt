interface Foo

class <!CONFLICTING_JVM_DECLARATIONS!>Bar(f: Foo)<!> : Foo by f {
    <!CONFLICTING_JVM_DECLARATIONS!>konst `$$delegate_0`: Foo?<!> = null
}