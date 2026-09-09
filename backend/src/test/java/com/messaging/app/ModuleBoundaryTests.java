package com.messaging.app;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ModuleBoundaryTests {
  @Test
  void controllersNeverImportRepositoriesAndServicesUseOnlyTheirOwnRepositories() throws Exception {
    Path root = Path.of("src/main/java/com/messaging");
    Pattern repositoryImport = Pattern.compile("import com\\.messaging\\.([a-z]+)\\.repository\\.");
    Pattern serviceImport = Pattern.compile("import com\\.messaging\\.([a-z]+)\\.service\\.");
    try (var files = Files.walk(root)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String relative = root.relativize(file).toString().replace('\\', '/');
        String module = relative.split("/")[0];
        if (relative.contains("/controller/")) {
          var serviceMatcher = serviceImport.matcher(Files.readString(file));
          while (serviceMatcher.find())
            assertEquals(
                module,
                serviceMatcher.group(1),
                relative + " calls another module's service directly");
        }
        var matcher = repositoryImport.matcher(Files.readString(file));
        while (matcher.find()) {
          assertFalse(relative.contains("/controller/"), relative + " imports a repository");
          if (relative.contains("/service/")) assertEquals(module, matcher.group(1), relative);
        }
      }
    }
  }
}
