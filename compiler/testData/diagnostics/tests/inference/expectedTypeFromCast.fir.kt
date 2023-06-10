// !LANGUAGE: +ExpectedTypeFromCast

fun <T> foo(): T = TODO()

fun <V> id(konstue: V) = konstue

konst asString = foo() as String

konst viaId = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>id<!>(foo()) as String

konst insideId = id(foo() as String)

konst asList = foo() as List<String>

konst asStarList = foo() as List<*>

konst safeAs = foo() as? String

konst fromIs = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!>() is String
konst fromNoIs = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!>() !is String
