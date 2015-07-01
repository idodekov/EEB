package org.redoubt.fs.util;

import java.util.UUID;

public class FileSystemUtils {
    private FileSystemUtils() {}
    
    public static String generateUniqueFileName() {
        return UUID.randomUUID().toString().toLowerCase();
    }
}
