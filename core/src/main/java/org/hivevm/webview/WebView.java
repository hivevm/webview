/**
 * MIT License
 * 
 * Copyright (c) 2025 HiveVM
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.hivevm.webview;

import static org.hivevm.webview.ffi.webview_h.WEBVIEW_ERROR_OK;
import static org.hivevm.webview.ffi.webview_h.WEBVIEW_ERROR_UNSPECIFIED;
import static org.hivevm.webview.ffi.webview_h.WEBVIEW_HINT_FIXED;
import static org.hivevm.webview.ffi.webview_h.WEBVIEW_HINT_MAX;
import static org.hivevm.webview.ffi.webview_h.WEBVIEW_HINT_MIN;
import static org.hivevm.webview.ffi.webview_h.WEBVIEW_HINT_NONE;
import static org.hivevm.webview.ffi.webview_h.webview_bind;
import static org.hivevm.webview.ffi.webview_h.webview_create;
import static org.hivevm.webview.ffi.webview_h.webview_destroy;
import static org.hivevm.webview.ffi.webview_h.webview_eval;
import static org.hivevm.webview.ffi.webview_h.webview_init;
import static org.hivevm.webview.ffi.webview_h.webview_navigate;
import static org.hivevm.webview.ffi.webview_h.webview_return;
import static org.hivevm.webview.ffi.webview_h.webview_run;
import static org.hivevm.webview.ffi.webview_h.webview_set_html;
import static org.hivevm.webview.ffi.webview_h.webview_set_size;
import static org.hivevm.webview.ffi.webview_h.webview_set_title;
import static org.hivevm.webview.ffi.webview_h.webview_terminate;
import static org.hivevm.webview.ffi.webview_h.webview_unbind;
import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebView implements Closeable, Runnable {

  private final static Linker LINKER = Linker.nativeLinker();

  public Arena                arena;
  public MemorySegment        handle;

  /**
   * Creates a new Webview. The default size will be set, and if the size is set again before
   * loading the URL, a splash will appear.<br/>
   * eg:
   *
   * <pre>
   * <code>
   *   WebView wv = new WebView(true);
   *   wv.setSize(1280, 720);
   *   wv.loadURL("...")
   * </code>
   * </pre>
   *
   * It's recommended that setting size together:
   * 
   * <pre>
   * <code>
   *   WebView wv = new WebView(true, 1280, 720);
   *   wv.loadURL("...")
   * </code>
   * </pre>
   *
   * @param debug Enables devtools/inspect element if true.
   */
  public WebView(boolean debug) {
    this(debug, 800, 600);
  }

  /**
   * Creates a new Webview.
   */
  public WebView(boolean debug, int width, int height) {
    try {
      NativeLoader.load();
    } catch (Throwable e) {
      e.printStackTrace();
      new RuntimeException(e);
    }

    arena = Arena.ofConfined();
    handle = webview_create(1, MemorySegment.NULL);

    loadURL(null);
    setSize(width, height);
  }

  public void setHTML(@Nullable String html) {
    webview_set_html(handle, arena.allocateFrom(html));
  }

  public void loadURL(@Nullable String url) {
    if (url == null) {
      url = "about:blank";
    }

    webview_navigate(handle, arena.allocateFrom(url));
  }

  public void setTitle(@NotNull String title) {
    webview_set_title(handle, title == null ? MemorySegment.NULL : arena.allocateFrom(title));
  }

  public void setMinSize(int width, int height) {
    webview_set_size(handle, width, height, WEBVIEW_HINT_MIN());
  }

  public void setMaxSize(int width, int height) {
    webview_set_size(handle, width, height, WEBVIEW_HINT_MAX());
  }

  public void setSize(int width, int height) {
    webview_set_size(handle, width, height, WEBVIEW_HINT_NONE());
  }

  public void setFixedSize(int width, int height) {
    webview_set_size(handle, width, height, WEBVIEW_HINT_FIXED());
  }

  /**
   * Sets the script to be run on page load. Defaults to no nested access (false).
   */
  public void setInitScript(@NotNull String script) {
    this.setInitScript(script, false);
  }

  /**
   * Sets the script to be run on page load. This get's called AFTER window.load. The
   * allowNestedAccess defines whether or not to inject the script into nested iframes.
   */
  public void setInitScript(@NotNull String script, boolean allowNestedAccess) {
    script = String.format("""
        (() => {
          try {
            if (window.top == window.self || %b) {
              %s
            }
          } catch (e) {
            console.error('[Webview]', 'An error occurred whilst evaluating init script:', %s, e);
          }
        """, allowNestedAccess, script, '"' + WebViewUtil.jsonEscape(script) + '"');

    webview_init(handle, arena.allocateFrom(script));
  }

  /**
   * Executes the given script NOW.
   */
  public void eval(@NotNull String script) {
    this.dispatch(() -> {
      var eval = String.format("""
          try {
            %s
          } catch (e) {
            console.error('[Webview]', 'An error occurred whilst evaluating script:', %s, e);
          }
          """, script, '"' + WebViewUtil.jsonEscape(script) + '"');

      webview_eval(handle, arena.allocateFrom(eval));
    });
  }

  /**
   * Binds a function to the JavaScript environment on page load. After calling the function in
   * JavaScript you will get a Promise instead of the value. This is to prevent you from locking up
   * the browser while waiting on your Java code to execute and generate a return value.
   * 
   * The callback handler, accepts a JsonArray (which are all arguments passed to the function())
   * and returns a value which is of type JsonElement (can be null). Exceptions are automatically
   * passed back to JavaScript.
   */
  public void bind(@NotNull String name, @NotNull WebViewBindCallback handler) {
    try {
    } catch (Exception e) {
      e.printStackTrace();
    }

    BindCallback callback = (seq, req, arg) -> {
      int result = WEBVIEW_ERROR_OK();
      String response;
      try {
        long seqL = seq.get(ValueLayout.JAVA_LONG, 0);
        long argL = arg.get(ValueLayout.JAVA_LONG, 0);
        String request = WebViewUtil.forceSafeChars(req.getString(0));
        response = handler.apply(request);
        if (response == null)
          response = "null";

        response = WebViewUtil.forceSafeChars(response);
      } catch (Throwable e) {
        e.printStackTrace();

        result = WEBVIEW_ERROR_UNSPECIFIED();
        response = '"' + WebViewUtil.jsonEscape(WebViewUtil.getExceptionStack(e)) + '"';
      }

      webview_return(handle, seq, result, arena.allocateFrom(response));
    };

    try {
      var methodType = MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class,
          MemorySegment.class);
      var handle = MethodHandles.lookup().findVirtual(BindCallback.class, "callback", methodType);
      handle = handle.bindTo(callback);
      var func =
          FunctionDescriptor.ofVoid(ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_LONG),
              ValueLayout.ADDRESS
                  .withTargetLayout(MemoryLayout.sequenceLayout(10, ValueLayout.JAVA_CHAR)),
              ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_LONG));
      var upcall = LINKER.upcallStub(handle, func, arena);

      webview_bind(this.handle, arena.allocateFrom(name), upcall, arena.allocate(0));

    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * Unbinds a function, removing it from future pages.
   */
  public void unbind(@NotNull String name) {
    webview_unbind(handle, arena.allocateFrom(name));
  }

  /** Executes an event on the event thread. */
  @Deprecated
  public void dispatch(@NotNull Runnable handler) {
    // N.webview_dispatch($pointer, ($pointer, arg) -> {
    // handler.run();
    // }, 0);
  }

  /**
   * Executes the webview event loop until the user presses "X" on the window.
   */
  public void run() {
    webview_run(handle);
    webview_destroy(handle);
  }

  /** Executes the webview event loop asynchronously until the user presses "X" on the window. */
  public void runAsync() {
    Thread t = new Thread(this);
    t.setDaemon(false);
    t.setName("Webview RunAsync Thread - #" + this.hashCode());
    t.start();
  }

  /** Closes the webview, call this to end the event loop and free up resources. */
  public void close() {
    webview_terminate(handle);
    arena.close();
    handle = null;
    arena = null;
  }

  @FunctionalInterface
  private interface BindCallback {
    /**
     * @param seq The request id, used in {@link webview_return}
     * @param req The javascript arguments converted to a json array (string)
     * @param arg Unused
     */
    void callback(MemorySegment seq, MemorySegment req, MemorySegment arg);
  }
}
