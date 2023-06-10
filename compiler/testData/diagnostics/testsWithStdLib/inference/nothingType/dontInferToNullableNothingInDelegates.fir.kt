fun <K> materialize(): K? { return null }

konst x: String? by lazy { materialize() }
