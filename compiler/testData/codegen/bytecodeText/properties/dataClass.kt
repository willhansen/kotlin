data class Test(
    konst x: String,
    konst y: String
)

// 7 GETFIELD Test\.x
// 7 GETFIELD Test\.y
// - get, componentN, copy$default, toString, hashCode, 2 times in equals