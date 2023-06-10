public final class C /* C*/ {
  @org.jetbrains.annotations.Nullable()
  private final Direction enumConst;

  @org.jetbrains.annotations.Nullable()
  public final Direction getEnumConst();//  getEnumConst()

  public  C();//  .ctor()
}

public enum Color /* Color*/ {
  RED,
  GREEN,
  BLUE;

  private final int rgb;

  @org.jetbrains.annotations.NotNull()
  public static Color konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static Color[] konstues();//  konstues()

  @org.jetbrains.annotations.NotNull()
  public static kotlin.enums.EnumEntries<Color> getEntries();//  getEntries()

  private  Color(int);//  .ctor(int)

  private  Color(java.lang.String);//  .ctor(java.lang.String)

  public final int getRgb();//  getRgb()
}

public enum Direction /* Direction*/ {
  @Some() NORTH,
  SOUTH,
  WEST,
  EAST;

  @org.jetbrains.annotations.NotNull()
  public static Direction konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static Direction[] konstues();//  konstues()

  @org.jetbrains.annotations.NotNull()
  public static kotlin.enums.EnumEntries<Direction> getEntries();//  getEntries()

  private  Direction();//  .ctor()
}

public abstract enum IntArithmetics /* IntArithmetics*/ implements java.util.function.BinaryOperator<java.lang.Integer>, java.util.function.IntBinaryOperator {
  PLUS,
  TIMES;

  @org.jetbrains.annotations.NotNull()
  public static IntArithmetics konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static IntArithmetics[] konstues();//  konstues()

  @org.jetbrains.annotations.NotNull()
  public static kotlin.enums.EnumEntries<IntArithmetics> getEntries();//  getEntries()

  private  IntArithmetics();//  .ctor()

  public int applyAsInt(int, int);//  applyAsInt(int, int)

  class PLUS ...

  class TIMES ...
}

public abstract enum ProtocolState /* ProtocolState*/ {
  WAITING,
  TALKING;

  @org.jetbrains.annotations.NotNull()
  public abstract ProtocolState signal();//  signal()

  @org.jetbrains.annotations.NotNull()
  public static ProtocolState konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static ProtocolState[] konstues();//  konstues()

  @org.jetbrains.annotations.NotNull()
  public static kotlin.enums.EnumEntries<ProtocolState> getEntries();//  getEntries()

  private  ProtocolState();//  .ctor()

  class TALKING ...

  class WAITING ...
}

@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
public abstract @interface Some /* Some*/ {
}
