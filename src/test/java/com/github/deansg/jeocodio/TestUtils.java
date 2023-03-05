package com.github.deansg.jeocodio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {
    public static String readResource(String resourceName) throws IOException {
        return Files.readString(Path.of("src", "test", "resources", resourceName));
    }
}
