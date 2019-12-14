package utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;

public class ImageUtils {

  public static double computeSimilarityRGB(BufferedImage img1, BufferedImage img2) throws IOException {
    return computeSimilarityRGB_Fastest(img1, img2);
  }

  // Naive approach
  // Example image : 7.4 seconds to process
  public static double computeSimilarityRGB_Slow(BufferedImage img1, BufferedImage img2) throws IOException {
    int width1 = img1.getWidth(null);
    int width2 = img2.getWidth(null);
    int height1 = img1.getHeight(null);
    int height2 = img2.getHeight(null);

    if ((width1 != width2) || (height1 != height2)) {
      throw new IOException("Images have different sizes");
    }

    double diff = 0;
    for (int y = 0; y < height1; y++) {
      for (int x = 0; x < width1; x++) {
        int rgb1 = img1.getRGB(x, y);
        int rgb2 = img2.getRGB(x, y);

        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = (rgb1) & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = (rgb2) & 0xff;

        double deltaR = (r2 - r1) / 255.;
        double deltaG = (g2 - g1) / 255.;
        double deltaB = (b2 - b1) / 255.;

        diff += Math.sqrt(Math.pow(deltaR, 2) + Math.pow(deltaG, 2) + Math.pow(deltaB, 2));
      }
    }

    double maxPixDiff = Math.sqrt(3); // max diff per color component is 1. so max diff on the 3 RGB component is
                                      // 1+1+1.
    double n = width1 * height1;
    double p = diff / (n * maxPixDiff);
    return p;
  }

  // Optimized version 1- Read Approach 5 :
  // http://chriskirk.blogspot.fr/2011/01/performance-comparison-of-java2d-image.html
  // Example image : 3.7 seconds to process
  public static double computeSimilarityRGB_Fast(BufferedImage img1, BufferedImage img2) throws IOException {
    int width1 = img1.getWidth(null);
    int width2 = img2.getWidth(null);
    int height1 = img1.getHeight(null);
    int height2 = img2.getHeight(null);

    if ((width1 != width2) || (height1 != height2)) {
      throw new IOException("Images have different sizes");
    }

    final WritableRaster raster1 = img1.getRaster();
    final WritableRaster raster2 = img2.getRaster();
    int[] pixels1 = new int[3 * width1];
    int[] pixels2 = new int[3 * width1];

    double diff = 0;
    for (int y = 0; y < height1; ++y) {
      pixels1 = raster1.getPixels(0, y, width1, 1, pixels1);
      pixels2 = raster2.getPixels(0, y, width1, 1, pixels2);

      for (int x = 0; x < width1; ++x) {
        int m = x * 3;

        double deltaR = (pixels2[m + 0] - pixels1[m + 0]) / 255.;
        double deltaG = (pixels2[m + 1] - pixels1[m + 1]) / 255.;
        double deltaB = (pixels2[m + 2] - pixels1[m + 2]) / 255.;

        diff += Math.sqrt(Math.pow(deltaR, 2) + Math.pow(deltaG, 2) + Math.pow(deltaB, 2));
      }
    }

    double maxPixDiff = Math.sqrt(3); // max diff per color component is 1. so max diff on the 3 RGB component is
                                      // 1+1+1.
    double n = width1 * height1;
    double p = diff / (n * maxPixDiff);
    return p;
  }

  // Optimized - Read Approach 8 :
  // http://chriskirk.blogspot.fr/2011/01/performance-comparison-of-java2d-image.html
  // And some personal optimization
  // Example image : 1.2 seconds to process
  public static double computeSimilarityRGB_Fastest(BufferedImage img1, BufferedImage img2) throws IOException {
    int width1 = img1.getWidth(null);
    int width2 = img2.getWidth(null);
    int height1 = img1.getHeight(null);
    int height2 = img2.getHeight(null);

    if ((width1 != width2) || (height1 != height2)) {
      throw new IOException("Images have different sizes");
    }

    DataBuffer db1 = img1.getRaster().getDataBuffer();
    DataBuffer db2 = img2.getRaster().getDataBuffer();

    double diff = 0;
    int size = db1.getSize(); // size = width * height * 3
    double p = 0;

    // TODO: jpeg format v9 can use 12bit per channel, see:
    // http://www.tomshardware.fr/articles/jpeg-lossless-12bit,1-46742.html

    if (size == (width1 * height1 * 3)) { // RGB 24bit per pixel - 3 bytes per pixel: 1 for R, 1 for G, 1 for B

      for (int i = 0; i < size; i += 3) {
        /*
         * double deltaR = (db2.getElem(i) - db1.getElem(i)) / 255.; double deltaG =
         * (db2.getElem(i+1) - db1.getElem(i+1)) / 255.; double deltaB =
         * (db2.getElem(i+2) - db1.getElem(i+2)) / 255.;
         * 
         * diff += Math.sqrt(Math.pow(deltaR, 2) + Math.pow(deltaG, 2) +
         * Math.pow(deltaB, 2));
         */

        double deltaR = (db2.getElem(i) - db1.getElem(i));
        double deltaG = (db2.getElem(i + 1) - db1.getElem(i + 1));
        double deltaB = (db2.getElem(i + 2) - db1.getElem(i + 2));

        diff += Math.sqrt(((deltaR * deltaR) + (deltaG * deltaG) + (deltaB * deltaB)) / 65025.);
      }

      double maxPixDiff = Math.sqrt(3); // max diff per color component is 1. So max diff on the 3 RGB component is
                                        // 1+1+1.
      double n = width1 * height1;
      p = diff / (n * maxPixDiff);

    } else if (size == (width1 * height1)) { // Gray 8bit per pixel - Don't know if it's possible in jpeg, but just in
                                             // case, code it! :)

      for (int i = 0; i < size; ++i) {
        diff += (db2.getElem(i) - db1.getElem(i)) / 255;
      }
      p = diff / size;
    }

    return p;
  }

  // JPEG Copy input image to output image with the new quality, and copy too the
  // EXIF data from input to output!
  public static void createJPEG(File input, File output, int quality) throws IOException {
    FileUtils.truncateFile(output);

    try (ImageInputStream iis = ImageIO.createImageInputStream(input);) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
      ImageReader reader = (ImageReader) readers.next();
      reader.setInput(iis, false);
      IIOMetadata metadata = reader.getImageMetadata(0);
      BufferedImage bi = reader.read(0);

      final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
      try (FileImageOutputStream fios = new FileImageOutputStream(output)) {

        writer.setOutput(fios);

        ImageWriteParam iwParam = writer.getDefaultWriteParam();
        iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwParam.setCompressionQuality(quality / 100f);
        if (iwParam instanceof JPEGImageWriteParam) {
        	JPEGImageWriteParam jpegImageWriteParam = (JPEGImageWriteParam) iwParam;
        	jpegImageWriteParam.setOptimizeHuffmanTables(true);
        }

        writer.write(null, new IIOImage(bi, null, metadata), iwParam);
        writer.dispose();

        reader.dispose();
      }
    }
    writeQualityInJPEG(output, quality);
  }

  // Save the input image as a jpeg file
  public static void createJPEG(BufferedImage input, File output, int quality) throws IOException {
    FileUtils.truncateFile(output);

    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
    try (FileImageOutputStream fios = new FileImageOutputStream(output)) {
      writer.setOutput(fios);
      ImageWriteParam iwParam = writer.getDefaultWriteParam();
      iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      iwParam.setCompressionQuality(quality / 100f);

      writer.write(null, new IIOImage(input, null, null), iwParam);
      writer.dispose();
    }
    writeQualityInJPEG(output, quality);
  }

  // Special trick to read the quality in the last byte of the file (because
  // JFIF/EXIF do not have this info)
  public static int readQualityInJPEG(File input) throws IOException {
    if ((input == null) || (input.exists() == false) || (input.canRead() == false)) {
      return -1;
    } else {
      FileInputStream in = new FileInputStream(input);
      int quality = -1;
      try {
        in.getChannel().position(in.getChannel().size() - 2);
        int b1 = in.read();
        int b2 = in.read();
        if ((b1 == 0xFF) && (b2 == 0xD9)) { // 0xFFD9 it's the EOI (End Of Image jpeg tag), meaning JPEGOptimized does
                                            // not append the quality byte
          quality = -1;
        } else {
          quality = b2;
        }
      } finally {
        in.close();
      }
      return quality;
    }
  }

  // Special trick to write the quality in the last byte of the file (because
  // JFIF/EXIF do not have this info)
  private static void writeQualityInJPEG(File output, int quality) throws IOException {
    try (FileOutputStream out = new FileOutputStream(output, true)) {
      out.write(quality & 0x7F);
    }
  }

}
