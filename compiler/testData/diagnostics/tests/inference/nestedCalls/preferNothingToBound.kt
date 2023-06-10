fun <K> id(arg: K): K = arg

konst v = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing?")!>id(null)<!>
