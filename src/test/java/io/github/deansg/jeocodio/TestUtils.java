package io.github.deansg.jeocodio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {
    public static byte[] readResource(String resourceName) throws IOException {
        return Files.readAllBytes(Path.of("src", "test", "resources", resourceName));
    }
}
