package cases.localClasses

import kotlin.comparisons.compareBy

private konst COMPARER = compareBy<String> { it.length }