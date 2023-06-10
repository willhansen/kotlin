//ALLOW_AST_ACCESS
package test

const private konst topLevel = 1

object A {
    const internal konst inObject = 2
}

class B {
    companion object {
        const konst inCompanion = 3
    }
}
