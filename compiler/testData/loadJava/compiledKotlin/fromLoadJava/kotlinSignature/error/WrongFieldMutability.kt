// ALLOW_AST_ACCESS
package test

import java.util.*

public open class WrongFieldMutability {
    public var fooNotFinal : String? = { "" }()
    public konst fooFinal : String? = { "Test" }()
}
