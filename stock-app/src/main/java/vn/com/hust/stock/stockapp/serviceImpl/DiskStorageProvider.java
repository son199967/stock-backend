package vn.com.hust.stock.stockapp.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.com.hust.stock.stockapp.service.StorageProvider;
import vn.com.hust.stock.stockmodel.exception.DataStoreException;
import vn.com.hust.stock.stockmodel.exception.LocationNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Component
public class DiskStorageProvider implements StorageProvider {

    private static final long INITIAL_SUBFOLDER_INDEX = 1;
    private final String rootFolder;
    private final long maxFilesCount;

    public DiskStorageProvider(@Value("${files.folder}") String rootFolder, @Value("${files.folder.max:10000}") long maxFilesCount) throws IOException {
        this.rootFolder = rootFolder;
        this.maxFilesCount = maxFilesCount;
        Path filesFolder = Paths.get(rootFolder);
        if (!Files.exists(filesFolder)) {
            Files.createDirectories(filesFolder);
        }
    }

    @Override
    public void store(String fileName, byte[] content) {
        try {
            Path locationPath = Paths.get(getLocation());
            Files.createDirectories(locationPath);
            Files.write(locationPath.resolve(fileName), content);
        } catch (IOException | LocationNotFoundException e) {
            log.error("Could not store file", e);
            throw new DataStoreException(e.getMessage());
        }
    }

    @Override
    public String getLocation() {
        try {
            long foldersCount = countFiles(rootFolder);
            String currentFolderName = String.valueOf(foldersCount);
            long filesCountWithinFolder = countFiles(rootFolder + File.separator + currentFolderName);
            return rootFolder + File.separator +
                    (filesCountWithinFolder > maxFilesCount ?
                            String.valueOf(foldersCount + 1) :
                            currentFolderName);
        } catch (IOException e) {
            throw new LocationNotFoundException();
        }
    }

    private long countFiles(String folderPath) throws IOException {
        Path path = Paths.get(folderPath);
        if (Files.exists(path) && Files.isDirectory(path)) {
            try (Stream<Path> files = Files.list(path)) {
                return files.count();
            }
        } else {
            return INITIAL_SUBFOLDER_INDEX;
        }
    }
}
