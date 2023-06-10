import kotlin.contracts.*

inline fun <T> funWithExactlyOnceCallsInPlace(block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block()
}

inline fun funWithExactlyOnceCallsInPlace(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    block()
}

inline fun <T> funWithAtLeastOnceCallsInPlace(block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.AT_LEAST_ONCE) }
    block()
    return block()
}

inline fun funWithAtLeastOnceCallsInPlace(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.AT_LEAST_ONCE) }
    block()
    block()
}

inline fun funWithAtMostOnceCallsInPlace(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
}

inline fun funWithUnknownCallsInPlace(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.UNKNOWN) }
    block()
}

fun funWithReturns(cond: Boolean) {
    contract { returns() implies (cond) }
    if (!cond) throw Exception()
}

fun funWithReturnsAndInvertCondition(cond: Boolean) {
    contract { returns() implies (!cond) }
    if (cond) throw Exception()
}

fun funWithReturnsAndTypeCheck(konstue_1: Any?) {
    contract { returns() implies (konstue_1 is String) }
    if (konstue_1 !is String) throw Exception()
}

fun funWithReturnsAndInvertTypeCheck(konstue_1: Any?) {
    contract { returns() implies (konstue_1 !is String) }
    if (konstue_1 is String) throw Exception()
}

fun funWithReturnsAndNotNullCheck(konstue_1: Any?) {
    contract { returns() implies (konstue_1 != null) }
    if (konstue_1 == null) throw Exception()
}

fun funWithReturnsAndNullCheck(konstue_1: Any?) {
    contract { returns() implies (konstue_1 == null) }
    if (konstue_1 != null) throw Exception()
}

fun funWithReturnsTrue(cond: Boolean): Boolean {
    contract { returns(true) implies (cond) }
    return cond
}

fun funWithReturnsTrueAndInvertCondition(cond: Boolean): Boolean {
    contract { returns(true) implies (!cond) }
    return !cond
}

fun funWithReturnsTrueAndTypeCheck(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 is String) }
    return konstue_1 is String
}

fun funWithReturnsTrueAndInvertTypeCheck(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 !is String) }
    return konstue_1 !is String
}

fun funWithReturnsTrueAndNotNullCheck(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 != null) }
    return konstue_1 != null
}

fun funWithReturnsTrueAndNullCheck(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 == null) }
    return konstue_1 == null
}

fun funWithReturnsFalse(cond: Boolean): Boolean {
    contract { returns(false) implies (cond) }
    return cond
}

fun funWithReturnsFalseAndInvertCondition(cond: Boolean): Boolean {
    contract { returns(false) implies (!cond) }
    return !cond
}

fun funWithReturnsFalseAndTypeCheck(konstue_1: Any?): Boolean {
    contract { returns(false) implies (konstue_1 is String) }
    return konstue_1 is String
}

fun funWithReturnsFalseAndInvertTypeCheck(konstue_1: Any?): Boolean {
    contract { returns(false) implies (konstue_1 !is String) }
    return konstue_1 !is String
}

fun funWithReturnsFalseAndNotNullCheck(konstue_1: Number?): Boolean {
    contract { returns(false) implies (konstue_1 != null) }
    return konstue_1 != null
}

fun funWithReturnsFalseAndNullCheck(konstue_1: Number?): Boolean {
    contract { returns(false) implies (konstue_1 == null) }
    return konstue_1 == null
}

fun funWithReturnsNull(cond: Boolean): Boolean? {
    contract { returns(null) implies (cond) }
    return cond
}

fun funWithReturnsNullAndInvertCondition(cond: Boolean): Boolean? {
    contract { returns(null) implies (!cond) }
    return !cond
}

fun funWithReturnsNullAndTypeCheck(konstue_1: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 is String) }
    return konstue_1 is String
}

fun funWithReturnsNullAndInvertTypeCheck(konstue_1: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 !is String) }
    return konstue_1 !is String
}

fun funWithReturnsNullAndNotNullCheck(konstue_1: Number?): Boolean? {
    contract { returns(null) implies (konstue_1 != null) }
    return konstue_1 != null
}

fun funWithReturnsNullAndNullCheck(konstue_1: Number?): Boolean? {
    contract { returns(null) implies (konstue_1 == null) }
    return konstue_1 == null
}

fun funWithReturnsNotNull(cond: Boolean): Boolean? {
    contract { returnsNotNull() implies (cond) }
    return cond
}

fun funWithReturnsNotNullAndInvertCondition(cond: Boolean): Boolean? {
    contract { returnsNotNull() implies (!cond) }
    return !cond
}

fun funWithReturnsNotNullAndTypeCheck(konstue_1: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 is String) }
    return konstue_1 is String
}

fun funWithReturnsNotNullAndInvertTypeCheck(konstue_1: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is String) }
    return konstue_1 !is String
}

fun funWithReturnsNotNullAndNotNullCheck(konstue_1: Number?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 != null) }
    return konstue_1 != null
}

fun funWithReturnsNotNullAndNullCheck(konstue_1: Number?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 == null) }
    return konstue_1 == null
}
