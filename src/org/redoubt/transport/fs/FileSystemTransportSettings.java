package org.redoubt.transport.fs;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.redoubt.api.transport.ITransportSettings;
import org.redoubt.transport.SettingsHolder;

public class FileSystemTransportSettings extends SettingsHolder implements ITransportSettings {
    private static final long serialVersionUID = -6133544684476689296L;
    public static final String TRANSPORT_NAME = "file_system";

    @Override
    public String getTransportName() {
        return TRANSPORT_NAME;
    }
    
    public String getName() {
        return (String) get(FileSystemSettingsKeyring.NAME);
    }

    public void setName(String name) {
        put(FileSystemSettingsKeyring.NAME, name);
    }
    
    public Path getFolder() {
        return Paths.get((String) get(FileSystemSettingsKeyring.FOLDER));
    }
    
    public void setFolder(Path folder) {
        put(FileSystemSettingsKeyring.FOLDER, folder);
    }
    
    public int getPollingInterval() {
        return Integer.parseInt((String) get(FileSystemSettingsKeyring.POLLING_INTERVAL));
    }
    
    public void setPollingInterval(int pollingInterval) {
        put(FileSystemSettingsKeyring.POLLING_INTERVAL, pollingInterval);
    }
    
    public class FileSystemSettingsKeyring {
        public static final String NAME = "name";
        public static final String FOLDER = "folder";
        public static final String POLLING_INTERVAL = "pollingInterval";
    }

}
