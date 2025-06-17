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

public enum OSFamily {
  // @formatter:off

  UNIX     ("Unix",    "nux|bsd|.ix|sun|solaris|hp-ux|mac|darwin"),
  WINDOWS  ("Windows", "win"),
  DOS      ("DOS",     "dos"),
  VMS      ("VMS",     "vms"),

  GENERIC  ("Generic", ""),

  ;
  // @formatter:on

  /** A friendly name for the family (e.g "Unix" or "Windows"). */
  public final String  name;
  private final String regex;

  private OSFamily(String name, String regex) {
    this.name = name;
    this.regex = regex;
  }

  static OSFamily get() {
    String osName = System.getProperty("os.name", "<blank>").toLowerCase();

    // Search the enums for a match, returning it.
    for (OSFamily e : values()) {
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
