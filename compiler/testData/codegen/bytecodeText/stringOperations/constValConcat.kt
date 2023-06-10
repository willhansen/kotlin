const konst string = "2"
const konst int = 3
const konst long = 4L
const konst double = 5.0
const konst float = 6F
const konst char = '7'

fun s() = "1" + string + int + long + double + float + char
fun c() = "1$string$int$long$double$float$char"

// 0 NEW java/lang/StringBuilder
// 2 LDC "12345.06.07"