interface Foo

class Bar(f: Foo) : Foo by f {
    konst `$$delegate_0`: Foo? = null
}