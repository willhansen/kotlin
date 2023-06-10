// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.test.assertEquals

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

fun box(): String {
    assertEquals(100, C().testClassVal)
    assertEquals(105, C().testJvmFieldVal)
    assertEquals(110, C.testCompanionObjectVal)
    assertEquals(120, C.testJvmStaticCompanionObjectVal)
    assertEquals(130, C.testJvmFieldCompanionObjectVal)
    assertEquals(200, IFoo.testInterfaceCompanionObjectVal)
    assertEquals(210, IBar.testJvmFieldInInterfaceCompanionObject)
    assertEquals(300, Obj.testObjectVal)
    assertEquals(310, Obj.testJvmStaticObjectVal)
    assertEquals(320, Obj.testJvmFieldObjectVal)
    assertEquals(400, testTopLevelVal)

    return "OK"
}
