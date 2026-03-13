package com.rutar.ttool_dk2;

import java.io.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import javax.swing.table.*;

import static java.nio.ByteOrder.*;
import static com.rutar.ttool_dk2.TToolDK2.*;

// ............................................................................
/// Обробка зашифрованих ігрових файлів
/// @author Rutar_Andriy
/// 07.03.2026

public class DatProcessor {

private byte[] allBytes;                                   // усі зчитані байти
private ByteBuffer buffer;                    // буфер для читання/запису даних

// ============================================================================
/// Читання таблиці символів
/// @param inputFile вхідний файл
/// @param table головна таблиця із даними
/// @throws IOException якщо відбулася помилка обробки файлу

public void read (File inputFile, JTable table) throws IOException {

// Доступ до моделі даних головної таблиці
DefaultTableModel tModel = (DefaultTableModel) table.getModel();

// Зчитування всіх байт
allBytes = Files.readAllBytes(inputFile.toPath());

// Ініціалізація буфера
buffer = ByteBuffer.wrap(allBytes);
buffer.order(LITTLE_ENDIAN);

// Читання магічного числа
byte[] magic = new byte[4];
buffer.get(magic);
buffer.getShort();

byte[] symbolBytes = new byte[2];                    // байти зчитаного символу
short symbolsCount = buffer.getShort();                   // кількість символів
ArrayList<String> row = new ArrayList<>();         // масив даних рядка таблиці

// Зчитування таблиці символів
for (int z = 0; z < symbolsCount; z++)
    { row.clear();
      buffer.get(symbolBytes);
      row.add(String.valueOf(z + 1));
      row.add(new String(new byte[] { symbolBytes[0] }, "cp1251"));
      row.add(symbolBytes[1] == 0x20 ? "+" : "-");
      tModel.addRow(row.toArray(String[]::new)); } }

// ============================================================================
/// Запис таблиці символів
/// @param outputFile вихідний файл
/// @param table головна таблиця із даними
/// @throws IOException якщо відбулася помилка обробки файлу

public void write (File outputFile, JTable table) throws IOException {

String tmp; // допоміжна змінна
ByteArrayOutputStream baos = new ByteArrayOutputStream();

for (int z = 0; z < table.getRowCount(); z++)
    { tmp = (String) table.getValueAt(z, 0);
      baos.write(tmp.getBytes());
      tmp = (String) table.getValueAt(z, 1);
      baos.write(tmp.getBytes());
      tmp = (String) table.getValueAt(z, 2);
      baos.write(tmp.getBytes()); }

// Запис результату в файл
try (FileOutputStream fos = new FileOutputStream(outputFile))
    { fos.write(allBytes); } }

// Кінець класу DatProcessor ==================================================

}
