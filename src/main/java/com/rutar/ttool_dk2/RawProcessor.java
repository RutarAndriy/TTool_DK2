package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import java.nio.*;
import javax.swing.*;
import java.nio.file.*;
import javax.imageio.*;
import java.awt.image.*;

import static javax.swing.JOptionPane.*;

// ............................................................................
/// Розпаковування/запаковування ігрових даних
/// @author Rutar_Andriy
/// 07.03.2026

public class RawProcessor {

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

public RawProcessor (JFrame window) { this.window = window; }

// ============================================================================
/// Розпаковування ігрових даних
/// @param file запакований файл

public void unpack (File file) {

// Ініціалізація вхідного файлу
inputFile = file;

// Ініціалізація вихідного файлу
outputFile = new File(inputFile.getAbsolutePath().replace(".raw", ".bmp"));

try {

// Зчитування файлу шрифта
data = Files.readAllBytes(inputFile.toPath());

// w = ...
// h = ...

// Ініціалізація зображення
image = new BufferedImage(w, h, imageType);

// ...

ImageIO.write(image, "bmp", outputFile);

showMessageDialog(window, "Файл успішно розпаковано!");

}

// ............................................................................

catch (HeadlessException | IOException e)
    { IO.println(e.getCause());
      showMessageDialog(window, "При розпакуванні файлу відбулася критична "
                              + "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Запаковування ігрових даних
/// @param file розпакований файл

public void pack (File file) {

// Ініціалізація вхідного файлу
inputFile = file;

// Ініціалізація вихідного файлу
outputFile = new File(inputFile.getAbsolutePath().replace(".bmp", ".raw"));

try (FileOutputStream fos = new FileOutputStream(outputFile);
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

// Зчитування даних зображення
image = ImageIO.read(inputFile);

// Перевірка формату прочитаного зображення
if (image.getType() != imageType)
    { String imageName = inputFile.getName();
      String msg = "Файл %s має неправильний формат!%n"
                 + "Повинен бути 24-бітний BMP";
      showMessageDialog(window, msg.formatted(imageName), "Помилка", 0);
      return; }

// Отримання розмірів зображення
w = image.getWidth();
h = image.getHeight();

// Ініціалізація буферу для запису даних
ByteBuffer buffer = ByteBuffer.allocate(w * h * 3);
buffer.order(ByteOrder.LITTLE_ENDIAN);

// Перетворюємо кольори пікселів у формат rgb565
for (int y = 0; y < h; y++) {
for (int x = 0; x < w; x++) {
    color = image.getRGB(x, y);
    buffer.putInt(color);
}
}

// Запис даних у файл
bos.write(buffer.array());

showMessageDialog(window, "Файл успішно запаковано!");

}

// ............................................................................

catch (Exception e)
    { IO.println(e.getCause());
      showMessageDialog(window, "При пакуванні файлу відбулася критична "
                              + "помилка", "Помилка", ERROR_MESSAGE); }
}

// Кінець класу RawProcessor ==================================================

}
