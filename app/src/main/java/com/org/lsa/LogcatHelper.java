package com.org.lsa;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogcatHelper {
    public static void captureLogs(Context context, String fileName) {
        Process process = null;
        BufferedReader bufferedReader = null;
        FileWriter writer = null;

        try {
            // Get the log file path based on the Android version
            File logFile;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            } else {
                logFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            }

            Log.d("Filename", "Log file path: " + logFile);

            // Check if the file exists; if so, open in append mode
            boolean fileExists = logFile.exists();
            writer = new FileWriter(logFile, true); // Open in append mode

            // If the file exists, add a date and time header
            if (fileExists) {
                String dateTimeHeader = "\n\n--- Logs captured on: " + getCurrentDateTime() + " ---\n";
                writer.write(dateTimeHeader);
            } else {
                // If the file does not exist, add an initial header
                String initialHeader = "--- Log File Created on: " + getCurrentDateTime() + " ---\n";
                writer.write(initialHeader);
            }

            // Run the logcat command
            String[] command = {"logcat", "-d"};
            process = Runtime.getRuntime().exec(command);

            // Read the logcat output and write it to the file
            InputStreamReader reader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                writer.write(line);
                writer.write("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close resources safely
            try {
                if (writer != null) {
                    writer.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to get the current date and time
    private static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
