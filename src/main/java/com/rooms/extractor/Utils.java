package com.rooms.extractor;// necessary imports

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Utils {
    public static FileInputStream getExcelFile(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error opening excel file: " + e.getMessage());
            return null;
        }
    }

    public static XSSFWorkbook getExcelWorkbook(FileInputStream inputStream) {
        try {
            return new XSSFWorkbook(inputStream);
        } catch (Exception e) {
            System.err.println("Error accessing excel workbook" + e.getMessage());
            return null;
        }
    }

    public static void closeWorkBook(XSSFWorkbook workbook) {
        try {
            workbook.close();
        } catch (IOException e) {
            System.err.println("Error in closing the workbook " + e.getMessage());
        }
    }

    public static Integer convertTOInt(String str) {
        try {
            return Integer.parseInt(str);

        } catch (Exception e) {
            System.err.println("given string is not a number");
            return null;
        }
    }

    public static LinkedHashMap<String, String> formatToMap(String courseTime) {
        int indexOfColon = courseTime.indexOf(':');
        String hourString = courseTime.substring(0, indexOfColon);
        String minuteString = courseTime.substring(indexOfColon + 1, indexOfColon + 3);
        //if hour or minute is null then an error will be shown in the console.
        int hour = convertTOInt(hourString);
        int minute = convertTOInt(minuteString);
        if (courseTime.endsWith("PM") && hour != 12) {
            hour += 12;
        }
        LinkedHashMap<String, String> timesMap = new LinkedHashMap<>(Map.of("hour", ";" + hour + ";"));
        timesMap.put("minute", ";" + minute + ";");
        return timesMap;
    }

    public static File createOutputFile() {
        String fileName = "src/main/resources/output.txt";
        File file = new File(fileName);
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            System.err.println("Error in creating file");
            return null;

        }
    }

    public static void saveStrToFile(String input) {

        File file = createOutputFile();

        try {
            FileWriter outputFile = new FileWriter(file);
            outputFile.write(input);
            outputFile.close();
        } catch (IOException e) {
            System.err.println("There was an error accessing the file " + e.getMessage());
        }
    }
}