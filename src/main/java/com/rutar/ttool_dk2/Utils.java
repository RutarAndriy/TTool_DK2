package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import javax.swing.*;
import java.nio.charset.*;
import javax.swing.filechooser.*;

// ............................................................................
/// Корисні допоміжні методи
/// @author Rutar_Andriy
/// 07.03.2026

public class Utils {

// Домашня директорія користувача
public static final File HOME_DIR = FileSystemView.getFileSystemView()
                                                  .getHomeDirectory();

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
        result.equals("|")  || result.equals("_")  || c < 32)
      { result = Integer.toString(Utils.fromCP1251CharToCode(c)); }
    
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
    chooser.removeChoosableFileFilter(chooser
           .getChoosableFileFilters()[0]);
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
/// Отримання даних із ByteBuffer
/// @param buffer буфер із даними
/// @return усі записані в буфер дані

public static byte[] getData (ByteBuffer buffer)
    { return Arrays.copyOf(buffer.array(), buffer.position()); }

// ============================================================================
/// Виведення байтового масиву в консоль у вигляді hex-значень
/// @param array байтовий масив для виведення в консоль

public static void printAsHex (byte[] array)
    { for (int q = 0; q < array.length; q++)
          { IO.print("%02X ".formatted(array[q]));
            if ((q+1) % 8  == 0) { IO.print(" "); }
            if ((q+1) % 16 == 0) { IO.println();  } } IO.println(); }

// Кінець класу Utils =========================================================

}
