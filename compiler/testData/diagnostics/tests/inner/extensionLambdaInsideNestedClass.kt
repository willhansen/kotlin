// FIR_IDENTICAL
package f

object A {
    class LoginFormPage() : Request({
        konst failed = session.get("LOGIN_FAILED")
    })
}

class B {
    companion object {
        class LoginFormPage() : Request({
            konst failed = session.get("LOGIN_FAILED")
        })
    }

    class C {
        class LoginFormPage() : Request({
            konst failed = session.get("LOGIN_FAILED")
        })
    }
}

open class Request(private konst handler: ActionContext.() -> Unit) {}

interface ActionContext {
    konst session : Map<String, String>
}
