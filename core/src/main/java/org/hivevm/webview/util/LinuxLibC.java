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
package org.hivevm.webview.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** This class allows you to detect whether or not a machine uses GNU or MUSL Libc. */
// Code adapted from here:
// https://github.com/lovell/detect-libc/blob/main/lib/detect-libc.js
class LinuxLibC {

  /**
   * If this returns true then you know that this OS supports GNU LibC. It may also support MUSL or
   * other standards.
   */
  public static boolean isGNU() throws IOException {
    if (Platform.osDistribution != OSDistribution.LINUX)
      throw new IllegalStateException("LinuxLibC is only supported on Linux.");

    if ("true".equalsIgnoreCase(System.getProperty("casterlabs.commons.forcegnu")))
      return true;

    try {
      return isGNUViaFS();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      return isGNUViaCommand();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  private static boolean isGNUViaFS() throws IOException {
    try (FileInputStream fin = new FileInputStream(new File("/usr/bin/ldd"))) {
      String ldd = PlatformUtil.readInputStreamString(fin, StandardCharsets.UTF_8);
      return ldd.contains("GNU C Library");
    }
  }

  private static boolean isGNUViaCommand() throws IOException {
    Process unameProc = Runtime.getRuntime()
        .exec("sh -c 'getconf GNU_LIBC_VERSION 2>&1 || true; ldd --version 2>&1 || true'");
    String unameResult =
        PlatformUtil.readInputStreamString(unameProc.getInputStream(), StandardCharsets.UTF_8);
    return unameResult.contains("glibc");
  }
}
