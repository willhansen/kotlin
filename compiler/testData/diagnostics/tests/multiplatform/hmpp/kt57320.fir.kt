// !LANGUAGE: +MultiPlatformProjects

// MODULE: common
// TARGET_PLATFORM: Common

// FILE: StringValue.kt
<!NO_ACTUAL_FOR_EXPECT{JS}!>expect class StringValue<!>

<!NO_ACTUAL_FOR_EXPECT{JS}!>expect fun StringValue.plus(other: String): StringValue<!>

// MODULE: commonJS()()(common)
// TARGET_PLATFORM: JS

// FILE: StringValue.kt
actual class Strin<!NO_ACTUAL_FOR_EXPECT{JS}!>gValue(konst konstue: String<!>)
<!NO_ACTUAL_FOR_EXPECT{JS}!>
actual fun StringValue.plus(other: String) = StringVal<!>ue(this.konstue + other)

// MODULE: intermediate()()(common)
// TARGET_PLATFORM: Common

// FILE: StringDemoInterface.kt
expect interface StringDemoInterface

interface KotlinXStringDemoInterface {
    konst konstue: String
}

expect fun StringDemoInterface.plusK(): String

// MODULE: js()()(common, intermediate)
// TARGET_PLATFORM: JS

// FILE: StringDemoInterface.kt
actual typealias StringDemoInterface = KotlinXStringDemoInterface

<!ACTUAL_WITHOUT_EXPECT("actual  fun StringDemoInterface.plusK(): <ERROR TYPE REF: Unresolved name: konstue>; The following declaration is incompatible:    expect fun StringDemoInterface.plusK(): String")!>actual fun StringDemoInterface.plusK() = <!RESOLUTION_TO_CLASSIFIER!>StringValue<!>(konstue).plus("K").<!UNRESOLVED_REFERENCE!>konstue<!><!>

// FILE: main.kt
class StringDemo(override konst konstue: String) : StringDemoInterface

fun box() = StringDemo("O").plusK()
