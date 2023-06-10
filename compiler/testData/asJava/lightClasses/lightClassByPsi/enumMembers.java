public static final class Companion /* Event.Companion*/ {
  @kotlin.jvm.JvmStatic()
  @org.jetbrains.annotations.Nullable()
  public final Event upTo(@org.jetbrains.annotations.NotNull() State);//  upTo(State)

  private  Companion();//  .ctor()
}

public static final class Companion /* State.Companion*/ {
  private  Companion();//  .ctor()

  public final boolean done(@org.jetbrains.annotations.NotNull() State);//  done(State)
}

public enum Event /* Event*/ {
  ON_CREATE,
  ON_START,
  ON_STOP,
  ON_DESTROY;

  @org.jetbrains.annotations.NotNull()
  public static final Event.Companion Companion;

  @kotlin.jvm.JvmStatic()
  @org.jetbrains.annotations.Nullable()
  public static final Event upTo(@org.jetbrains.annotations.NotNull() State);//  upTo(State)

  @org.jetbrains.annotations.NotNull()
  public static Event konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static Event[] konstues();//  konstues()

  private  Event();//  .ctor()

  class Companion ...
}

public enum State /* State*/ {
  ENQUEUED,
  RUNNING,
  SUCCEEDED,
  FAILED,
  BLOCKED,
  CANCELLED;

  @org.jetbrains.annotations.NotNull()
  public static final State.Companion Companion;

  @org.jetbrains.annotations.NotNull()
  public static State konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static State[] konstues();//  konstues()

  private  State();//  .ctor()

  public final boolean isAtLeast(@org.jetbrains.annotations.NotNull() State);//  isAtLeast(State)

  public final boolean isFinished();//  isFinished()

  class Companion ...
}
