// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

abstract class AbstractBaseClass

open class OpenBaseClass

interface BaseInterface

inline class TestExtendsAbstractClass(konst x: Int) : <!VALUE_CLASS_CANNOT_EXTEND_CLASSES!>AbstractBaseClass<!>()

inline class TestExtendsOpenClass(konst x: Int) : <!VALUE_CLASS_CANNOT_EXTEND_CLASSES!>OpenBaseClass<!>()

inline class TestImplementsInterface(konst x: Int) : BaseInterface