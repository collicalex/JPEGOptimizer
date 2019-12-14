package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

  public static void copyFile(File source, File dest) throws IOException {
    try (FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);
        FileChannel sourceChannel = fis.getChannel();
        FileChannel destChannel = fos.getChannel();) {
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
    }
  }

  // Truncate the given file
  public static void truncateFile(File file) throws IOException {
    if (file.exists()) {
      try (FileOutputStream fos = new FileOutputStream(file, true); FileChannel outChan = fos.getChannel();) {
        outChan.truncate(0);
      }
    }
  }

}
