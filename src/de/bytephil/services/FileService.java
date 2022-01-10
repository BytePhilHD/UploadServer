package de.bytephil.services;

import de.bytephil.enums.MessageType;
import de.bytephil.main.Main;
import de.bytephil.utils.Console;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileService {

    public void copyDirectoryfromResources(String resourcesName, String destinationDirectoryLocation) throws IOException, URISyntaxException {
        if (new File(destinationDirectoryLocation).exists() && !Main.getInstance().testing) {
            return;
        }
        if (Main.getInstance().testing) {
            try {
                deleteDirectory(new File("WebPages"));
            } catch (Exception e1) {
                Console.printout("Error whilst trying to delete existing folder!", MessageType.ERROR);
                return;
            }
        }
            try {
            JarFile jar = new JarFile(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
            final Enumeration<JarEntry> entries = jar.entries();
            final String newpath = resourcesName;
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(newpath) && !entry.isDirectory()) {
                    final File dest =
                            new File(destinationDirectoryLocation, entry.getName().substring(newpath.length()));
                    final File parent = dest.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    writeToFile(jar.getInputStream(entry), dest);
                }
            }
        } catch (FileNotFoundException e) { // When there is no JAR (usually in IDEs)
            URL res = getClass().getClassLoader().getResource(resourcesName);
            System.out.println("Resource " + res);
            assert res != null;
            File file = Paths.get(res.toURI()).toFile();
            String sourceDirectoryLocation = file.getAbsolutePath();
            Files.walk(Paths.get(sourceDirectoryLocation))
                    .forEach(source -> {
                        Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                                .substring(sourceDirectoryLocation.length()));
                        try {
                            Files.copy(source, destination);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    });
        }
    }
    private void writeToFile(final InputStream input, final File target)
            throws IOException {
        final OutputStream os = Files.newOutputStream(target.toPath());
        final byte[] buffer = new byte[8192];
        int length = input.read(buffer);
        while (length > 0) {
            os.write(buffer, 0, length);
            length = input.read(buffer);
        }
        input.close();
        os.close();
    }
    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

}
