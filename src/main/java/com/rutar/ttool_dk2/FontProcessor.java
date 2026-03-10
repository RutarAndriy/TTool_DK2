package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import java.awt.image.*;

import static java.io.File.*;
import static java.nio.ByteOrder.*;
import static javax.swing.JOptionPane.*;
import static com.rutar.ttool_dk2.TToolDK2.*;


// ............................................................................
/// Обробка ігрових шрифтів
/// @author Rutar_Andriy
/// 07.03.2026

public class FontProcessor {

private byte[] data;                                             // дані шрифта
private File inputFile;                                   // вхідний файл/папка
private File outputFile;                                 // вихідний файл/папка
private ByteBuffer buffer;                                       // буфер даних
private BufferedImage image;                           // зображення для запису

private final JFrame window;                          // головне вікно програми
private final int imageType = BufferedImage.TYPE_3BYTE_BGR;   // тип зображення

// ============================================================================
/// Конструктор за замовчуванням
/// @param window головне вікно програми

public FontProcessor (JFrame window) { this.window = window; }

// ============================================================================
/// Декомпіляція шрифта
/// @param font вхідний файл

public void decompile (File font) {

// Ініціалізація вхідного файлу
inputFile = font;

try {

// Зчитування файлу шрифта
data = Files.readAllBytes(inputFile.toPath());

// Створення папки для запису результатів розпакування
outputFile = new File(inputFile.getAbsolutePath().replace(".bf4", separator));
outputFile.mkdir();

// Ініціалізація буферу для зчитування даних
buffer = ByteBuffer.wrap(data).order(LITTLE_ENDIAN);

// Вихідний буфер для запису заголовку шрифта у файл
ByteArrayOutputStream baos = new ByteArrayOutputStream();

// Отримання магічного числа
byte[] magic = new byte[4];
buffer.get(magic);
baos.write(magic);

// Мінімальна ширина символу
byte minWidth  = buffer.get();
baos.write(minWidth);

// Максимальна висота символу
byte maxheight = buffer.get();
baos.write(maxheight);

// Кількість символів у шрифті
short symbolsCount = buffer.getShort();

// Отримання зміщень блоків даних
var indexes = new ArrayList<Integer>();
for (int z = 0; z < symbolsCount; z++)
    { indexes.add(buffer.getInt()); }

// ............................................................................
// Обробка усіх символів у циклі

Symbol symbol;
for (int q = 0; q < indexes.size(); q++) {

    // Отримання даних символа
    int to, from = indexes.get(q);
    if (q < indexes.size() - 1) { to = indexes.get(q+1);  }
    else                        { to = buffer.capacity(); }
    byte[] charData = new byte[to - from];
    buffer.get(charData);

    // Ініціалізація символа
    symbol = new Symbol(q, charData);
    if (debug) { IO.println(symbol.toString()); }
    
    // Запис результату в файл
    String fileName = String.format("%03d_%s", q + 1, Utils
                            .fromCharToString(symbol.getChar()));
    File output = new File(outputFile.getAbsolutePath() + separator +
                                                          fileName + ".bin");
    try (var fos = new FileOutputStream(output))
        { fos.write(symbol.getData()); } }

// Файл для запису заголовку шрифта
var header = new File(outputFile.getAbsolutePath() + separator + "header.bin");

// Запис даних у файл
try (var fos = new FileOutputStream(header))
    { fos.write(baos.toByteArray()); }

showMessageDialog(window, "Шрифт успішно розпаковано!");

}

// ............................................................................

catch (HeadlessException | IOException e)
    { IO.println(e.getCause());
      showMessageDialog(window, "При розпакуванні шрифта відбулася критична "
                              + "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Компіляція шрифта
/// @param dir вхідна папка

public void compile (File dir) {

int index = 0;

// Ініціалізація вхідної папки
inputFile = dir;

// Ініціалізація файлу що містить заголовок шрифта
File header = new File(inputFile.getAbsolutePath() + separator + "header.bin");

// Ініціалізація вихідного файлу
outputFile = new File(inputFile.getAbsolutePath() + ".bf4");

// ............................................................................
// Збирання окремих символів у єдиний файл шрифту

try (FileOutputStream fos = new FileOutputStream(outputFile);
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

// Масив зображень окремих символів
File[] allFiles = inputFile.listFiles();

// Запис інформації про заголовок шрифту
byte[] headerBytes = Files.readAllBytes(header.toPath());
index += headerBytes.length;
bos.write(headerBytes);

// Запис інформації про кількость символів у шрифті
buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);
short symbolCount = (short) (allFiles.length - 1);
buffer.putShort(symbolCount);
data = Utils.getData(buffer);
bos.write(data);

// Задання початкового значення індексу
index += 4 + 2 + (symbolCount - 1) * 4;

// Ініціалізація вихідного байтового потоку
var baos = new ByteArrayOutputStream();

// Обробка символів у циклі
for (int z = 0; z < allFiles.length - 1; z++) {

    // Отримання назви файлу для обробки
    String imageName = String.format("%03d", z + 1);
    for (File f : allFiles)
        { if (f.getName().startsWith(imageName)) { inputFile = f;
                                                   break; } }
    
    // Запис інформації про поточний індекс
    buffer.clear();
    buffer.putInt(index);
    bos.write(Utils.getData(buffer));
    
    // Зчитування даних символа
    byte[] symbolBytes = Files.readAllBytes(inputFile.toPath());
    index += symbolBytes.length;
    baos.write(symbolBytes);

}

// Запис даних у файл
bos.write(baos.toByteArray());

showMessageDialog(window, "Шрифт успішно запаковано!");

}

// ............................................................................

catch (Exception e)
    { IO.println(e.getCause());
      showMessageDialog(window, "При пакуванні шрифта відбулася критична "
                              + "помилка", "Помилка", ERROR_MESSAGE); }
}

// Кінець класу FontProcessor =================================================

}
