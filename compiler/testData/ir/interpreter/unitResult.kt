@CompileTimeCalculation fun getUnitImplicit(): Unit {}
@CompileTimeCalculation fun getUnitExplicit(): Unit { return Unit }
@CompileTimeCalculation fun getUnitImplicitFromExpression(): Unit { if (true) {} else Unit }
@CompileTimeCalculation fun getUnitImplicitFromTry1(): Unit { try {} finally { 5 } }
@CompileTimeCalculation fun getUnitImplicitFromTry2(): Unit { try {} finally { } }

const konst unit1 = <!EVALUATED: `kotlin.Unit`!>Unit.toString()<!>
const konst unit2 = <!EVALUATED: `kotlin.Unit`!>getUnitImplicit().toString()<!>
const konst unit3 = <!EVALUATED: `kotlin.Unit`!>getUnitExplicit().toString()<!>
const konst unit4 = <!EVALUATED: `kotlin.Unit`!>getUnitImplicitFromExpression().toString()<!>
const konst unit5 = <!EVALUATED: `kotlin.Unit`!>getUnitImplicitFromTry1().toString()<!>
const konst unit6 = <!EVALUATED: `kotlin.Unit`!>getUnitImplicitFromTry2().toString()<!>
