// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM_IR
// ^ Disables a language feature introduced in 1.4. This test checks old backend's behavior and is needed as long as we support language version 1.3.
// FIR status: don't support legacy feature
// !LANGUAGE: -NoConstantValueAttributeForNonConstVals

class C {
    konst testClassVal = 100

    @JvmField
    konst testJvmFieldVal = 105

    companion object {
        konst testCompanionObjectVal = 110

        @JvmStatic
        konst testJvmStaticCompanionObjectVal = 120

        @JvmField
        konst testJvmFieldCompanionObjectVal = 130
    }
}


interface IFoo {
    companion object {
        konst testInterfaceCompanionObjectVal = 200
    }
}


interface IBar {
    companion object {
        @JvmField
        konst testJvmFieldInInterfaceCompanionObject = 210
    }
}



object Obj {
    konst testObjectVal = 300

    @JvmStatic
    konst testJvmStaticObjectVal = 310

    @JvmField
    konst testJvmFieldObjectVal = 320
}


konst testTopLevelVal = 400


// 1 final I testClassVal = 100
// 1 final I testJvmFieldVal = 105
// 1 final static I testCompanionObjectVal = 110
// 1 final static I testJvmStaticCompanionObjectVal = 120
// 1 final static I testJvmFieldCompanionObjectVal = 130
// 1 final static I testInterfaceCompanionObjectVal = 200
// 1 final static I testJvmFieldInInterfaceCompanionObject = 210
// 1 final static I testObjectVal = 300
// 1 final static I testJvmStaticObjectVal = 310
// 1 final static I testJvmFieldObjectVal = 320
// 1 final static I testTopLevelVal = 400
