private enum class Foo { A, B }

class Bar(<!EXPOSED_PARAMETER_TYPE!>konst foo: Foo<!>)