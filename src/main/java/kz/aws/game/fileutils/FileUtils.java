package kz.aws.game.fileutils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<Integer> extractNumbers(String fileName) {
        List<Integer> numbers = new ArrayList<>();

        // Поиск индекса символа `_` в строке
        int underscoreIndex = fileName.indexOf('_');

        // Если символ `_` не найден или находится в начале строки, выход из метода
        if (underscoreIndex <= 0) {
            return numbers;
        }

        // Итерация от начала строки до символа `_`
        for (int i = 0; i < underscoreIndex; i++) {
            char currentChar = fileName.charAt(i);

            // Проверка, является ли текущий символ цифрой
            if (Character.isDigit(currentChar)) {
                // Начало числа - создание временной строки для накопления цифр
                StringBuilder numberBuilder = new StringBuilder();
                numberBuilder.append(currentChar);

                // Итерация дальше, чтобы собрать все цифры числа
                for (int j = i + 1; j < underscoreIndex; j++) {
                    char nextChar = fileName.charAt(j);

                    // Если следующий символ - цифра, добавляем ее к числу
                    if (Character.isDigit(nextChar)) {
                        numberBuilder.append(nextChar);
                    } else {
                        // Если следующий символ не цифра, прекращаем итерацию
                        break;
                    }
                }

                // Преобразование временной строки в число и добавление его в список
                try {
                    int extractedNumber = Integer.parseInt(numberBuilder.toString());
                    numbers.add(extractedNumber);
                } catch (NumberFormatException e) {
                    // В случае ошибки преобразования, игнорируем текущее число
                }
            }
        }

        return numbers;
    }
    
    public static List<Integer> extractNumbersFromDirectory(String directoryPath) {
        List<Integer> allNumbers = new ArrayList<>();

        File directory = new File(directoryPath);

        // Проверяем, является ли путь директорией
        if (directory.isDirectory()) {
            // Получаем список файлов в директории
            File[] files = directory.listFiles();

            if (files != null) {
                // Итерируемся по всем файлам и применяем метод extractNumbers к каждому файлу
                for (File file : files) {
                    if (file.isFile()) {
                        List<Integer> numbers = extractNumbers(file.getName());
                        allNumbers.addAll(numbers);
                    }
                }
            }
        }

        return allNumbers;
    }
    
    public static String findFileByNumber(String directoryPath, int number) {
        File directory = new File(directoryPath);

        // Проверяем, является ли путь директорией
        if (directory.isDirectory()) {
            // Получаем список файлов в директории
            File[] files = directory.listFiles();

            if (files != null) {
                // Итерируемся по всем файлам и ищем файл, начинающийся с указанной цифры
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();

                        // Проверяем, начинается ли файл с указанной цифры
                        if (fileName.matches("^" + number + "_.*")) {
                            return fileName;
                        }
                    }
                }
            }
        }

        return null; // Если файл не найден
    }
}

