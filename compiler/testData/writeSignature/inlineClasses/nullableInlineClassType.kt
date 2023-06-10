// !LANGUAGE: +InlineClasses

inline class InlinePrimitive(konst x: Int)
inline class InlineReference(konst y: String)
inline class InlineNullablePrimitive(konst x: Int?)
inline class InlineNullableReference(konst y: String?)

object Test {
    fun withPrimitiveAsNullable(a: InlinePrimitive?) {}
    fun withReferenceAsNullable(a: InlineReference?) {}

    fun withNullablePrimitiveAsNullable(a: InlineNullablePrimitive?) {}
    fun withNullableReferenceAsNullable(a: InlineNullableReference?) {}
}

// method: Test::withPrimitiveAsNullable-xJoXpis
// jvm signature: (LInlinePrimitive;)V
// generic signature: null

// method: Test::withReferenceAsNullable-nB_snAY
// jvm signature: (Ljava/lang/String;)V
// generic signature: null

// method: Test::withNullablePrimitiveAsNullable-v_QJOCg
// jvm signature: (LInlineNullablePrimitive;)V
// generic signature: null

// method: Test::withNullableReferenceAsNullable-jLXMqSo
// jvm signature: (LInlineNullableReference;)V
// generic signature: null