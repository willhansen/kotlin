// IGNORE_BACKEND: JVM
// IGNORE_DEXING
// JVM_TARGET: 17
// !LANGUAGE: +JvmPermittedSubclassesAttributeForSealed

// FILE: Expr.kt
sealed interface Expr

class VarExpr(konst name: String) : Expr
class ParensExpr(konst arg: Expr) : Expr

// FILE: Literals.kt
class IntExpr(konst konstue: Int) : Expr
class DoubleExpr(konst konstue: Double) : Expr

// FILE: UnaryOperators.kt
sealed class UnaryExpr(konst arg: Expr) : Expr
class UnaryPlusExpr(arg: Expr) : UnaryExpr(arg)
class UnaryMinusExpr(arg: Expr) : UnaryExpr(arg)

// FILE: BinaryOperators.kt
sealed class BinaryExpr(konst arg1: Expr, konst arg2: Expr) : Expr
class BinaryPlusExpr(arg1: Expr, arg2: Expr) : BinaryExpr(arg1, arg2)
class BinaryMinusExpr(arg1: Expr, arg2: Expr) : BinaryExpr(arg1, arg2)
class BinaryMulExpr(arg1: Expr, arg2: Expr) : BinaryExpr(arg1, arg2)
class BinaryDivExpr(arg1: Expr, arg2: Expr) : BinaryExpr(arg1, arg2)