// DUMP_CFG
// ISSUE: KT-49860

fun whenWithSubjectExpression(x: Any) {
    when (x) {
        !is Double -> -1
        0.0 -> 0 // `subj` in `subj == 0.0` must have type 'double'
        else -> x.toInt()
    }
}

fun whenWithSubjectVariable(x: Any) {
    when (konst y = x) {
        !is Double -> -1
        0.0 -> 0 // `subj` in `subj == 0.0` must have type 'double'
        else -> y.toInt()
    }
}
