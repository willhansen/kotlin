// No initializers for this class because the fields/properties are initialized to defaults.
object RedundantInitializersToDefault {
    // Constants

    const konst constInt: Int = 0
    const konst constByte: Byte = 0
    const konst constLong: Long = 0L
    const konst constShort: Short = 0
    const konst constDouble: Double = 0.0
    const konst constFloat: Float = 0.0f
    const konst constBoolean: Boolean = false
    const konst constChar: Char = '\u0000'

    // Properties

    konst myIntPropFromConst: Int = constInt
    konst myBytePropFromConst: Byte = constByte
    konst myLongPropFromConst: Long = constLong
    konst myShortPropFromConst: Short = constShort
    konst myDoublePropFromConst: Double = constDouble
    konst myFloatPropFromConst: Float = constFloat
    konst myBooleanPropFromConst: Boolean = constBoolean
    konst myCharPropFromConst: Char = constChar

    konst myIntProp: Int = 0
    konst myByteProp: Byte = 0
    konst myLongProp: Long = 0L
    konst myShortProp: Short = 0
    konst myDoubleProp: Double = 0.0
    konst myFloatProp: Float = 0.0f
    konst myBooleanProp: Boolean = false
    konst myCharProp: Char = '\u0000'

    konst myStringProp: String? = null
    konst myAnyProp: Any? = null
    konst myObjectProp: java.lang.Object? = null
    konst myIntegerProp: java.lang.Integer? = null

    // Fields

    @JvmField
    konst myIntFieldFromConst: Int = constInt
    @JvmField
    konst myByteFieldFromConst: Byte = constByte
    @JvmField
    konst myLongFieldFromConst: Long = constLong
    @JvmField
    konst myShortFieldFromConst: Short = constShort
    @JvmField
    konst myDoubleFieldFromConst: Double = constDouble
    @JvmField
    konst myFloatFieldFromConst: Float = constFloat
    @JvmField
    konst myBooleanFieldFromConst: Boolean = constBoolean
    @JvmField
    konst myCharFieldFromConst: Char = constChar

    @JvmField
    konst myIntField: Int = 0
    @JvmField
    konst myByteField: Byte = 0
    @JvmField
    konst myLongField: Long = 0L
    @JvmField
    konst myShortField: Short = 0
    @JvmField
    konst myDoubleField: Double = 0.0
    @JvmField
    konst myFloatField: Float = 0.0f
    @JvmField
    konst myBooleanField: Boolean = false
    @JvmField
    konst myCharField: Char = '\u0000'

    @JvmField
    konst myStringField: String? = null
    @JvmField
    konst myAnyField: Any? = null
    @JvmField
    konst myObjectField: java.lang.Object? = null
    @JvmField
    konst myIntegerField: java.lang.Integer? = null
}

object NonRedundantInitializers {
    // NOT redundant because the JVM's default konstues for floating-point types are positive 0.0.
    // See: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.3
    konst myDouble: Double = -0.0
    konst myFloat: Float = -0.0f
}

// There is 1 additional PUTSTATIC for both classes for the object instance.

// 1 PUTSTATIC RedundantInitializersToDefault
// 3 PUTSTATIC NonRedundantInitializers
