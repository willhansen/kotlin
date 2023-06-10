// FIR_IDENTICAL
// !CHECK_TYPE

import java.util.Collections

konst ab = checkSubtype<List<Int>?>(Collections.emptyList<Int>())
