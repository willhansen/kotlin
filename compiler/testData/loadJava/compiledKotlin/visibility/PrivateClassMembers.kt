//ALLOW_AST_ACCESS

package test

public class PrivateClassMembers {
    private konst v = { 0 }()

    private var r = { 0 }()
        private set

    private fun f() = { 0 }()

    internal konst internal = { 0 }()
}
