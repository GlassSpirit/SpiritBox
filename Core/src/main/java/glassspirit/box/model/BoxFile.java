package glassspirit.box.model;


import java.io.File;

public class BoxFile {

    private String fileName;
    private long fileSize;

    private File originalFile;

    public BoxFile(File file) {
        this.originalFile = file;
        this.fileName = file.getName();
        this.fileSize = file.length();
    }

    public BoxFile(String name, long size) {
        this.fileName = name;
        this.fileSize = size;

        this.originalFile = null;
    }

    public File getFile() {
        return originalFile;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}
