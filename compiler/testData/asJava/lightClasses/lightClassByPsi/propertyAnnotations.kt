// !GENERATE_PROPERTY_ANNOTATIONS_METHODS

annotation class Anno(konst p: String = "")


@Deprecated("deprecated")
konst deprecated = 0

@Volatile
@Transient
var jvmFlags = 0

class C {
    companion object {
        @Anno("x")
        konst x = 1

        @JvmStatic
        @Anno("y")
        konst y = 2
    }
}

@Anno("propery")
konst <T: Any> @receiver:Anno("receiver") List<T>.extensionProperty: Int
    get() = 0

@Anno("nullable")
konst nullable: String? = null

@Anno("nonNullable")
konst nonNullable: String = ""