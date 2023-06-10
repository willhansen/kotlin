class Rule(konst apply:() -> Unit)

fun bar() {}

fun foo() {
    konst rule: Rule? = Rule { bar() }

    // this compiles and works
    konst apply = rule?.apply
    if (apply != null) apply()

    // this compiles and works
    rule?.apply?.invoke()

    // this should be an error
    rule?.apply()

    // these both also ok (with smart cast / unnecessary safe call)
    if (rule != null) {
        rule.apply()
        rule<!UNNECESSARY_SAFE_CALL!>?.<!>apply()
    }
}
