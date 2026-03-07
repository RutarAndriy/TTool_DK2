package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.nio.charset.*;
import javax.swing.filechooser.*;
import org.apache.commons.compress.compressors.deflate.*;

// ............................................................................
/// Корисні допоміжні методи
/// @author Rutar_Andriy
/// 07.03.2026

public class Utils {

// Домашня директорія користувача
public static final File HOME_DIR = FileSystemView.getFileSystemView()
                                                  .getHomeDirectory();

// ============================================================================
/// Перетворення кольору RGB565 в колір RGB888
/// @param rgb565 колір у RGB565 представленні
/// @return колір у RGB888 представленні

public static int from565to888rgb (short rgb565) {
    
    int r5 = (rgb565 >> 11) & 0x1F; // 5 байт
    int g6 = (rgb565 >> 5)  & 0x3F; // 6 байт
    int b5 =  rgb565        & 0x1F; // 5 байт
    
    int r8 = (r5 * 255) / 31;       // 8 байт
    int g8 = (g6 * 255) / 63;       // 8 байт
    int b8 = (b5 * 255) / 31;       // 8 байт
    
    return (r8 << 16) | (g8 << 8) | b8;
}

// ============================================================================
/// Перетворення кольору RGB888 в колір RGB565
/// @param rgb888 колір у RGB888 представленні
/// @return колір у RGB565 представленні

public static short from888to565rgb (int rgb888) {
    
    int r8 = (rgb888 >> 16) & 0xFF; // 8 байт
    int g8 = (rgb888 >> 8)  & 0xFF; // 8 байт
    int b8 =  rgb888        & 0xFF; // 8 байт
    
    int r5 = (r8 * 31 + 127) / 255; // 5 байт
    int g6 = (g8 * 63 + 127) / 255; // 6 байт
    int b5 = (b8 * 31 + 127) / 255; // 5 байт
    
    return (short) ((r5 << 11) | (g6 << 5) | b5);
}

// ============================================================================
/// Перетворення кольору RGB555 в колір RGB888
/// @param rgb555 колір у RGB555 представленні
/// @return колір у RGB888 представленні

public static int from555to888rgb (short rgb555) {
    
    int r5 = (rgb555 >> 10) & 0x1F; // 5 байт
    int g6 = (rgb555 >> 5)  & 0x1F; // 5 байт
    int b5 =  rgb555        & 0x1F; // 5 байт
    
    int r8 = (r5 * 255) / 31;       // 8 байт
    int g8 = (g6 * 255) / 31;       // 8 байт
    int b8 = (b5 * 255) / 31;       // 8 байт
    
    return (r8 << 16) | (g8 << 8) | b8;
}

// ============================================================================
/// Перетворення кольору RGB888 в колір RGB555
/// @param rgb888 колір у RGB888 представленні
/// @return колір у RGB555 представленні

public static short from888to555rgb (int rgb888) {
    
    int r8 = (rgb888 >> 16) & 0xFF; // 8 байт
    int g8 = (rgb888 >> 8)  & 0xFF; // 8 байт
    int b8 =  rgb888        & 0xFF; // 8 байт

    int r5 = (r8 * 31 + 127) / 255; // 5 байт
    int g5 = (g8 * 31 + 127) / 255; // 5 байт
    int b5 = (b8 * 31 + 127) / 255; // 5 байт

    return (short) ((r5 << 10) | (g5 << 5) | b5);
}

// ============================================================================
/// Перетворення кольору ARGB1555 в колір ARGB8888
/// @param argb1555 колір у ARGB1555 представленні
/// @return колір у RGB8888 представленні

public static int from1555to8888argb (short argb1555) {
    
    int a1 = (argb1555 >> 15) & 0x01; // 1 байт
    int r5 = (argb1555 >> 10) & 0x1F; // 5 байт
    int g5 = (argb1555 >> 5)  & 0x1F; // 6 байт
    int b5 =  argb1555        & 0x1F; // 5 байт
    
    int a8 =  a1 == 1 ? 0xFF : 0x00;  // 8 байт
    int r8 = (r5 << 3) | (r5 >> 2);   // 8 байт
    int g8 = (g5 << 3) | (g5 >> 2);   // 8 байт
    int b8 = (b5 << 3) | (b5 >> 2);   // 8 байт
    
    return (a8 << 24) | (r8 << 16) | (g8 << 8) | b8;
}

// ============================================================================
/// Перетворення кольору ARGB888 в колір ARGB1555
/// @param argb8888 колір у ARGB8888 представленні
/// @return колір у ARGB1555 представленні

public static short from8888to1555argb (int argb8888) {
    
    int a8 = (argb8888 >> 24) & 0xFF; // 8 байт
    int r8 = (argb8888 >> 16) & 0xFF; // 8 байт
    int g8 = (argb8888 >> 8)  & 0xFF; // 8 байт
    int b8 =  argb8888        & 0xFF; // 8 байт

    int a1 = (a8 >= 128) ? 1 : 0;     // 1 байт
    int r5 = (r8 * 31 + 127) / 255;   // 5 байт
    int g5 = (g8 * 31 + 127) / 255;   // 5 байт
    int b5 = (b8 * 31 + 127) / 255;   // 5 байт

    return (short) ((a1 << 15) | (r5 << 10) | (g5 << 5) | b5);
}

// ============================================================================
/// Отримання коду символу в кодуванні cp1251
/// @param c символ
/// @return код символу в кодуванні cp1251

public static int fromCP1251CharToCode (char c) {
    
    return String.valueOf(c).getBytes(Charset.forName("cp1251"))[0] & 0xFF;
}

// ============================================================================
/// Отримання символу за його кодом в кодуванні cp1251
/// @param code код символу в кодуванні cp1251
/// @return відповідний символ

public static char fromCodeToCP1251Char (int code) {
    
    byte bCode = (byte) code;
    return new String(new byte[]{bCode}, Charset.forName("cp1251")).charAt(0);
}

// ============================================================================
/// Перетворення символу на рядок
/// @param c символ для перетворення
/// @return рядкове представлення символу

public static String fromCharToString (char c) {
    
    String result = String.valueOf(c);

    // Обробка усіх символів, які не можна використовувати в іменах 
    // файлів на Windows (\ / : * ? " < > |), а також символу "_"
    if (result.equals("\\") || result.equals("/")  ||
        result.equals(":")  || result.equals("*")  ||
        result.equals("?")  || result.equals("\"") ||
        result.equals("<")  || result.equals(">")  ||
        result.equals("|")  || result.equals("_")) {
        
        result = Integer.toString(Utils.fromCP1251CharToCode(c));
    
    }
    
    return result;
}

// ============================================================================
/// Перетворення рядка на символ
/// @param s рядок для перетворення
/// @return символьне представлення рядка

public static char fromStringToChar (String s) {
    
    if (s.length() == 1) { return s.charAt(0); }
    else { return fromCodeToCP1251Char(Integer.parseInt(s)); }  
}

// ============================================================================
/// Розпакування даних, запакованих з допомогою алгоритму zlib (deflate)
/// @param compressed дані, запаковані з допомогою алгоритму zlib (deflate)
/// @return розпаковані дані

public static byte[] zlibDecompress (byte[] compressed) {

try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
     DeflateCompressorInputStream dis = new DeflateCompressorInputStream(bais);
     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

    int n;
    byte[] byteBuffer = new byte[8192];
    
    while ((n = dis.read(byteBuffer)) != -1) { baos.write(byteBuffer, 0, n); }
    
    return baos.toByteArray();
}

catch (Exception e) { IO.println("zlib decompress error");
                      return null; }
}

// ============================================================================
/// Запакування даних з допомогою алгоритму zlib (deflate)
/// @param decompressed незапаковані дані
/// @return запаковані дані

public static byte[] zlibCompress (byte[] decompressed) {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (DeflateCompressorOutputStream dos =
     new DeflateCompressorOutputStream(baos)) { dos.write(decompressed); }

    catch (Exception e) { IO.println("zlib compress error");
                          return null; }

    return baos.toByteArray();
}

// ============================================================================
/// Виділення клітинок у таблиці
/// @param table таблиця, клітинки якої потрібно виділяти
/// @param col номер стовбця клітинки, яку потрібно виділити
/// @param row номер рядка клітинки, яку потрібно виділити

public static void selectCell (JTable table, int col, int row) {

    table.setRowSelectionInterval   (row, row);
    table.setColumnSelectionInterval(col, col);

    Rectangle rect = table.getCellRect(row, col, true);
    table.scrollRectToVisible(rect);

}

// ============================================================================
/// Отримання налаштованого JFileChooser'а
/// @param selectionMode тип виділення (папки, файли, папки+файли)
/// @param ext розширення файлів
/// @param desc опис розширення файлів
/// @return налаштований екземпляр JFileChooser'а

public static JFileChooser getFileChooser (int selectionMode,
                                           String ext, String desc)
    { return getFileChooser(selectionMode, Map.of(ext, desc)); }

// ============================================================================
/// Отримання налаштованого JFileChooser'а
/// @param selectionMode тип виділення (папки, файли, папки+файли)
/// @param filters масив розширень та описів файлів
/// @return налаштований екземпляр JFileChooser'а

public static JFileChooser getFileChooser (int selectionMode,
                                           Map<String, String> filters) {
    
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(selectionMode);
    // chooser.removeChoosableFileFilter(chooser
    //        .getChoosableFileFilters()[0]);
    chooser.setCurrentDirectory(HOME_DIR);
    
    filters.forEach((ext, desc) ->
        { FileNameExtensionFilter f = new FileNameExtensionFilter(desc, ext);
          chooser.addChoosableFileFilter(f); });
    
    return chooser;

}

// ============================================================================
/// Отримання папки, у якій міститься останній виділений файл/папка
/// @param chooser jFileChooser, який використовувався для вибору файлу
/// @return папка, у якій міститься останній виділений файл/папка

public static File getLastDir (JFileChooser chooser) {
    
    File file = chooser.getSelectedFile();
    
    // Якщо останього файлу немає - повертаємо null
    if (file == null)
        { return null; }
    // Якщо останній файл є папкою - повертаємо батьківську папку
    else if (file.isDirectory())
        { return new File(file.getParent()); }
    // Якщо останній файл є файлом - повертаємо шлях до його папки
    else
        { return new File(file.getPath().replace(file.getName(), "")); }

}

// ============================================================================
/// Заміна невикористовуваних символів у тексті
/// @param value текст із невикористовуваними символами
/// @return текст із заміненими символами

public static String replaceUnusedChars (String value) {
    
    return value.replace('’', '\'')
                .replace('Ґ', 'Г')
                .replace('ґ', 'г');
}

// ============================================================================
/// Виведення байтового масиву в консоль у вигляді hex-значень
/// @param array байтовий масив для виведення в консоль

public static void printAsHex (byte[] array)
    { for (int q = 0; q < array.length; q++)
          { IO.print(" " + String.format("%02X", array[q]));
            if ((q+1) % 8  == 0) { IO.print(" ");  }
            if ((q+1) % 16 == 0) { IO.println(""); } } IO.println(); }

// Кінець класу Utils =========================================================

}
