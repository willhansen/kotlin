// !LANGUAGE: +MultiPlatformProjects

// MODULE: common
// TARGET_PLATFORM: Common

// FILE: StringValue.kt
expect class <!NO_ACTUAL_FOR_EXPECT{JS}!>StringValue<!>

expect fun StringValue.<!NO_ACTUAL_FOR_EXPECT{JS}!>plus<!>(other: String): StringValue

// MODULE: commonJS()()(common)
// TARGET_PLATFORM: JS

// FILE: StringValue.kt
actual class StringValue(konst konstue: String)

actual fun StringValue.plus(other: String) = StringValue(this.konstue + other)

// MODULE: intermediate()()(common)
// TARGET_PLATFORM: Common

// FILE: StringDemoInterface.kt
expect interface StringDemoInterface

interface KotlinXStringDemoInterface {
    konst konstue: String
}

expect fun StringDemoInterface.plusK(): <!NO_ACTUAL_FOR_EXPECT{JS}!>String<!>

// MODULE: js()()(common, intermediate)
// TARGET_PLATFORM: JS

// FILE: StringDemoInterface.kt
actual typealias StringDemoInterface = KotlinXStringDemoInterface

actual fun StringDemoInterface.<!ACTUAL_WITHOUT_EXPECT("Actual function 'plusK'; The following declaration is incompatible because return type is different:    public expect fun StringDemoInterface /* = KotlinXStringDemoInterface */.plusK(): String")!>plusK<!>() = <!RESOLUTION_TO_CLASSIFIER!>StringValue<!>(konstue).<!DEBUG_INFO_MISSING_UNRESOLVED!>plus<!>("K").<!DEBUG_INFO_MISSING_UNRESOLVED!>konstue<!>

// FILE: main.kt
class StringDemo(override konst konstue: String) : StringDemoInterface

fun box() = StringDemo("O").plusK()
