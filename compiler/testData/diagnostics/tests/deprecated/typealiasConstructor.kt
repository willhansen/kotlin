@Deprecated("Deprecated class")
open class DeprecatedClass

open class WithDeprecatedCtor(konst x: Int) {
    @Deprecated("Deprecated constructor")
    constructor() : this(0)
}

typealias DeprecatedClassAlias = <!DEPRECATION!>DeprecatedClass<!>
typealias WithDeprecatedCtorAlias = WithDeprecatedCtor
typealias ArrayListOfDeprecatedClass = ArrayList<<!DEPRECATION!>DeprecatedClass<!>>

class Test1 : <!TYPEALIAS_EXPANSION_DEPRECATION!>DeprecatedClassAlias<!>()

class Test2 : <!TYPEALIAS_EXPANSION_DEPRECATION!>WithDeprecatedCtorAlias<!>()

konst test3 = <!TYPEALIAS_EXPANSION_DEPRECATION!>ArrayListOfDeprecatedClass<!>()
