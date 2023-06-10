class In<in T>(konst x: Any)

typealias InAlias<T> = In<T>

konst test1 = In<<!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>out String<!>>("")
konst test2 = InAlias<<!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>out String<!>>("")
