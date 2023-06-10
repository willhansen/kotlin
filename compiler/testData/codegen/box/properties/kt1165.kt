public abstract class VirtualFile() {
    public abstract konst size : Long
}

public class PhysicalVirtualFile : VirtualFile() {
    public override konst size: Long
    get() = 11
}

fun box() : String {
    PhysicalVirtualFile()
    return "OK"
}
