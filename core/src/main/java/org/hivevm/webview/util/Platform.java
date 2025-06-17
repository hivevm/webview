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

import java.io.IOException;
import java.nio.ByteOrder;
import org.jetbrains.annotations.NotNull;


public class Platform {

  /* ---------------- */
  /* CPU Architecture */
  /* ---------------- */

  /** Whether or not the current machine's endianess is big endian. */
  public static final boolean        isBigEndian    =
      ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

  /** The processor's word size/bitness, or -1 if unknown. Usually 32 or 64. */
  public static final int            wordSize       = PlatformUtil.getWordSize();

  /** The CPU Architecture of the host, e.g x86 or arm. */
  public static final ArchFamily     archFamily     = ArchFamily.get();

  /** The CPU Target of the host, e.g x86_64 or aarch64. */
  public static final String         archTarget     =
      archFamily.getArchTarget(wordSize, isBigEndian);

  /* ---------------- */
  /* Operating System */
  /* ---------------- */

  /** The family of the host's OS, e.g macOS or Windows NT */
  public static final OSFamily       osFamily       = OSFamily.get();

  /** The family distribution of the host's OS, e.g Unix or Windows */
  public static final OSDistribution osDistribution = OSDistribution.get(osFamily);

  /* ---------------- */
  /* Helpers */
  /* ---------------- */

  /**
   * A convenience method for generating file names for OS-specific library files.
   *
   * @param libraryName The name of the library (e.g "WebView")
   *
   * @return the formatted string (e.g "libwebview.so" or "WebView.dll")
   *
   * @apiNote &bull; This returns "*.dylib" on macOS, since that's the more common format; Be aware
   *          that macOS supports both .so and .dylib extensions for libraries.
   *
   * @apiNote &bull; This returns "*.dll" on Windows, since that's the more common format; Be aware
   *          that Windows supports both .exe and .dll extensions for libraries.
   */
  public static String formatLibrary(@NotNull String libraryName) {
    switch (osDistribution) {
      // DOS
      case MS_DOS:
        return String.format("%s.exe", libraryName).toUpperCase();

      // Windows
      case WINDOWS_9X:
      case WINDOWS_NT:
        return String.format("%s.dll", libraryName);

      // Unix
      case MACOS:
        return String.format("%s.dylib", libraryName).toLowerCase();

      case BSD:
      case SOLARIS:
      case LINUX:
        return String.format("%s.so", libraryName).toLowerCase();

      // VMS
      case OPEN_VMS:
        return String.format("%s.exe", libraryName).toUpperCase();

      case GENERIC:
        break;

      // Don't create a `default:` entry.
      // We want the compiler to warn us about missed values.

    }
    return libraryName;
  }

  /** If this returns true then you know that this OS supports GNU LibC. It may also support MUSL or other standards. */
  public static boolean isGNU() throws IOException {
    return LinuxLibC.isGNU();
  }
}
