package com.zeewain.base.utils;

import java.io.File;
import java.util.Comparator;

/**
 * 按时间降序排列
 */
public class FileCompareUtils implements Comparator<File> {
    @Override
    public int compare(File file1, File file2) {
        if (file1.lastModified() < file2.lastModified()) {
            return 1;
        } else {
            return -1;
        }
    }
}
