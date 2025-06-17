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

enum ArchFamily {
  // @formatter:off
    X86   ("x86",   false, "x86|i[0-9]86|ia32|amd64|ia64|itanium64"),
    ARM   ("arm",   false, "arm|aarch"),
    PPC   ("ppc",   true,  "ppc|power"),
    SPARC ("sparc", true,  "sparc"),
    MIPS  ("mips",  true,  "mips"),
    S390  ("s390",  true,  "s390"),
    RISCV ("riscv", false, "riscv"),
    ;
    // @formatter:on

  private final String name;
  public final boolean isUsuallyBigEndian;
  private final String regex;

  /**
   * Constructs an instance of {@link ArchFamily}.
   *
   * @param name
   * @param isUsuallyBigEndian
   * @param regex
   */
  private ArchFamily(String name, boolean isUsuallyBigEndian, String regex) {
    this.name = name;
    this.isUsuallyBigEndian = isUsuallyBigEndian;
    this.regex = regex;
  }

  static ArchFamily get() {
    String osArch = System.getProperty("os.arch", "<blank>").toLowerCase();

    // Search the enums for a match, returning it.
    for (ArchFamily arch : values()) {
      if (Pattern.compile(arch.regex).matcher(osArch).find()) {
        return arch;
      }
    }

    // Couldn't find a match.
    throw new UnsupportedOperationException("Unknown cpu arch: " + osArch);
  }

  @Override
  public String toString() {
    return this.name;
  }

  /**
   * @param wordSize The word size, usually 32 or 64.
   */
  public String getArchTarget(int wordSize) {
    return getArchTarget(wordSize, this.isUsuallyBigEndian);
  }

  /**
   * @param wordSize The word size, usually 32 or 64.
   * @param bigEndian Whether or not the processor is bigEndian or littleEndian. Some CPUs don't
   *        support this so this will be silently ignored.
   */
  public String getArchTarget(int wordSize, boolean isBigEndian) {
    // https://github.com/llvm/llvm-project/blob/main/llvm/include/llvm/TargetParser/Triple.h
    switch (this) {
      case ARM:
        return wordSize == 64 ? //
            (isBigEndian ? "aarch64_be" : "aarch64") : //
            (isBigEndian ? "armeb" : "arm");

      case MIPS:
        return wordSize == 64 ? //
            (isBigEndian ? "mips64" : "mips64el") : //
            (isBigEndian ? "mips" : "mipsel");

      case PPC:
        return wordSize == 64 ? //
            (isBigEndian ? "ppc64" : "ppc64le") : //
            (isBigEndian ? "ppc" : "ppcle");

      case RISCV:
        return wordSize == 64 ? "riscv64" : "riscv32";

      case S390:
        return "systemz"; // TODO LLVM appears to not have a s390x variant?

      case SPARC:
        return wordSize == 64 ? //
            ("sparcv9") : //
            (isBigEndian ? "sparc" : "sparcel");

      case X86:
        return wordSize == 64 ? "x86_64" : "x86";

      // Don't create a `default:` entry.
      // We want the compiler to warn us about missed values.
    }

    throw new RuntimeException("Unable to figure out LLVM for arch: " + Platform.archFamily);
  }

}
