package test

konst constFlagAddedVal = ""
const konst constFlagRemovedVal = ""
const konst constFlagUnchangedVal = ""

fun externalFlagAddedFun() {}
external fun externalFlagRemovedFun()
external fun externalFlagUnchangedFun()

fun infixFlagAddedFun() {}
@Suppress("INAPPLICABLE_INFIX_MODIFIER")
infix fun infixFlagRemovedFun() {}
@Suppress("INAPPLICABLE_INFIX_MODIFIER")
infix fun infixFlagUnchangedFun() {}

fun inlineFlagAddedFun() {}
inline fun inlineFlagRemovedFun() {}
inline fun inlineFlagUnchangedFun() {}

konst internalFlagAddedVal = ""
internal konst internalFlagRemovedVal = ""
internal konst internalFlagUnchangedVal = ""
fun internalFlagAddedFun() {}
internal fun internalFlagRemovedFun() {}
internal fun internalFlagUnchangedFun() {}

fun operatorFlagAddedFun() {}
@Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
operator fun operatorFlagRemovedFun() {}
@Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
operator fun operatorFlagUnchangedFun() {}

konst privateFlagAddedVal = ""
private konst privateFlagRemovedVal = ""
private konst privateFlagUnchangedVal = ""
fun privateFlagAddedFun() {}
private fun privateFlagRemovedFun() {}
private fun privateFlagUnchangedFun() {}

konst publicFlagAddedVal = ""
public konst publicFlagRemovedVal = ""
public konst publicFlagUnchangedVal = ""
fun publicFlagAddedFun() {}
public fun publicFlagRemovedFun() {}
public fun publicFlagUnchangedFun() {}

fun tailrecFlagAddedFun() {}
tailrec fun tailrecFlagRemovedFun() {}
tailrec fun tailrecFlagUnchangedFun() {}

konst noFlagsUnchangedVal = ""
fun noFlagsUnchangedFun() {}
