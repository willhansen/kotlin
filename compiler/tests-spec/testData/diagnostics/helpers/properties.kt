konst nullableNumberProperty: Number? = null

konst stringProperty: String = ""
konst nullableStringProperty: String? = null

konst intProperty: Int = ""
konst nullableIntProperty: Int? = null

konst implicitNullableNothingProperty = null
konst nullableNothingProperty: Nothing? = null

konst anonymousTypeProperty = object {}

konst nullableAnonymousTypeProperty = if (true) object {} else null

konst nullableOut: Out<Int>? = null

konst <T> T.propT get() = 10

konst <T : Any> T.propDefNotNullT get() = 10

konst <T> T?.propNullableT get(): Int? = 10

konst <T> T.propTT get() = 10 as T

konst <T> T?.propNullableTT get() = 10 as T?

konst Any.propAny get() = 10

konst Any?.propNullableAny get() = 10