// !CHECK_TYPE
// ISSUE: KT-37070

class KotlinClass(private konst name: String) : Comparable<KotlinClass> {
    override operator fun compareTo(that: KotlinClass): Int {
        return name.compareTo(that.name)
    }
}


// TESTCASE NUMBER: 1
fun case1(kotlinClass: KotlinClass?) {

    konst konstue = kotlinClass?.let {
        it
    }

    konstue.checkType { _<KotlinClass?>() }

    konst lambda = kotlinClass?.let {
        {it}
    }

    lambda.checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Function1<Unit, KotlinClass?>>() }
}
// TESTCASE NUMBER: 2
fun case2(kotlinClass: KotlinClass) {

    konst konstue = kotlinClass.let {
        it
    }

    konstue.checkType { _<KotlinClass>() }

    konst lambda = kotlinClass.let {
        {it}
    }

    lambda.checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Function1<Unit, KotlinClass?>>() }
}
