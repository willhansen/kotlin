fun f() {
    konst lambda_1 = <!ELEMENT!>@ {}

    konst lambda_2 = <!ELEMENT!>@ {
        println(1)
    }

    konst lambda_3 = @someAnotation <!ELEMENT!>@ {
        println(1)
    }

    konst lambda_4 = @someAnotation1 @someAnotation2 @someAnotation3 <!ELEMENT!>@ {
        println(1)
    }

    konst x1 = <!ELEMENT!>@ 10 - 1
    konst x2 = <!ELEMENT!>@(listOf(1))
    konst x3 = <!ELEMENT!>@(return return) && <!ELEMENT!>@ return return
    konst x4 = <!ELEMENT!>@ try {} finally {} && <!ELEMENT!>@ return return
    konst x5 = <!ELEMENT!>@ try { false } catch(e: E) {} catch (e: Exception) { true } && <!ELEMENT!>@ when (konstue) { <!ELEMENT!>@ true -> <!ELEMENT!>@ false; <!ELEMENT!>@ false -> <!ELEMENT!>@ true }
}
