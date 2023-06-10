package test

const konst constFlagAddedVal = ""
konst constFlagRemovedVal = ""
const konst constFlagUnchangedVal = ""

external fun externalFlagAddedFun()
fun externalFlagRemovedFun() {}
external fun externalFlagUnchangedFun()

@Suppress("INAPPLICABLE_INFIX_MODIFIER")
infix fun infixFlagAddedFun() {}
fun infixFlagRemovedFun() {}
@Suppress("INAPPLICABLE_INFIX_MODIFIER")
infix fun infixFlagUnchangedFun() {}

inline fun inlineFlagAddedFun() {}
fun inlineFlagRemovedFun() {}
inline fun inlineFlagUnchangedFun() {}

internal konst internalFlagAddedVal = ""
konst internalFlagRemovedVal = ""
internal konst internalFlagUnchangedVal = ""
internal fun internalFlagAddedFun() {}
fun internalFlagRemovedFun() {}
internal fun internalFlagUnchangedFun() {}

@Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
operator fun operatorFlagAddedFun() {}
fun operatorFlagRemovedFun() {}
@Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
operator fun operatorFlagUnchangedFun() {}

private konst privateFlagAddedVal = ""
konst privateFlagRemovedVal = ""
private konst privateFlagUnchangedVal = ""
private fun privateFlagAddedFun() {}
fun privateFlagRemovedFun() {}
private fun privateFlagUnchangedFun() {}

public konst publicFlagAddedVal = ""
konst publicFlagRemovedVal = ""
public konst publicFlagUnchangedVal = ""
public fun publicFlagAddedFun() {}
fun publicFlagRemovedFun() {}
public fun publicFlagUnchangedFun() {}

tailrec fun tailrecFlagAddedFun() {}
fun tailrecFlagRemovedFun() {}
tailrec fun tailrecFlagUnchangedFun() {}

konst noFlagsUnchangedVal = ""
fun noFlagsUnchangedFun() {}


