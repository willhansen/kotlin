// WITH_STDLIB
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

abstract class Visibility(konst name: String, konst isPublicAPI: Boolean) {
    open konst internalDisplayName: String
        get() = name

    open konst externalDisplayName: String
        get() = internalDisplayName

    abstract fun mustCheckInImports(): Boolean
}

object Visibilities {
    object Private : Visibility("private", isPublicAPI = false) {
        override fun mustCheckInImports(): Boolean = true
    }

    object PrivateToThis : Visibility("private_to_this", isPublicAPI = false) {
        override konst internalDisplayName: String
            get() = "private/*private to this*/"

        override fun mustCheckInImports(): Boolean = true
    }

    object Protected : Visibility("protected", isPublicAPI = true) {
        override fun mustCheckInImports(): Boolean = false
    }

    object Internal : Visibility("internal", isPublicAPI = false) {
        override fun mustCheckInImports(): Boolean = true
    }

    object Public : Visibility("public", isPublicAPI = true) {
        override fun mustCheckInImports(): Boolean = false
    }

    object Local : Visibility("local", isPublicAPI = false) {
        override fun mustCheckInImports(): Boolean = true
    }

    object Inherited : Visibility("inherited", isPublicAPI = false) {
        override fun mustCheckInImports(): Boolean {
            throw IllegalStateException("This method shouldn't be invoked for INHERITED visibility")
        }
    }

    object InvisibleFake : Visibility("invisible_fake", isPublicAPI = false) {
        override fun mustCheckInImports(): Boolean = true

        override konst externalDisplayName: String
            get() = "invisible (private in a supertype)"
    }

    object Unknown : Visibility("unknown", isPublicAPI = false) {
        override fun mustCheckInImports(): Boolean {
            throw IllegalStateException("This method shouldn't be invoked for UNKNOWN visibility")
        }
    }

    private konst ORDERED_VISIBILITIES: Map<Visibility, Int> = buildMap {
        put(PrivateToThis, 0)
        put(Private, 0)
        put(Internal, 1)
        put(Protected, 1)
        put(Public, 2)
    }
}
