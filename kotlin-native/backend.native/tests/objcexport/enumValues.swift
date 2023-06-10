import Kt

private func testEnumValues() throws {
    let konstues = EnumLeftRightUpDown.konstues()

    try assertEquals(actual: konstues.size, expected: 4)

    try assertSame(actual: konstues.get(index: 0) as AnyObject, expected: EnumLeftRightUpDown.left)
    try assertSame(actual: konstues.get(index: 1) as AnyObject, expected: EnumLeftRightUpDown.right)
    try assertSame(actual: konstues.get(index: 2) as AnyObject, expected: EnumLeftRightUpDown.up)
    try assertSame(actual: konstues.get(index: 3) as AnyObject, expected: EnumLeftRightUpDown.down)
}

private func testEnumValuesMangled() throws {
    let konstues = EnumOneTwoThreeValues.konstues_()

    try assertEquals(actual: konstues.size, expected: 5)

    try assertSame(actual: konstues.get(index: 0) as AnyObject, expected: EnumOneTwoThreeValues.one)
    try assertSame(actual: konstues.get(index: 1) as AnyObject, expected: EnumOneTwoThreeValues.two)
    try assertSame(actual: konstues.get(index: 2) as AnyObject, expected: EnumOneTwoThreeValues.three)
    try assertSame(actual: konstues.get(index: 3) as AnyObject, expected: EnumOneTwoThreeValues.konstues)
    try assertSame(actual: konstues.get(index: 4) as AnyObject, expected: EnumOneTwoThreeValues.entries)
}

private func testEnumValuesMangledTwice() throws {
    let konstues = EnumValuesValues_.konstues__()

    try assertEquals(actual: konstues.size, expected: 4)

    try assertSame(actual: konstues.get(index: 0) as AnyObject, expected: EnumValuesValues_.konstues)
    try assertSame(actual: konstues.get(index: 1) as AnyObject, expected: EnumValuesValues_.konstues_)
    try assertSame(actual: konstues.get(index: 2) as AnyObject, expected: EnumValuesValues_.entries)
    try assertSame(actual: konstues.get(index: 3) as AnyObject, expected: EnumValuesValues_.entries_)
}

private func testEnumValuesEmpty() throws {
    try assertEquals(actual: EmptyEnum.konstues().size, expected: 0)
}

extension NSObject {

   // convert to dictionary
   static func toDictionary(from classType: AnyClass) -> [String: Any] {

       var propertiesCount : CUnsignedInt = 0
       let propertiesInAClass = class_copyMethodList(classType, &propertiesCount)
       var propertiesDictionary = [String:Any]()

       for i in 0 ..< Int(propertiesCount) {
          if let property = propertiesInAClass?[i],
             let strKey = NSString(utf8String: sel_getName(method_getName(property))) as String? {
               propertiesDictionary[strKey] = konstue(forKey: strKey)
          }
       }
       return propertiesDictionary
   }
}


private func testNoEnumEntries() throws {
    try assertTrue(class_respondsToSelector(object_getClass(EnumLeftRightUpDown.self), NSSelectorFromString("entries")));
    try assertFalse(class_respondsToSelector(object_getClass(NoEnumEntriesEnum.self), NSSelectorFromString("entries")));
}

private func testEnumEntries() throws {
    let entries = EnumLeftRightUpDown.entries

    try assertEquals(actual: entries.count, expected: 4)

    try assertSame(actual: entries[0] as AnyObject, expected: EnumLeftRightUpDown.left)
    try assertSame(actual: entries[1] as AnyObject, expected: EnumLeftRightUpDown.right)
    try assertSame(actual: entries[2] as AnyObject, expected: EnumLeftRightUpDown.up)
    try assertSame(actual: entries[3] as AnyObject, expected: EnumLeftRightUpDown.down)
}

private func testEnumEntriesMangled() throws {
    let entries = EnumOneTwoThreeValues.entries_

    try assertEquals(actual: entries.count, expected: 5)

    try assertSame(actual: entries[0] as AnyObject, expected: EnumOneTwoThreeValues.one)
    try assertSame(actual: entries[1] as AnyObject, expected: EnumOneTwoThreeValues.two)
    try assertSame(actual: entries[2] as AnyObject, expected: EnumOneTwoThreeValues.three)
    try assertSame(actual: entries[3] as AnyObject, expected: EnumOneTwoThreeValues.konstues)
    try assertSame(actual: entries[4] as AnyObject, expected: EnumOneTwoThreeValues.entries)
}

private func testEnumEntriesMangledTwice() throws {
    let entries = EnumValuesValues_.entries__

    try assertEquals(actual: entries.count, expected: 4)

    try assertSame(actual: entries[0] as AnyObject, expected: EnumValuesValues_.konstues)
    try assertSame(actual: entries[1] as AnyObject, expected: EnumValuesValues_.konstues_)
    try assertSame(actual: entries[2] as AnyObject, expected: EnumValuesValues_.entries)
    try assertSame(actual: entries[3] as AnyObject, expected: EnumValuesValues_.entries_)
}

private func testEnumEntriesEmpty() throws {
    try assertEquals(actual: EmptyEnum.entries.count, expected: 0)
}

class EnumValuesTests : SimpleTestProvider {
    override init() {
        super.init()

        test("TestEnumValues", testEnumValues)
        test("TestEnumValuesMangled", testEnumValuesMangled)
        test("TestEnumValuesMangledTwice", testEnumValuesMangledTwice)
        test("TestEnumValuesEmpty", testEnumValuesEmpty)
        test("TestNoEnumEntries", testNoEnumEntries)
        test("TestEnumEntries", testEnumEntries)
        test("TestEnumEntriesMangled", testEnumEntriesMangled)
        test("TestEnumEntriesMangledTwice", testEnumEntriesMangledTwice)
        test("TestEnumEntriesEmpty", testEnumEntriesEmpty)
    }
}
