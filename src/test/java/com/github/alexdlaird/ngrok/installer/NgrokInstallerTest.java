package com.github.alexdlaird.ngrok.installer;

import com.github.alexdlaird.ngrok.NgrokTestCase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NgrokInstallerTest extends NgrokTestCase {

    @Test
    public void testInstallNgrok() throws IOException, InterruptedException {
        Thread.sleep(1000);

        // GIVEN
        if (Files.exists(javaNgrokConfig.getNgrokPath())) {
            Files.delete(javaNgrokConfig.getNgrokPath());
        }
        assertFalse(Files.exists(javaNgrokConfig.getNgrokPath()));

        // WHEN
        ngrokInstaller.installNgrok(javaNgrokConfig.getNgrokPath());

        // THEN
        assertTrue(Files.exists(javaNgrokConfig.getNgrokPath()));
    }

    @Test
    public void testInstallDefaultConfig() throws IOException {
        // GIVEN
        if (Files.exists(javaNgrokConfig.getConfigPath())) {
            Files.delete(javaNgrokConfig.getConfigPath());
        }
        assertFalse(Files.exists(javaNgrokConfig.getConfigPath()));

        // WHEN
        ngrokInstaller.installDefaultConfig(javaNgrokConfig.getConfigPath(), Collections.emptyMap());

        // THEN
        assertTrue(Files.exists(javaNgrokConfig.getConfigPath()));
    }
}
