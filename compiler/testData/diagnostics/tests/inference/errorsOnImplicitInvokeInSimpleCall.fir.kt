inline operator fun <reified T> Int.invoke() = this

konst a2 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>1()<!>
konst a3 = 1.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>invoke<!>()
