fun <K> materialize(): K? { return null }

konst x: String? by lazy { <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String?")!>materialize()<!> }
