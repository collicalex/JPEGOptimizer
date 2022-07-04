package gui.windows;

import java.io.File;
import java.util.concurrent.Callable;

import core.JPEGFiles;

public class OptimizerCallable implements Callable<JPEGFiles> {
  JPEGFiles jpegFile;
  File outputDirectory;
  double maxVisualDiff;
  long minSize;
  boolean overwriteDst;

  public OptimizerCallable(JPEGFiles jpegFile, File outputDirectory, double maxVisualDiff, long minSize,
      boolean overwriteDst) {
    super();
    this.jpegFile = jpegFile;
    this.outputDirectory = outputDirectory;
    this.maxVisualDiff = maxVisualDiff;
    this.minSize = minSize;
    this.overwriteDst = overwriteDst;
  }

  @Override
  public JPEGFiles call() throws Exception {
    jpegFile.optimize(outputDirectory, maxVisualDiff, minSize, overwriteDst);
    return jpegFile;
  }

}
