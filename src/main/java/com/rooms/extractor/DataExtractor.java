package com.rooms.extractor;// apache library imports

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataExtractor {
    public static void extractData(String excelFilePath) {
        LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();
        String courseName = "courseName";
        List<String> daysList = List.of("sunday", "monday", "tuesday", "wednesday", "thursday");

        FileInputStream inputStream = Utils.getExcelFile(excelFilePath);
        XSSFWorkbook workbook = Utils.getExcelWorkbook(inputStream);
        if (inputStream == null || workbook == null) {
            return;
        }

        XSSFSheet sheet = workbook.getSheetAt(0);
        CellAddress firstRoomCell = new CellAddress("Y4");
        int roomCol = firstRoomCell.getColumn();
        int lastRow = sheet.getLastRowNum();

        // loop over all the rooms
        for (int currRow = 3; currRow < lastRow; currRow++) {
            XSSFRow roomRow = sheet.getRow(currRow);
            if (roomRow.getLastCellNum() <= 1) {
                continue;
            }
            XSSFCell roomCell = roomRow.getCell(roomCol);
            if (roomCell.getCellType() != CellType.STRING || roomCell.getStringCellValue().equals("ONLINE")) {
                continue;
            }

            // include only tuwaiq rooms
            String currentRoom = roomCell.getStringCellValue().trim();
            if (!(currentRoom.startsWith("E") || currentRoom.startsWith("F") || currentRoom.startsWith("G")
                    || currentRoom.startsWith("H"))) {
                continue;
            }

            LinkedHashMap<String, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("name", roomCell.getStringCellValue());
            // initializing each day as null the semicolons are used to format it into json
            // map
            daysList.forEach(day -> innerMap.put(day, ";null;"));
            XSSFRow daysRow = sheet.getRow(currRow + 1);

            // check which days are busy in the particular room
            for (int j = 9; j < daysRow.getLastCellNum(); j++) {
                ArrayList<LinkedHashMap<String, Object>> dayCourses = new ArrayList<>();
                XSSFCell daysCell = daysRow.getCell(j);
                if (daysCell.getCellType() != CellType.STRING) {
                    continue;
                }
                String dayCellString = daysCell.getStringCellValue().toLowerCase();

                for (int coursesRowNum = daysRow.getRowNum() + 2;; coursesRowNum++) {
                    LinkedHashMap<String, Object> courseDetailsMap = new LinkedHashMap<>();
                    XSSFRow coursesRow = sheet.getRow(coursesRowNum);
                    if (coursesRow == null || coursesRow.getLastCellNum() <= 1) {
                        break;
                    }
                    XSSFCell courseCell = coursesRow.getCell(j);
                    if (courseCell.getCellType() != CellType.STRING
                            || coursesRow.getCell(0).getCellType() == CellType.BLANK) {
                        continue;
                    }
                    String courseTimeStart = coursesRow.getCell(0).getStringCellValue();

                    LinkedHashMap<String, String> startTimesMap = Utils.formatToMap(courseTimeStart);

                    String courseTimeEnd = coursesRow.getCell(5).getStringCellValue();

                    LinkedHashMap<String, String> endTimesMap = Utils.formatToMap(courseTimeEnd);

                    courseDetailsMap.put("timeStart", startTimesMap);
                    courseDetailsMap.put("timeEnd", endTimesMap);
                    courseDetailsMap.put(courseName, courseCell.getStringCellValue());

                    if (!dayCourses.isEmpty()) {
                        LinkedHashMap<String, Object> freeTimeMap = new LinkedHashMap<>();
                        LinkedHashMap<String, Object> prevEntry = dayCourses.get(dayCourses.size() - 1);

                        LinkedHashMap<String, String> prevTimeEnd = null;
                        Object prevTimeEndObject = prevEntry.get("timeEnd");
                        if (prevTimeEndObject instanceof LinkedHashMap<?, ?>) {
                            LinkedHashMap<?, ?> rawPrevTimeEnd = (LinkedHashMap<?, ?>) prevTimeEndObject;
                            prevTimeEnd = new LinkedHashMap<>();
                            for (Map.Entry<?, ?> entry : rawPrevTimeEnd.entrySet()) {
                                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                                    prevTimeEnd.put((String) entry.getKey(), (String) entry.getValue());
                                }
                            }
                        }

                        freeTimeMap.put("timeStart", prevTimeEnd);
                        freeTimeMap.put("timeEnd", startTimesMap);
                        freeTimeMap.put("courseName", "Free");
                        dayCourses.add(freeTimeMap);

                    }
                    dayCourses.add(courseDetailsMap);

                }
                innerMap.put(dayCellString, dayCourses);
            }
            jsonMap.put(roomCell.getStringCellValue(), innerMap);

        }
        Utils.closeWorkBook(workbook);
        // converting java map to json map
        String rawMapString = jsonMap.toString().replace("=", ":").replace(Character.toString(160), " ");
        StringBuilder jsonMapString;
        jsonMapString = new StringBuilder();
        char[] rawMapCharArray = rawMapString.toCharArray();
        boolean isQuoteOpen = false;
        boolean isPaused = false;
        for (char character : rawMapCharArray) {
            if (!Character.isLetterOrDigit(character)) {
                if (character == ';') {
                    isPaused = !isPaused;
                    continue;
                }
                if (isQuoteOpen && !Character.isWhitespace(character) && character != '-' && character != '_') {
                    isQuoteOpen = false;
                    jsonMapString.append('"');
                }
                jsonMapString.append(character);
                continue;
            }
            if (!isQuoteOpen && !isPaused) {
                jsonMapString.append('"');
                isQuoteOpen = true;
            }
            jsonMapString.append(character);

        }
        Utils.saveStrToFile(jsonMapString.toString());
    }

}
