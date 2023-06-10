// TARGET_BACKEND: JVM

abstract class ConeClassLikeLookupTag {
    abstract konst status: String
}

class ConeClassLikeLookupTagImpl : ConeClassLikeLookupTag() {
    override var status: String = ""
        private set

    fun change(newStatus: String) {
        status = newStatus
    }
}

class ConeClassLikeErrorLookupTag : ConeClassLikeLookupTag() {
    override konst status: String
        get() = "ERROR"
}

fun ConeClassLikeLookupTag.foo(): String {
    (this as? ConeClassLikeLookupTagImpl)?.status?.takeIf { it == "OK" }?.let { return it }
    return status.also {
        (this as? ConeClassLikeLookupTagImpl)?.bar(it)
    }
}

fun ConeClassLikeLookupTagImpl.bar(s: String) {
    change(s)
}

fun box(): String {
    konst tag = ConeClassLikeErrorLookupTag()
    tag.foo()
    konst tag2 = ConeClassLikeLookupTagImpl()
    tag2.change("OK")
    tag2.foo()
    return tag2.status
}