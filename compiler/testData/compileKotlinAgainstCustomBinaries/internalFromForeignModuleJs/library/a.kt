package a

internal interface InternalInterface

public class PublicClass {
    internal fun internalMemberFun() {}

    internal companion object {}
}

internal konst internalVal = ""

internal fun internalFun(s: String): String = s

internal typealias InternalTypealias = InternalInterface
