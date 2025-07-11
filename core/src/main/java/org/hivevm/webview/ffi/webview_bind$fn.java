// Generated by jextract

package org.hivevm.webview.ffi;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

/**
 * {@snippet lang = c : * void (*fn)(const char *, const char *, void *)
 * }
 */
public class webview_bind$fn {

  webview_bind$fn() {
    // Should not be called directly
  }

  /**
   * The function pointer signature, expressed as a functional interface
   */
  public interface Function {
    void apply(MemorySegment _x0, MemorySegment _x1, MemorySegment _x2);
  }

  private static final FunctionDescriptor $DESC =
      FunctionDescriptor.ofVoid(webview_h.C_POINTER, webview_h.C_POINTER, webview_h.C_POINTER);

  /**
   * The descriptor of this function pointer
   */
  public static FunctionDescriptor descriptor() {
    return $DESC;
  }

  private static final MethodHandle UP$MH =
      webview_h.upcallHandle(webview_bind$fn.Function.class, "apply", $DESC);

  /**
   * Allocates a new upcall stub, whose implementation is defined by {@code fi}. The lifetime of the
   * returned segment is managed by {@code arena}
   */
  public static MemorySegment allocate(webview_bind$fn.Function fi, Arena arena) {
    return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
  }

  private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

  /**
   * Invoke the upcall stub {@code funcPtr}, with given parameters
   */
  public static void invoke(MemorySegment funcPtr, MemorySegment _x0, MemorySegment _x1,
      MemorySegment _x2) {
    try {
      DOWN$MH.invokeExact(funcPtr, _x0, _x1, _x2);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
}

