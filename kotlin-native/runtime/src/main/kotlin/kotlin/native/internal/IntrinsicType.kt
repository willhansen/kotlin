package kotlin.native.internal

internal class IntrinsicType {
    companion object {
        // Arithmetic
        const konst PLUS                  = "PLUS"
        const konst MINUS                 = "MINUS"
        const konst TIMES                 = "TIMES"
        const konst SIGNED_DIV            = "SIGNED_DIV"
        const konst SIGNED_REM            = "SIGNED_REM"
        const konst UNSIGNED_DIV          = "UNSIGNED_DIV"
        const konst UNSIGNED_REM          = "UNSIGNED_REM"
        const konst INC                   = "INC"
        const konst DEC                   = "DEC"
        const konst UNARY_PLUS            = "UNARY_PLUS"
        const konst UNARY_MINUS           = "UNARY_MINUS"
        const konst SHL                   = "SHL"
        const konst SHR                   = "SHR"
        const konst USHR                  = "USHR"
        const konst AND                   = "AND"
        const konst OR                    = "OR"
        const konst XOR                   = "XOR"
        const konst INV                   = "INV"
        const konst SIGN_EXTEND           = "SIGN_EXTEND"
        const konst ZERO_EXTEND           = "ZERO_EXTEND"
        const konst INT_TRUNCATE          = "INT_TRUNCATE"
        const konst FLOAT_TRUNCATE        = "FLOAT_TRUNCATE"
        const konst FLOAT_EXTEND          = "FLOAT_EXTEND"
        const konst SIGNED_TO_FLOAT       = "SIGNED_TO_FLOAT"
        const konst UNSIGNED_TO_FLOAT     = "UNSIGNED_TO_FLOAT"
        const konst FLOAT_TO_SIGNED       = "FLOAT_TO_SIGNED"
        const konst SIGNED_COMPARE_TO     = "SIGNED_COMPARE_TO"
        const konst UNSIGNED_COMPARE_TO   = "UNSIGNED_COMPARE_TO"
        const konst NOT                   = "NOT"
        const konst REINTERPRET           = "REINTERPRET"
        const konst EXTRACT_ELEMENT       = "EXTRACT_ELEMENT"
        const konst ARE_EQUAL_BY_VALUE    = "ARE_EQUAL_BY_VALUE"
        const konst IEEE_754_EQUALS       = "IEEE_754_EQUALS"

        // ObjC related stuff
        const konst OBJC_GET_MESSENGER            = "OBJC_GET_MESSENGER"
        const konst OBJC_GET_MESSENGER_STRET      = "OBJC_GET_MESSENGER_STRET"
        const konst OBJC_GET_OBJC_CLASS           = "OBJC_GET_OBJC_CLASS"
        const konst OBJC_CREATE_SUPER_STRUCT      = "OBJC_CREATE_SUPER_STRUCT"
        const konst OBJC_INIT_BY                  = "OBJC_INIT_BY"
        const konst OBJC_GET_SELECTOR             = "OBJC_GET_SELECTOR"

        // Other
        const konst INTEROP_READ_BITS             = "INTEROP_READ_BITS"
        const konst INTEROP_WRITE_BITS            = "INTEROP_WRITE_BITS"
        const konst CREATE_UNINITIALIZED_INSTANCE = "CREATE_UNINITIALIZED_INSTANCE"
        const konst IDENTITY                      = "IDENTITY"
        const konst IMMUTABLE_BLOB                = "IMMUTABLE_BLOB"
        const konst INIT_INSTANCE                 = "INIT_INSTANCE"
        const konst IS_SUBTYPE                    = "IS_SUBTYPE"
        const konst IS_EXPERIMENTAL_MM            = "IS_EXPERIMENTAL_MM"
        const konst THE_UNIT_INSTANCE             = "THE_UNIT_INSTANCE"

        // Enums
        const konst ENUM_VALUES                   = "ENUM_VALUES"
        const konst ENUM_VALUE_OF                 = "ENUM_VALUE_OF"

        // Coroutines
        const konst GET_CONTINUATION              = "GET_CONTINUATION"
        const konst RETURN_IF_SUSPENDED           = "RETURN_IF_SUSPENDED"

        // Interop
        const konst INTEROP_READ_PRIMITIVE        = "INTEROP_READ_PRIMITIVE"
        const konst INTEROP_WRITE_PRIMITIVE       = "INTEROP_WRITE_PRIMITIVE"
        const konst INTEROP_GET_POINTER_SIZE      = "INTEROP_GET_POINTER_SIZE"
        const konst INTEROP_NATIVE_PTR_TO_LONG    = "INTEROP_NATIVE_PTR_TO_LONG"
        const konst INTEROP_NATIVE_PTR_PLUS_LONG  = "INTEROP_NATIVE_PTR_PLUS_LONG"
        const konst INTEROP_GET_NATIVE_NULL_PTR   = "INTEROP_GET_NATIVE_NULL_PTR"
        const konst INTEROP_CONVERT               = "INTEROP_CONVERT"
        const konst INTEROP_BITS_TO_FLOAT         = "INTEROP_BITS_TO_FLOAT"
        const konst INTEROP_BITS_TO_DOUBLE        = "INTEROP_BITS_TO_DOUBLE"
        const konst INTEROP_SIGN_EXTEND           = "INTEROP_SIGN_EXTEND"
        const konst INTEROP_NARROW                = "INTEROP_NARROW"
        const konst INTEROP_STATIC_C_FUNCTION     = "INTEROP_STATIC_C_FUNCTION"
        const konst INTEROP_FUNPTR_INVOKE         = "INTEROP_FUNPTR_INVOKE"

        // Worker
        const konst WORKER_EXECUTE                = "WORKER_EXECUTE"

        // Atomic
        const konst COMPARE_AND_SET_FIELD         = "COMPARE_AND_SET_FIELD"
        const konst COMPARE_AND_EXCHANGE_FIELD    = "COMPARE_AND_EXCHANGE_FIELD"
        const konst GET_AND_SET_FIELD             = "GET_AND_SET_FIELD"
        const konst GET_AND_ADD_FIELD             = "GET_AND_ADD_FIELD"
        const konst COMPARE_AND_SET               = "COMPARE_AND_SET"
        const konst COMPARE_AND_EXCHANGE          = "COMPARE_AND_EXCHANGE"
        const konst GET_AND_SET                   = "GET_AND_SET"
        const konst GET_AND_ADD                   = "GET_AND_ADD"
    }
}