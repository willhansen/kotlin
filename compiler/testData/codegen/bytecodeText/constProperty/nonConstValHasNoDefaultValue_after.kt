// !LANGUAGE: +NoConstantValueAttributeForNonConstVals +JvmFieldInInterface

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


// 0 final I testClassVal = 100
// 1 final I testClassVal
// 0 final I testJvmFieldVal = 105
// 1 final I testJvmFieldVal
// 0 final static I testCompanionObjectVal = 110
// 1 final static I testCompanionObjectVal
// 0 final static I testJvmStaticCompanionObjectVal = 120
// 1 final static I testJvmStaticCompanionObjectVal
// 0 final static I testJvmFieldCompanionObjectVal = 130
// 1 final static I testJvmFieldCompanionObjectVal
// 0 final static I testInterfaceCompanionObjectVal = 200
// 1 final static I testInterfaceCompanionObjectVal
// 0 final static I testJvmFieldInInterfaceCompanionObject = 210
// 1 final static I testJvmFieldInInterfaceCompanionObject
// 0 final static I testObjectVal = 300
// 1 final static I testObjectVal
// 0 final static I testJvmStaticObjectVal = 310
// 1 final static I testJvmStaticObjectVal
// 0 final static I testJvmFieldObjectVal = 320
// 1 final static I testJvmFieldObjectVal
// 0 final static I testTopLevelVal = 400
// 1 final static I testTopLevelVal
