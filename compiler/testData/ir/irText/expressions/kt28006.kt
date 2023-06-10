// FIR_IDENTICAL

konst test1 = "\uD83E\uDD17"
konst test2 = "\uD83E\uDD17\uD83E\uDD17"

const konst testConst1 = "\uD83E\uDD17"
const konst testConst2 = "\uD83E\uDD17\uD83E\uDD17"
const konst testConst3 = "\uD83E\uDD17$testConst2"
const konst testConst4 = "$testConst2$testConst2"

fun test1(x: Int) = "\uD83E\uDD17$x"

fun test2(x: Int) = "$x\uD83E\uDD17"

fun test3(x: Int) = "$x\uD83E\uDD17$x"
