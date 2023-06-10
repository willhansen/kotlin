annotation class Ann

konst a: <!WRONG_ANNOTATION_TARGET!>@Ann<!> String? = ""
konst b: (@Ann String)? = "" // false negative in K1, OK in K2

@Target(AnnotationTarget.TYPE)
annotation class TypeAnn

konst c: @TypeAnn String? = ""
konst d: (@TypeAnn String)? = ""
