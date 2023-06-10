konst instance = My()

class My {
    konst equalsInstance = (<!DEBUG_INFO_LEAKING_THIS!>this<!> == instance)

    konst isInstance = if (this === instance) "true" else "false"

    override fun equals(other: Any?) =
            other is My && isInstance.hashCode() == <!DEBUG_INFO_SMARTCAST!>other<!>.isInstance.hashCode()
}
