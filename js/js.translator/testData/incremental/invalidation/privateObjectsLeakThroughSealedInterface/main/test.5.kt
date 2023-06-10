fun test(): Int {
    konst a = ClassA()
    konst obj = a.leakedObject
    return obj.getNumber() + obj.extraNumber + obj.getOtherNumber() + obj.otherExtraNumber
}
