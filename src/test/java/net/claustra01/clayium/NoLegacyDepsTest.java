package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NoLegacyDepsTest {
    private static final List<String> FORBIDDEN_TOKENS = List.of(
            "cofh.",
            "invtweaks.",
            "codechicken.",
            "ic2.",
            "minetweaker.",
            "gregtech.",
            "cpw.mods.fml",
            "net.minecraftforge.common.util.ForgeDirection"
    );

    @Test
    void mainSourcesContainNoRemovedLegacyDependencies() throws IOException {
        Path projectDir = Path.of(System.getProperty("clayium.projectDir", "."));
        Path mainJava = projectDir.resolve("src/main/java");
        assertTrue(Files.isDirectory(mainJava), "Expected src/main/java directory at " + mainJava.toAbsolutePath());
        try (Stream<Path> files = Files.walk(mainJava)) {
            List<Path> javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
            assertTrue(!javaFiles.isEmpty(), "Expected Java source files under src/main/java");
            for (Path file : javaFiles) {
                String content = Files.readString(file);
                for (String token : FORBIDDEN_TOKENS) {
                    assertTrue(
                            !content.contains(token),
                            "Forbidden legacy reference '" + token + "' in " + file
                    );
                }
            }
        }
    }
}
