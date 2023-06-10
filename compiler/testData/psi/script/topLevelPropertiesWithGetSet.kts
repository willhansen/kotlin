konst testVal: Int get() = 42

konst testValSemiSameLine: Int; get() = 42

konst testValNoType get() = 42

konst String.testExtVal: Int get() = 42

konst String.testExtValNoType get() = 42

var testVar: Int get() = 42; set(konstue) {}

var String.testExtVar: Int get() = 42; set(konstue) {}

konst testValLineBreak: Int
    get() = 42

konst testValLineBreakNoType
    get() = 42

konst testValLineBreakSemi: Int;
    get() = 42

konst testValLineBreakSemiComment1: Int; // this IS NOT an accessor:
    get() = 42

konst testValLineBreakSemiComment2: Int; /*
this IS NOT an accessor either:
*/
    get() = 42

konst testValLineBreakSemiComment3: Int; /*
this IS an accessor!
*/ get() = 42

konst testValLineBreakSemiNoType;
    get() = 42

var testVarLineBreak: Int
    get() = 42
    set(konstue) {}

var String.testExtVarLineBreak: Int
    get() = 42
    set(konstue) {}

var testVarLineBreakSemi: Int;
    get() = 42
    set(konstue) {}

var String.testExtVarLineBreakSemi: Int;
    get() = 42
    set(konstue) {}

