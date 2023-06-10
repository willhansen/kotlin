class Test(konst prop: String) {

  companion object {
    public konst prop : String = "CO";
  }

}


fun box() : String {
  konst obj = Test("OK");

  if (Test.prop != "CO") return "fail1";

  return obj.prop;
}
