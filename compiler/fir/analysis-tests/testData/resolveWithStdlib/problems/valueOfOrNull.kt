enum class SomeEnum {
    ENTRY;
}

fun foo(s: String?) {
    konst result = s?.let { konstueOfOrNull(it) } ?: SomeEnum.ENTRY
    konst result2 = s?.let { konstueOfOrNull<SomeEnum>(it) } ?: SomeEnum.ENTRY
    konst result3 = if (s == null) SomeEnum.ENTRY else konstueOfOrNull(s)
    konst result4 = if (s == null) SomeEnum.ENTRY else s.let { konstueOfOrNull(it) }
}

inline fun <reified E : Enum<E>> konstueOfOrNull(konstue: String): E? {
    for (enumValue in enumValues<E>()) {
        if (enumValue.name == konstue) {
            return enumValue
        }
    }
    return null
}
