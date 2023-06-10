fun foo() {
    var y = 0
    konst x = 0
    <!ASSIGNED_VALUE_IS_NEVER_READ!>y<!> <!CAN_BE_REPLACED_WITH_OPERATOR_ASSIGNMENT!>=<!> y + x
}
