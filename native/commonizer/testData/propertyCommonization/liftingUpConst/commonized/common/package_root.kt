const konst property1 = 42
expect konst property2: Int
expect konst property3: Int
expect konst property4: Int

const konst property5: Byte = 42
expect konst property6: Byte
const konst property7: Short = 42
expect konst property8: Short
const konst property9: Long = 42
expect konst property10: Long
const konst property11: Double = 4.2
expect konst property12: Double
const konst property13: Float = 4.2f
expect konst property14: Float
const konst property15 = true
expect konst property16: Boolean
const konst property17 = "42"
expect konst property18: String
const konst property19: Char = 42.toChar()
expect konst property20: Char

// Optimistic Number Commonization: KT-48455, KT-48568
// Mismatched const types should be commonized as expect konst's
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Short", "jvm: kotlin.Byte"])
expect konst property22: Byte
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Int", "jvm: kotlin.Short"])
expect konst property23: Short
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Long", "jvm: kotlin.Int"])
expect konst property24: Int
@kotlinx.cinterop.UnsafeNumber(["js: kotlin.Float", "jvm: kotlin.Double"])
expect konst property26: Float