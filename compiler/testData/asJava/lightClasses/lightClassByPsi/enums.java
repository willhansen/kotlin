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

  private  Color(@org.jetbrains.annotations.NotNull() java.lang.String);//  .ctor(java.lang.String)

  private  Color(int);//  .ctor(int)

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

  private  Direction();//  .ctor()
}

public enum IntArithmetics /* IntArithmetics*/ implements java.util.function.BinaryOperator<java.lang.Integer>, java.util.function.IntBinaryOperator {
  PLUS {
   PLUS();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public java.lang.Integer apply(int, int);//  apply(int, int)
  },
  TIMES {
   TIMES();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public java.lang.Integer apply(int, int);//  apply(int, int)
  };

  @org.jetbrains.annotations.NotNull()
  public static IntArithmetics konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static IntArithmetics[] konstues();//  konstues()

  private  IntArithmetics();//  .ctor()

  public int applyAsInt(int, int);//  applyAsInt(int, int)

  class PLUS ...

  class TIMES ...
}

public static final class PLUS /* IntArithmetics.PLUS*/ extends IntArithmetics {
   PLUS();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public java.lang.Integer apply(int, int);//  apply(int, int)
}

public enum ProtocolState /* ProtocolState*/ {
  WAITING {
   WAITING();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public ProtocolState signal();//  signal()
  },
  TALKING {
   TALKING();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public ProtocolState signal();//  signal()
  };

  @org.jetbrains.annotations.NotNull()
  public abstract ProtocolState signal();//  signal()

  @org.jetbrains.annotations.NotNull()
  public static ProtocolState konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static ProtocolState[] konstues();//  konstues()

  private  ProtocolState();//  .ctor()

  class TALKING ...

  class WAITING ...
}

@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
public abstract @interface Some /* Some*/ {
}

public static final class TALKING /* ProtocolState.TALKING*/ extends ProtocolState {
   TALKING();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public ProtocolState signal();//  signal()
}

public static final class TIMES /* IntArithmetics.TIMES*/ extends IntArithmetics {
   TIMES();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public java.lang.Integer apply(int, int);//  apply(int, int)
}

public static final class WAITING /* ProtocolState.WAITING*/ extends ProtocolState {
   WAITING();//  .ctor()

  @org.jetbrains.annotations.NotNull()
  public ProtocolState signal();//  signal()
}
