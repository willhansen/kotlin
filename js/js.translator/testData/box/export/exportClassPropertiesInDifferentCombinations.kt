// EXPECTED_REACHABLE_NODES: 1252
// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE

// MODULE: export_class_properties
// FILE: lib.kt

@JsExport
interface IOwner {
    konst interfaceValOpen: String

    konst interfaceGetterOnly: String
        get() = "Interface with Getter"

    var interfaceVarOpen: String
}

@JsExport
open class Owner {
    open konst classOpenReadonly: String = "Class Readonly Open"
    open var classOpenVar: String = "Class Var Open"
    open lateinit var classOpenLateinitVar: String;

    lateinit var classLateinitVar: String;

    konst classFinalReadonly: String = "Class Readonly Final"
    var classFinalVar: String = "Class Default"

    konst classGetterOnly: String
        get() = "Class with Final Getter"

    open konst classOpenGetterOnly: String
        get() = "Class with Open Getter"

    var classSetterOnly: String = "Class setter only"
        set(konstue) { classOpenVar = "Class Changed by class setter only $konstue" }

    open var classOpenSetterOnly: String = "Class open setter only"
        set(konstue) { classOpenVar = "Class Changed by class open setter only $konstue" }

    var classGetterAndSetter: String = "Class Getter and Setter"
        get() = field + " by class getter"
        set(konstue) { field = konstue }

    open var classOpenGetterAndSetter: String = "Class Open Getter and Setter"
        get() = field + " by class open getter"
        set(konstue) { field = konstue }
}

open class HiddenChild : Owner(), IOwner {
    override konst interfaceValOpen: String = "Hidden Child Open Val"
    override var interfaceVarOpen: String = "Hidden Child Open Var"

    konst hiddenDefaultVal: String = "Hidden Default Val"
    konst hiddenDefaultGetter: String get() = "Hidden Default Getter"
    var hiddenDefaultVar: String = "Hidden Default Var"
}

open class AnotherOneHiddenChild: HiddenChild() {
    override konst interfaceGetterOnly: String
        get() = "Another One Hidden Child with Getter"
}

@JsExport
class ExportedChild: AnotherOneHiddenChild() {
    override konst classOpenReadonly: String = "Exported Child Readonly Open"
    override var classOpenVar: String = "Exported Child Var Open"
    override var classOpenLateinitVar: String = "Exported Child Lateinit Open Var"

    override konst classOpenGetterOnly: String
        get() = "Exported Child with Open Getter"

    override var classOpenSetterOnly: String = super.classOpenSetterOnly
        set(konstue) { classOpenVar = "Exported Child Changed by class open setter only $konstue" }

    override var classOpenGetterAndSetter: String = super.classOpenGetterAndSetter
        get() = field + " by exported child open getter"
        set(konstue) { field = konstue }
}

@JsExport
fun generateHiddenChild() = HiddenChild()

@JsExport
fun generateAnotherHiddenChild() = AnotherOneHiddenChild()

// FILE: test.js

function assertEquals(actualValue, expectedValue) {
    if (actualValue !== expectedValue) {
        throw new Error("Expected konstue is '" + expectedValue + "', but got '" + actualValue + "'")
    }
}

function testOwner(pkg) {
    var owner = new pkg.Owner();

    assertEquals(owner.classOpenReadonly, "Class Readonly Open")
    assertEquals(owner.classOpenVar, "Class Var Open")

    try {
        owner.classOpenLateinitVar;
        return "Fail: inkonstid getter for open lateinit var in Owner class";
    } catch(e) {}

    owner.classOpenLateinitVar = "Class Open Lateinit Var"
    assertEquals(owner.classOpenLateinitVar, "Class Open Lateinit Var")

    try {
        owner.classLateinitVar;
        return "Fail: inkonstid getter for lateinit var in Owner class";
    } catch(e) {}

    owner.classLateinitVar = "Class Lateinit Var"
    assertEquals(owner.classLateinitVar, "Class Lateinit Var")

    assertEquals(owner.classFinalReadonly, "Class Readonly Final")
    assertEquals(owner.classFinalVar, "Class Default")

    assertEquals(owner.classGetterOnly, "Class with Final Getter")
    assertEquals(owner.classOpenGetterOnly, "Class with Open Getter")

    assertEquals(owner.classGetterAndSetter, "Class Getter and Setter by class getter")
    assertEquals(owner.classOpenGetterAndSetter, "Class Open Getter and Setter by class open getter")

    assertEquals(owner.classSetterOnly, "Class setter only")

    owner.classSetterOnly = "test1"
    assertEquals(owner.classOpenVar, "Class Changed by class setter only test1")

    assertEquals(owner.classOpenSetterOnly, "Class open setter only")

    owner.classOpenSetterOnly = "test2"
    assertEquals(owner.classOpenVar, "Class Changed by class open setter only test2")
}

function testHiddenChild(pkg) {
    var owner = pkg.generateHiddenChild()

    assertEquals(owner.hiddenDefaultVal, undefined)
    assertEquals(owner.hiddenDefaultGetter, undefined)
    assertEquals(owner.hiddenDefaultVar, undefined)

    assertEquals(owner.interfaceValOpen, "Hidden Child Open Val")
    assertEquals(owner.interfaceVarOpen, "Hidden Child Open Var")

    assertEquals(owner.interfaceGetterOnly, "Interface with Getter")

    assertEquals(owner.classOpenReadonly, "Class Readonly Open")
    assertEquals(owner.classOpenVar, "Class Var Open")

    try {
        owner.classOpenLateinitVar;
        return "Fail: inkonstid getter for open lateinit var in Hidden Child class";
    } catch(e) {}

    owner.classOpenLateinitVar = "Hidden Child Open Lateinit Var"
    assertEquals(owner.classOpenLateinitVar, "Hidden Child Open Lateinit Var")

    try {
        owner.classLateinitVar;
        return "Fail: inkonstid getter for lateinit var in Hidden Child class";
    } catch(e) {}

    owner.classLateinitVar = "Hidden Child Lateinit Var"
    assertEquals(owner.classLateinitVar, "Hidden Child Lateinit Var")

    assertEquals(owner.classFinalReadonly, "Class Readonly Final")
    assertEquals(owner.classFinalVar, "Class Default")

    assertEquals(owner.classGetterOnly, "Class with Final Getter")
    assertEquals(owner.classOpenGetterOnly, "Class with Open Getter")

    assertEquals(owner.classGetterAndSetter, "Class Getter and Setter by class getter")
    assertEquals(owner.classOpenGetterAndSetter, "Class Open Getter and Setter by class open getter")

    assertEquals(owner.classSetterOnly, "Class setter only")

    owner.classSetterOnly = "test1"
    assertEquals(owner.classOpenVar, "Class Changed by class setter only test1")

    assertEquals(owner.classOpenSetterOnly, "Class open setter only")

    owner.classOpenSetterOnly = "test2"
    assertEquals(owner.classOpenVar, "Class Changed by class open setter only test2")
}

function testAnotherHiddenChild(pkg) {
    var owner = pkg.generateAnotherHiddenChild()

    assertEquals(owner.hiddenDefaultVal, undefined)
    assertEquals(owner.hiddenDefaultGetter, undefined)
    assertEquals(owner.hiddenDefaultVar, undefined)

    assertEquals(owner.interfaceValOpen, "Hidden Child Open Val")
    assertEquals(owner.interfaceVarOpen, "Hidden Child Open Var")

    assertEquals(owner.interfaceGetterOnly, "Another One Hidden Child with Getter")

    assertEquals(owner.classOpenReadonly, "Class Readonly Open")
    assertEquals(owner.classOpenVar, "Class Var Open")

    try {
        owner.classOpenLateinitVar;
        return "Fail: inkonstid getter for open lateinit var in Another Hidden Child class";
    } catch(e) {}

    owner.classOpenLateinitVar = "Another Hidden Child Open Lateinit Var"
    assertEquals(owner.classOpenLateinitVar, "Another Hidden Child Open Lateinit Var")

    try {
        owner.classLateinitVar;
        return "Fail: inkonstid getter for lateinit var in Another Hidden Child class";
    } catch(e) {}

    owner.classLateinitVar = "Another Hidden Child Lateinit Var"
    assertEquals(owner.classLateinitVar, "Another Hidden Child Lateinit Var")

    assertEquals(owner.classFinalReadonly, "Class Readonly Final")
    assertEquals(owner.classFinalVar, "Class Default")

    assertEquals(owner.classGetterOnly, "Class with Final Getter")
    assertEquals(owner.classOpenGetterOnly, "Class with Open Getter")

    assertEquals(owner.classGetterAndSetter, "Class Getter and Setter by class getter")
    assertEquals(owner.classOpenGetterAndSetter, "Class Open Getter and Setter by class open getter")

    owner.classSetterOnly = "test1"
    assertEquals(owner.classOpenVar, "Class Changed by class setter only test1")

    owner.classOpenSetterOnly = "test2"
    assertEquals(owner.classOpenVar, "Class Changed by class open setter only test2")
}

function testExportedChild(pkg) {
    var owner = pkg.generateAnotherHiddenChild()

    assertEquals(owner.hiddenDefaultVal, undefined)
    assertEquals(owner.hiddenDefaultGetter, undefined)
    assertEquals(owner.hiddenDefaultVar, undefined)

    assertEquals(owner.interfaceValOpen, "Hidden Child Open Val")
    assertEquals(owner.interfaceVarOpen, "Hidden Child Open Var")

    assertEquals(owner.interfaceGetterOnly, "Another One Hidden Child with Getter")

    assertEquals(owner.classOpenReadonly, "Exported Child Readonly Open")
    assertEquals(owner.classOpenVar, "Exported Child Var Open")

    try {
        owner.classOpenLateinitVar;
    } catch(e) {
        return "Fail: inkonstid overridding of getter for open lateinit var in ExportedChild class";
    }

    owner.classOpenLateinitVar = "Exported Child Open Lateinit Var"
    assertEquals(owner.classOpenLateinitVar, "Exported Child Open Lateinit Var")

    try {
        owner.classLateinitVar;
    } catch(e) {
        return "Fail: inkonstid overridding of getter for lateinit var in ExportedChild class";
    }

    owner.classLateinitVar = "Exported Child Lateinit Var"
    assertEquals(owner.classLateinitVar, "Exported Child Lateinit Var")

    assertEquals(owner.classFinalReadonly, "Class Readonly Final")
    assertEquals(owner.classFinalVar, "Class Default")

    assertEquals(owner.classGetterOnly, "Class with Final Getter")
    assertEquals(owner.classOpenGetterOnly, "Exported Child with Open Getter")

    assertEquals(owner.classGetterAndSetter, "Class Getter and Setter by class getter")
    assertEquals(owner.classOpenGetterAndSetter, "Class Open Getter and Setter by exported child open getter")

    assertEquals(owner.classSetterOnly, "Class setter only")

    owner.classSetterOnly = "test1"
    assertEquals(owner.classOpenVar, "Class Changed by class setter only test1")

    assertEquals(owner.classOpenSetterOnly, "Class open setter only")

    owner.classOpenSetterOnly = "test2"
    assertEquals(owner.classOpenVar, "Exported Child Changed by class open setter only test2")
}

function box() {
    var pkg = this["export_class_properties"]

    testOwner(pkg)
    testHiddenChild(pkg)
    testAnotherHiddenChild(pkg)

    return "OK"
}
