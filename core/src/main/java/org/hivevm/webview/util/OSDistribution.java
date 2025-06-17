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

import java.util.regex.Pattern;


public enum OSDistribution {
  // @formatter:off

    // DOS
    MS_DOS     (OSFamily.DOS,     "MS-DOS",      "MSDOS",   "<manually detected>"),

    // Windows
    WINDOWS_9X (OSFamily.WINDOWS, "Windows 9x",  "MSDOS",   "windows (95|98|me|ce)"),
    WINDOWS_NT (OSFamily.WINDOWS, "Windows NT",  "Windows", "win"),

    // Unix
    MACOS      (OSFamily.UNIX,    "macOS",       "macOS",   "mac|darwin"),
    SOLARIS    (OSFamily.UNIX,    "Solaris",     "Solaris", "sun|solaris"),
    BSD        (OSFamily.UNIX,    "BSD",         "BSD",     "bsd"),
    LINUX      (OSFamily.UNIX,    "Linux",       "Linux",   "nux"),

    // VMS
    OPEN_VMS   (OSFamily.VMS,     "OpenVMS",     "VMS",     "vms"),

    /**
     * This is the fallback, this is not to be considered to be a valid value.
     */
    GENERIC    (null,    "Generic",     "Generic", ""),

    ;
    // @formatter:on

  private final OSFamily family;

  /** A friendly name for the distribution (e.g "macOS" or "Windows NT"). */
  public final String    name;

  /** A "standard" target name. */
  public final String    target;

  private final String   regex;

  /**
   * Constructs an instance of {@link OSDistribution}.
   *
   * @param family
   * @param name
   * @param target
   * @param regex
   */
  private OSDistribution(OSFamily family, String name, String target, String regex) {
    this.family = family;
    this.name = name;
    this.target = target;
    this.regex = regex;
  }

  static OSDistribution get(OSFamily family) {
    // If the OS Family is MS DOS then we can't detect it via normal means.
    // One way is to match path separator which changed in Windows 9x.
    if ((family == OSFamily.DOS) && System.getProperty("path.separator", "").equals(";")) {
      return MS_DOS;
    }

    String osName = System.getProperty("os.name", "<blank>").toLowerCase();

    // Loop through the distributions and find one that belongs to the
    // detected family and matches the regex, returning it if so.
    for (OSDistribution e : values()) {
      if (e.family != family) {
        continue;
      }

      if (Pattern.compile(e.regex).matcher(osName).find()) {
        return e;
      }
    }

    // Fallback.
    return GENERIC;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
