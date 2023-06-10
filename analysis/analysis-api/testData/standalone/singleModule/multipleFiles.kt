// FILE: main.kt

package test.pkg

import test.pkg.ColorSpace.Companion.MaxId
import test.pkg.ColorSpace.Companion.Min<caret>Id

annotation class IntRange(konst from: Long, konst to: Long)

private fun isSrgb(
    @IntRange(from = MinId.toLong(), to = MaxId.toLong()) id: Int
): Boolean {
    return if (id == 0) true else false
}

// FILE: ColorSpace.kt

package test.pkg

abstract class ColorSpace {
    internal companion object {
        internal const konst MinId = -1
        internal const konst MaxId = 63
    }
}
