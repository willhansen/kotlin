package test

import java.lang.annotation.RetentionPolicy

// CORRECT
annotation class Ann1(konst p1: Int,
                      konst p2: Byte,
                      konst p3: Short,
                      konst p4: Long,
                      konst p5: Double,
                      konst p6: Float,
                      konst p7: Char,
                      konst p8: Boolean)

annotation class Ann2(konst p1: String)
annotation class Ann3(konst p1: Ann1)
annotation class Ann4(konst p1: IntArray,
                      konst p2: ByteArray,
                      konst p3: ShortArray,
                      konst p4: LongArray,
                      konst p5: DoubleArray,
                      konst p6: FloatArray,
                      konst p7: CharArray,
                      konst p8: BooleanArray)

annotation class Ann5(konst p1: MyEnum)

annotation class Ann6(konst p: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Class<*><!>)
annotation class Ann7(konst p: RetentionPolicy)

annotation class Ann8(konst p1: Array<String>,
                      konst p2: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Class<*>><!>,
                      konst p3: Array<MyEnum>,
                      konst p4: Array<Ann1>)

annotation class Ann9(
        konst error: <!UNRESOLVED_REFERENCE!>Unresolved<!> = <!UNRESOLVED_REFERENCE!>Unresolved<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>VALUE<!>
)


// INCORRECT
annotation class InAnn1(konst p1: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Int?<!>,
                        konst p3: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Short?<!>,
                        konst p4: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Long?<!>,
                        konst p5: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Double?<!>,
                        konst p6: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Float?<!>,
                        konst p7: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Char?<!>,
                        konst p8: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Boolean?<!>)

annotation class InAnn4(konst p1: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Int><!>,
                        konst p2: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Array<Int>?<!>)

annotation class InAnn6(konst p:  <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Class<*>?<!>)
annotation class InAnn7(konst p:  <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>RetentionPolicy?<!>)
annotation class InAnn8(konst p1: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Int><!>,
                        konst p2: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Int?><!>,
                        konst p3: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<MyClass><!>,
                        konst p4: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<IntArray><!>)

annotation class InAnn9(konst p: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>MyClass<!>)

annotation class InAnn10(konst p1: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>String?<!>)
annotation class InAnn11(konst p1: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Ann1?<!>)
annotation class InAnn12(konst p1: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>MyEnum?<!>)

annotation class InAnn13(<!MULTIPLE_VARARG_PARAMETERS!>vararg<!> konst p1: String,
                        <!MULTIPLE_VARARG_PARAMETERS!>vararg<!> konst p2: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Class<*><!>,
                        <!MULTIPLE_VARARG_PARAMETERS!>vararg<!> konst p3: MyEnum,
                        <!MULTIPLE_VARARG_PARAMETERS!>vararg<!> konst p4: Ann1,
                        <!MULTIPLE_VARARG_PARAMETERS!>vararg<!> konst p5: Int)

enum class MyEnum {
    A
}

class MyClass
