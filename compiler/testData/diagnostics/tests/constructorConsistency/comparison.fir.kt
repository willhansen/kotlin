konst instance = My()

class My {
    konst equalsInstance = (this == instance)

    konst isInstance = if (this === instance) "true" else "false"

    override fun equals(other: Any?) =
            other is My && isInstance.hashCode() == other.isInstance.hashCode()
}
