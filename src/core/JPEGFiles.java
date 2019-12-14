package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import utils.FileUtils;
import utils.ImageUtils;
import utils.ReadableUtils;

public class JPEGFiles {

  public static int NOT_YET_OPTIMIZED = 0;
  public static int OPTIMIZING = 1;
  public static int OPTIMIZED_OK = 2;
  public static int OPTIMIZED_KO = 3;
  public static int OPTIMIZED_UNNECESSARY = 4;
  public static int OPTIMIZED_OVERWRITE_NOT_ALLOWED = 5;

  private JPEGFilesListener _listener;
  private Loger _loger;

  private File _src;
  private File _dst;

  private long _originalSrcSize;

  private long _start;
  private long _end;

  private int _state = NOT_YET_OPTIMIZED;

  private int _jpegQualityFound = 100;

  private int _maxOptimSteps = 2 * 7; // max steps in dichotomic search between 0-100 = Math.ceil(Math.log2(101));
                                      // multiply per 2 because we do 2 sub step (create jpeg + compute diff)
  private int _currentOptimStep = 0;

  public JPEGFiles(File src) {
    _loger = null;
    _listener = null;
    _src = src;
    _originalSrcSize = _src.length();
  }

  public void setLoger(Loger loger) {
    _loger = loger;
  }

  public void setListener(JPEGFilesListener listener) {
    _listener = listener;
  }

  public void setDst(File file) {
    _dst = file;
  }

  public File getSrc() {
    return _src;
  }

  public File getDst() {
    return _dst;
  }

  public Double getEarnRate() {
    Double earn = null;
    if (_dst != null && _src != null) {
      if (_dst.exists() && _src.exists()) {
        earn = 1. - (_dst.length() / (double) _originalSrcSize);
      }
    }
    return earn;
  }

  public Long getEarnSize() {
    Long earn = null;
    if (_dst != null && _src != null) {
      if (_dst.exists() && _src.exists()) {
        earn = _originalSrcSize - _dst.length();
      }
    }
    return earn;
  }

  public void reinitState() {
    setState(NOT_YET_OPTIMIZED);
    _originalSrcSize = _src.length();
  }

  public int getState() {
    return _state;
  }

  public boolean canDisplayDiffImages() {
    if (_state == OPTIMIZED_OK) {
      if (_src.getAbsolutePath().compareTo(_dst.getAbsolutePath()) != 0) {
        return true;
      }
    }
    return false;
  }

  public long getElaspedTime() {
    return _end - _start;
  }

  public int getMaxOptimStep() {
    return _maxOptimSteps;
  }

  public int getCurrentOptimStep() {
    return _currentOptimStep;
  }

  public int getJpegQualityFound() {
    return _jpegQualityFound;
  }

  public long getOriginalSrcSize() {
    return _originalSrcSize;
  }

  private void setState(int state) {
    _state = state;
    if (_state == OPTIMIZING) {
      _currentOptimStep = 0;
    }
    _listener.stateChange(this);
  }

  private void incCurrentOptimStep() {
    _currentOptimStep++;
    _listener.stateChange(this);
  }

  private boolean optimize(BufferedImage img1, File tmp, int quality, double maxVisualDiff) throws IOException {
    log("   Trying quality " + quality + "%");

    if (tmp.exists()) {
      tmp.delete();
    }

    long start1 = System.currentTimeMillis();
    ImageUtils.createJPEG(_src, tmp, quality);
    long end1 = System.currentTimeMillis();
    log("   * Size : " + ReadableUtils.fileSize(tmp.length()) + "\t (" + ReadableUtils.interval(end1 - start1) + ")");
    incCurrentOptimStep();

    long start2 = System.currentTimeMillis();
    BufferedImage img2 = ImageIO.read(tmp);
    double diff = ImageUtils.computeSimilarityRGB(img1, img2);
    long end2 = System.currentTimeMillis();
    incCurrentOptimStep();

    img2 = null;
    log("   * Diff : " + ReadableUtils.rate(diff) + "\t (" + ReadableUtils.interval(end2 - start2) + ")");
    diff *= 100.;
    if (diff < maxVisualDiff) {
      log("   [OK] Visual diff is correct.");
      _jpegQualityFound = quality;
      return true;
    } else {
      log("   [KO] Visual diff is too important, try a better quality.");
      return false;
    }
  }

  private boolean optimize(File dstDir, double maxVisualDiff) throws IOException {
    String threadName = "thread-" + Thread.currentThread().getId();
    File tmp = new File(dstDir, "JpegOptimizer." + threadName + ".tmp.jpg");
    BufferedImage img1 = ImageIO.read(_src);

    int minQ = 0;
    int maxQ = 100;
    int foundQuality = -1;
    while (minQ <= maxQ) {
      log(" - Dichotomic search between (" + minQ + ", " + maxQ + ") qualities :");
      int quality = (int) Math.floor((minQ + maxQ) / 2.);
      if (optimize(img1, tmp, quality, maxVisualDiff) == true) {
        foundQuality = quality;
        maxQ = quality - 1;
      } else {
        minQ = quality + 1;
      }
    }
    img1 = null;
    tmp.delete();

    if ((foundQuality >= 0) && (foundQuality < 100)) {
      log(" - [OK] Best quality found is " + foundQuality + "%");
      if (_dst.exists()) {
        log("   * Deleting existing destination file.");
        _dst.delete();
      }
      log("   * Creating result destination file.");
      ImageUtils.createJPEG(_src, _dst, foundQuality);
      return true;
    } else {
      log(" - [KO] Unable to optimize the file");
      return false;
    }
  }

  public void optimize(File dstDir, double maxVisualDiff, long minFileSizeToOptimize, boolean overwriteDst)
      throws IOException {
    System.out.println("Max Diff : " + maxVisualDiff);
    _start = System.currentTimeMillis();
    setState(OPTIMIZING);
    log("Optimizing " + _src.getAbsolutePath() + " (" + ReadableUtils.fileSize(_originalSrcSize) + ")");

    if (_dst.exists() && (overwriteDst == false)) {
      setState(OPTIMIZED_OVERWRITE_NOT_ALLOWED);
    } else {
      if (_src.length() <= minFileSizeToOptimize) {
        if (_dst.getAbsolutePath().compareTo(_src.getAbsolutePath()) != 0) {
          log(" - File too small, copy source file to destination.");
          if (_dst.exists()) {
            log("   * Deleting existing destination file.");
            _dst.delete();
          }
          log("   * Copying source file to destination.");
          FileUtils.copyFile(_src, _dst);
        }
        setState(OPTIMIZED_UNNECESSARY);
      } else {
        boolean isOptimized = optimize(dstDir, maxVisualDiff);
        setState(isOptimized ? OPTIMIZED_OK : OPTIMIZED_KO);
      }
    }

    _end = System.currentTimeMillis();

    if (_state == OPTIMIZED_OK) {
      success("Optimization done: from " + ReadableUtils.fileSize(_originalSrcSize) + " to "
          + ReadableUtils.fileSize(_dst.length()) + ". Earn " + ReadableUtils.rate(getEarnRate()));
    } else if (_state == OPTIMIZED_KO) {
      error("Unable to optimize file (too many visual difference when compressing).");
    } else if (_state == OPTIMIZED_UNNECESSARY) {
      success("Optimization unecessary (file already too small).");
    } else if (_state == OPTIMIZED_OVERWRITE_NOT_ALLOWED) {
      warn("Unable to optimize file (destination file already exists and overwrite is not allowed).");
    }
    log("Done in " + ReadableUtils.interval(_end - _start));
    log("--------------------------------------------------------------------------------------");
  }

  // ----------------------------------------------------------

  private void log(String txt) {
    System.out.println(txt);
    _loger.log(txt, true);
  }

  private void error(String txt) {
    System.err.println(txt);
    _loger.error(txt, true);
  }

  private void warn(String txt) {
    System.out.println(txt);
    _loger.warn(txt, true);
  }

  private void success(String txt) {
    System.out.println(txt);
    _loger.success(txt, true);
  }

}
