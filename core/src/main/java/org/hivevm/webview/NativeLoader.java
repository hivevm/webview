// Copyright (C) 2025 HiveVM.org
// SPDX-License-Identifier: MIT License

package org.hivevm.webview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.hivevm.webview.util.Platform;

/**
 * The {@link NativeLoader} loads the native libraries from the hosting OS.
 */
public class NativeLoader {

  private static String[] getLibraries() throws IOException {
    return switch (Platform.osDistribution) {
      case LINUX -> new String[] {String.format("/natives/%s/linux/%s/libwebview.so",
          Platform.archTarget, Platform.isGNU() ? "gnu" : "musl")};
      case MACOS -> new String[] {"/natives/" + Platform.archTarget + "/macos/libwebview.dylib"};
      case WINDOWS_NT -> new String[] {
          "/natives/" + Platform.archTarget + "/windows_nt/webview2loader.dll",
          "/natives/" + Platform.archTarget + "/windows_nt/webview.dll"};
      default -> null;
    };
  }

  public static void load() throws IOException {
    String[] libraries = NativeLoader.getLibraries();

    if (libraries == null) {
      throw new IllegalStateException(
          "Unsupported platform: " + Platform.osDistribution + ":" + Platform.archTarget);
    }

    // Extract all of the libraries.
    var userDir = new File(System.getProperty("user.dir"));
    for (String lib : libraries) {
      File target = new File(userDir, new File(lib).getName());
      if (target.exists())
        target.delete();
      target.deleteOnExit();

      // Copy it to a file.
      try (InputStream in = NativeLoader.class.getResourceAsStream(lib.toLowerCase())) {
        byte[] bytes = in.readAllBytes();
        Files.write(target.toPath(), bytes);
      } catch (Exception e) {
        if (e.getMessage().contains("used by another"))
          continue; // Ignore.

        System.err.println("Unable to extract native: " + lib + " to: " + target);
        throw e;
      }

      // Load it. This is so Native will be able to link it.
      System.load(target.getAbsolutePath());
    }
  }
}
