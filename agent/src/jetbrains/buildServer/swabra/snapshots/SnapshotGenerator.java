package jetbrains.buildServer.swabra.snapshots;

import jetbrains.buildServer.swabra.SwabraLogger;
import static jetbrains.buildServer.swabra.snapshots.SnapshotUtil.*;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: vbedrosova
 * Date: 23.01.2010
 * Time: 14:04:16
 */
public class SnapshotGenerator {
  private final File myTempDir;
  private final File myCheckoutDir;
  private String myCheckoutDirParent;

  private final SwabraLogger myLogger;

  public SnapshotGenerator(@NotNull File checkoutDir,
                       @NotNull File tempDir,
                       @NotNull SwabraLogger logger) {
    myTempDir = tempDir;
    myCheckoutDir = checkoutDir;
    myCheckoutDirParent = checkoutDir.getParent();
    if (myCheckoutDirParent.endsWith(File.separator)) {
      myCheckoutDirParent = myCheckoutDirParent.substring(0, myCheckoutDirParent.length() - 1);
    }
    myLogger = logger;
  }

  public boolean snapshot(@NotNull String snapshotName) {
    final File snapshot = new File(myTempDir, snapshotName + FILE_SUFFIX);
    if (snapshot.exists()) {
      myLogger.debug("Swabra: Snapshot file " + snapshot.getAbsolutePath() + " exists, try deleting");
      if (!FileUtil.delete(snapshot)) {
        myLogger.debug("Swabra: Unable to delete " + snapshot.getAbsolutePath());
        return false;
      }
    }
    myLogger.message("Swabra: Saving state of checkout directory " + myCheckoutDir +
      " to snapshot file " + snapshot.getAbsolutePath(), true);

    BufferedWriter snapshotWriter = null;
    try {
      snapshotWriter = new BufferedWriter(new FileWriter(snapshot));
      snapshotWriter.write(getSnapshotHeader(myCheckoutDirParent));
      snapshotWriter.write(getSnapshotEntry(myCheckoutDir, myCheckoutDirParent));
      saveState(myCheckoutDir, snapshotWriter);
      myLogger.message("Swabra: Finished saving state of checkout directory " + myCheckoutDir + " to snapshot file " + snapshot.getAbsolutePath(), false);
    } catch (Exception e) {
      myLogger.error("Swabra: Unable to save snapshot of checkout directory '" + myCheckoutDir.getAbsolutePath()
        + "' to file " + snapshot.getAbsolutePath());
      myLogger.exception(e, true);
      return false;
    } finally {
      try {
        if (snapshotWriter != null) {
          snapshotWriter.close();
        }
      } catch (IOException e) {
        myLogger.exception(e, true);
        return false;
      }
    }
    return true;
  }

  private void saveState(@NotNull File dir, @NotNull BufferedWriter snapshotWriter) throws Exception {
    final File[] files = dir.listFiles();
    if (files == null || files.length == 0) return;
    final List<File> dirs = new ArrayList<File>();
    for (File file : files) {
      if (file.isFile()) {
        snapshotWriter.write(getSnapshotEntry(file, myCheckoutDirParent));
      } else {
        dirs.add(file);
      }
    }
    for (File d : dirs) {
      snapshotWriter.write(getSnapshotEntry(d, myCheckoutDirParent));
      saveState(d, snapshotWriter);
    }
  }
}