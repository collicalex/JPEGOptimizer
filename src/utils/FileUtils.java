package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

	public static void copyFile(File source, File dest) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel sourceChannel = null;
		FileChannel destChannel = null;
		try {
			fis = new FileInputStream(source);
			fos = new FileOutputStream(dest);
			sourceChannel = fis.getChannel();
			destChannel = fos.getChannel();
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		} finally {
			sourceChannel.close();
			destChannel.close();
			fis.close();
			fos.close();
		}
	}
	
	//Truncate the given file
	public static void truncateFile(File file) throws IOException {
		if (file.exists()) {
			FileOutputStream fos = new FileOutputStream(file, true);
			FileChannel outChan = fos.getChannel();
		    outChan.truncate(0);
		    outChan.close();
		    fos.close();
		}
	}
	
}
