package it.fulminazzo.yamlparser.utils;

import it.fulminazzo.yamlparser.enums.LogMessage;

import java.io.*;
import java.util.Scanner;

/**
 * File utils.
 */
public class FileUtils {

    /**
     * Writes the given input stream to the specified file.
     *
     * @param output      the file
     * @param inputStream the input stream
     * @throws IOException the IO Exception
     */
    public static void writeToFile(File output, InputStream inputStream) throws IOException {
        if (!output.exists()) createNewFile(output);
        FileOutputStream outputStream = new FileOutputStream(output);
        if (inputStream != null) {
            while (inputStream.available() > 0) {
                byte[] tmp = new byte[Math.min(inputStream.available(), 8192)];
                if (inputStream.read(tmp) == -1) break;
                outputStream.write(tmp);
            }
            inputStream.close();
        }
        outputStream.close();
    }

    /**
     * Recursively create a file (if the parents do not exist, they will be created).
     *
     * @param file the file
     * @throws IOException the IO Exception
     */
    public static void createNewFile(File file) throws IOException {
        if (!file.getParentFile().exists()) createFolder(file.getParentFile());
        if (!file.createNewFile())
            throw new IOException(LogMessage.FILE_CREATE_ERROR.getMessage("%file%", file.getName()));
    }

    /**
     * Recursively create a folder.
     *
     * @param folder the folder
     * @throws IOException the IO Exception
     */
    public static void createFolder(File folder) throws IOException {
        File parent = folder.getParentFile();
        if (parent != null && !parent.exists()) createFolder(parent);
        if (!folder.mkdir())
            throw new IOException(LogMessage.FOLDER_CREATE_ERROR.getMessage("%folder%", folder.getName()));
    }

    /**
     * Copy one file to another.
     *
     * @param file1 the file to copy
     * @param file2 the resulting file
     * @throws IOException the IO Exception
     */
    public static void copyFile(File file1, File file2) throws IOException {
        if (!file1.exists()) return;
        if (!file2.exists()) createNewFile(file2);
        FileInputStream inputStream = new FileInputStream(file1);
        FileOutputStream outputStream = new FileOutputStream(file2);
        while (inputStream.available() > 0) {
            byte[] tmp = new byte[Math.min(inputStream.available(), 8192)];
            if (inputStream.read(tmp) == -1) break;
            outputStream.write(tmp);
        }
        inputStream.close();
        outputStream.close();
    }

    /**
     * Renames a file.
     *
     * @param fileFrom the file to start from
     * @param fileTo   the result file to be renamed to
     * @throws IOException the IO Exception
     */
    public static void renameFile(File fileFrom, File fileTo) throws IOException {
        if (!fileFrom.renameTo(fileTo))
            throw new IOException(LogMessage.FILE_RENAME_ERROR.getMessage("%file%", fileFrom.getName()));
    }

    /**
     * Deletes a file.
     *
     * @param file the file to be deleted
     * @throws IOException the IO Exception
     */
    public static void deleteFile(File file) throws IOException {
        if (!file.delete())
            throw new IOException(LogMessage.FILE_DELETE_ERROR.getMessage("%file%", file.getName()));
    }

    /**
     * Recursively deletes a folder.
     *
     * @param folder the folder to be deleted
     * @throws IOException the IO Exception
     */
    public static void deleteFolder(File folder) throws IOException {
        recursiveDelete(folder);
    }

    /**
     * Deletes all files and folders contained in the specified folder.
     * Then it deletes it as well.
     *
     * @param folder the folder to start with
     * @throws IOException the IO Exception
     */
    private static void recursiveDelete(File folder) throws IOException {
        File[] allContents = folder.listFiles();
        if (allContents != null)
            for (File file : allContents)
                if (file.isDirectory()) recursiveDelete(file);
                else deleteFile(file);
        if (!folder.delete())
            throw new IOException(LogMessage.FOLDER_DELETE_ERROR.getMessage("%folder%", folder.getName()));
    }

    /**
     * Compares two files line by line.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true, if they are identical
     */
    public static boolean compareTwoFiles(File file1, File file2) {
        try {
            Scanner fileScanner1 = new Scanner(file1);
            Scanner fileScanner2 = new Scanner(file2);
            while (fileScanner1.hasNextLine() && fileScanner2.hasNextLine())
                if (!fileScanner1.nextLine().equalsIgnoreCase(fileScanner2.nextLine()))
                    return false;
            return !fileScanner1.hasNextLine() && !fileScanner2.hasNextLine();
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /**
     * Converts a string into kebab-case.
     * For example, if the string is "CamelCase", it will be
     * converted into "kebab-case".
     *
     * @param string the string to convert
     * @return the converted string
     */
    public static String formatStringToYaml(String string) {
        StringBuilder result = new StringBuilder();
        for (String s : string.split("")) {
            if (s.equals(s.toUpperCase()) && !result.toString().isEmpty()) result.append("-");
            result.append(s.toLowerCase());
        }
        return result.toString();
    }
}