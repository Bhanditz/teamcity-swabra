/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.swabra.snapshots.iteration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * User: vbedrosova
 * Date: 30.01.2010
 * Time: 14:27:18
 */
public class FileSystemFilesIterator implements FilesIterator {
  @NotNull
  private File myRootFolder;
  private Stack<Iterator<File>> myIterators;

  public FileSystemFilesIterator(@NotNull File rootFolder) {
    myRootFolder = rootFolder;
  }

  @Nullable
  public FileInfo getNext() {
    if (myIterators == null) {
      myIterators = new Stack<Iterator<File>>();
      return processFolder(myRootFolder);
    }
    if (myIterators.isEmpty()) {
      return null;
    }
    final Iterator<File> it = myIterators.peek();
    if (it.hasNext()) {
      final File next = it.next();
      if (next.isFile()) {
        return createFileInfo(next);
      } else {
        return processFolder(next);
      }
    } else {
      myIterators.pop();
      if (myIterators.isEmpty()) {
        return null;
      }
      return getNext();
    }
  }

  private FileInfo processFolder(File folder) {
    final File[] files = folder.listFiles();
    if (files != null && files.length > 0) {
      final List<File> filesList = Arrays.asList(files);
      Collections.sort(filesList, new FilesComparator());
      myIterators.push(filesList.iterator());
    }
    return createFileInfo(folder);
  }

  private static FileInfo createFileInfo(File file) {
    return new FileInfo(file.getAbsolutePath(), file.length(), file.lastModified(), file.isFile());
  }
}