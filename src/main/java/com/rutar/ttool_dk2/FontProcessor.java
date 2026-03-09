package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.nio.file.*;
import javax.imageio.*;
import java.awt.image.*;

import static java.io.File.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import static javax.swing.JOptionPane.*;

// ............................................................................
/// Обробка ігрових шрифтів
/// @author Rutar_Andriy
/// 07.03.2026

public class FontProcessor {

private int w;                                             // ширина зображення
private int h;                                             // висота зображення
private int color;                                 // колір конкретного пікселя
private byte[] data;                                             // дані шрифта
private File inputFile;                                   // вхідний файл/папка
private File outputFile;                                 // вихідний файл/папка
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
ByteBuffer buffer = ByteBuffer.wrap(data);
buffer.order(ByteOrder.LITTLE_ENDIAN);

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
int position;
var indexes = new ArrayList<Integer>();
for (int z = 0; z < symbolsCount; z++)
    { indexes.add(buffer.getInt()); }

// ............................................................................
// Обробка усіх символів у циклі

Symbol symbol;
for (int q = 0; q < indexes.size()-1; q++) {

    // Отримання даних символа
    int from = indexes.get(q);
    int to   = indexes.get(q+1);
    byte[] charData = new byte[to - from];
    buffer.get(charData);

    // Ініціалізація символа
    symbol = new Symbol(q, charData);
    
    // Запис результату в файл
    String fileName = String.format("%03d", q + 1);
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

// Ініціалізація вхідного файлу
inputFile = dir;

// Ініціалізація вихідного файлу
outputFile = new File(inputFile.getAbsolutePath() + ".fnt");

// ............................................................................
// Збирання окремих символів у єдиний файл шрифту

try (FileOutputStream fos = new FileOutputStream(outputFile);
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

// Масив зображень окремих символів
File[] allFiles = inputFile.listFiles();

// Обробка символів у циклі
for (int z = 0; z < allFiles.length; z++) {

    // Отримання назви файлу для обробки
    String imageName = String.format("%03d_%02X", z + 1, z + 1);
    for (File f : allFiles)
        { if (f.getName().startsWith(imageName)) { imageName = f.getName();
                                                   break; } }
    
    // Зчитування даних зображення
    image = ImageIO.read(new File(inputFile.getAbsolutePath() + separator
                                                              + imageName));
    
    // Перевірка формату прочитаного зображення
    if (image.getType() != imageType)
        { String msg = "Файл %s має неправильний формат!%n"
                     + "Повинен бути 24-бітний BMP";
          showMessageDialog(window, msg.formatted(imageName), "Помилка", 0);
          return; }
    
    // Отримання масиву пікселів зображення
    byte[] imageData = ((DataBufferByte)(image.getRaster().getDataBuffer()))
                                                          .getData();
    byte[] writable = new byte[imageData.length / 3];
    
    // Обробка та запис даних
    for (int pixel = 0; pixel < writable.length; pixel++)
        { writable[pixel] = (byte) (imageData[pixel * 3] == 0 ? 0x0 : 0x1); }
    bos.write(writable);

}

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
