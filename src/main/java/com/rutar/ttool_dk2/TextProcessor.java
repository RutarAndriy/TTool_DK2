package com.rutar.ttool_dk2;

import java.io.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import java.util.stream.*;
import javax.swing.table.*;

import static java.io.File.*;
import static java.nio.ByteOrder.*;
import static com.rutar.ttool_dk2.TToolDK2.*;

// ............................................................................
/// Обробка зашифрованих ігрових файлів
/// @author Rutar_Andriy
/// 07.03.2026

public class TextProcessor {

private String tmp;                                         // допоміжна змінна
private ByteBuffer buffer;                    // буфер для читання/запису даних

private static byte[] allBytes;                            // усі зчитані байти
private static char[] symbolsTable;                           // масив символів
private static byte[][] codesTable;                     // масив байтових кодів

// ............................................................................

private final int DATA_BLOCK_SIZE = 4;                    // розмір блоку даних
private final int HEADER_SIZE = 12;                         // розмір заголовку
private final StringBuilder builder = new StringBuilder();   // збирач символів
private final ArrayList<String> parts = new ArrayList<>();    // частини тексту

// ............................................................................

private final char oLink = '⋐';                     // символ початку посилання
private final char cLink = '⋑';                       // символ кінця посилання

private final char[] unprintableSymbols =   // заміни для недрукованих символів
  { '➀', '➁', '➂', '➃', '➄', '➅', '➆', '➇', '➈', '➉',
    '➊', '➋', '➌', '➍', '➎', '➏', '➐', '➑', '➒', '➓',
    'Ⅰ', 'Ⅱ', 'Ⅲ', 'Ⅳ', 'Ⅴ', 'Ⅵ', 'Ⅶ', 'Ⅷ', 'Ⅸ', 'Ⅹ', 'Ⅺ', 'Ⅻ' };

// ============================================================================
/// Читання зашифрованих ігрових текстів
/// @param inputFile вхідний файл
/// @param table головна таблиця із даними
/// @throws IOException якщо відбулася помилка обробки файлу

public void read (File inputFile, JTable table) throws Exception {

// Доступ до моделі даних головної таблиці
DefaultTableModel tModel = (DefaultTableModel) table.getModel();

// Ініціалізація та перевірка наявності файлу "MBToUni.dat"
File datFile = new File(inputFile.getParent() + separator + "MBToUni.dat");
if (!datFile.exists()) { throw new Exception("MBToUni.dat"); }

// Ініціалізація таблиці перетворення символів
initSymbolTable(datFile);

// Зчитування всіх байт вхідного файлу
allBytes = Files.readAllBytes(inputFile.toPath());

// Ініціалізація буфера
buffer = ByteBuffer.wrap(allBytes);
buffer.order(LITTLE_ENDIAN);

// Читання магічного числа
byte[] magic = new byte[4];
buffer.get(magic);
buffer.getInt();

int stringsCount = buffer.getInt();                         // кількість рядків
int[] offsets = new int[stringsCount];        // масив зміщень текстових рядків
ArrayList<String> row = new ArrayList<>();         // масив даних рядка таблиці

// Зчитування всіх зміщень текстових рядків
for (int z = 0; z < offsets.length; z++)
  { offsets[z] = buffer.getInt();
    if (debug) { IO.println("offset №%04d = 0x%X"
                   .formatted(z+1, offsets[z])); } }

// Зчитування всіх текстових рядків
for (int q = 0; q < offsets.length; q++)
  { // Визначення початку і кінця даних
    int from = offsets[q] + HEADER_SIZE;
    int to   = (q == offsets.length - 1) ? allBytes.length :
                                           offsets[q+1] + HEADER_SIZE;
    // Копіювання даних із загального масиву
    byte[] textData = Arrays.copyOfRange(allBytes, from, to);
    // Розшифровування текстового блоку
    tmp = decryptString(textData);
    // Додавання даних у таблицю
    row.clear();
    row.add(String.valueOf(q + 1));
    row.add(tmp);
    tModel.addRow(row.toArray(String[]::new)); } }

// ============================================================================
/// Запис зашифрованих ігрових текстів
/// @param outputFile вихідний файл
/// @param table головна таблиця із даними
/// @throws IOException якщо відбулася помилка обробки файлу

public void write (File outputFile, JTable table) throws Exception {

// Ініціалізація необхідних змінних
byte[] encryptedData;
ByteBuffer intBuffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);
int entrysCount = table.getRowCount();
int index = entrysCount * 4;

// Вихідні байтові потоки для запису даних
ByteArrayOutputStream main = new ByteArrayOutputStream();
ByteArrayOutputStream data = new ByteArrayOutputStream();

// Запис магічного числа та метаінформації
main.write(allBytes, 0, HEADER_SIZE);

// Запис інформації про зміщення блоків
for (int z = 0; z < table.getRowCount(); z++)
  { tmp = (String) table.getValueAt(z, 1);
    tmp = Utils.replaceUnusedChars(tmp);
    encryptedData = encryptString(tmp);
    data.write(encryptedData);
    intBuffer.clear().putInt(index);
    main.writeBytes(intBuffer.array());
    index += encryptedData.length; }

// Запис зашифрованих текстових блоків
main.writeBytes(data.toByteArray());

// Запис результату в файл
try (FileOutputStream fos = new FileOutputStream(outputFile))
  { fos.write(main.toByteArray()); }

}

// ============================================================================
/// Ініціалізація таблиці кодування символів
/// @param datFile вхідний "MBToUni.dat" файл
/// @throws IOException якщо відбулася помилка обробки файлу

private void initSymbolTable (File datFile) throws Exception {

// Зчитування всіх байт
allBytes = Files.readAllBytes(datFile.toPath());

// Ініціалізація буфера
buffer = ByteBuffer.wrap(allBytes);
buffer.order(LITTLE_ENDIAN);

// Читання магічного числа
byte[] magic = new byte[4];
buffer.get(magic);
buffer.getShort();

byte[] symbolBytes = new byte[2];                    // байти зчитаного символу
short symbolsCount = buffer.getShort();                   // кількість символів
symbolsTable = new char[symbolsCount];         // ініціалізація масиву символів
codesTable = new byte[symbolsCount][2];  // ініціалізація масиву байтових кодів

// Зчитування таблиці символів
for (int z = 0; z < symbolsCount; z++)
  { buffer.get(symbolBytes);
    codesTable[z] = symbolBytes.clone();
    symbolsTable[z] = getCharByBytes(symbolBytes);
    if (debug) { IO.println("%03d <> 0x%1$02X <> \"%s\""
                   .formatted(z, symbolsTable[z])); } }

}

// ============================================================================
/// Перетворення масиву байт на символ
/// @param bytes масив байт, який потрібно перетворити
/// @return символ

private char getCharByBytes (byte[] bytes) {

    // Перетворення байту у беззнакове ціле число
    int n = Byte.toUnsignedInt(bytes[0]);

    // Якщо символ є однобайтним - використовуємо кодування cp1251
    if (bytes[1] == 0) {
      // Заміна недрукованих спецсимволів
      if      (n < 32)   { return unprintableSymbols[n]; }
      // Заміна символу м'якого переносу
      else if (n == 173) { return '□'; }
      // Заміна символу нерозривного пробілу
      else if (n == 160) { return '■'; }
      // Заміна особливого керуючого символу
      else if (n == 152) { return '☑'; }
      // Заміна символу видалення
      else if (n == 127) { return '☒'; }
      // Обробка звичайних символів
      else { try { tmp = new String(new byte[] { bytes[0] }, "cp1251");
                   return tmp.toCharArray()[0]; }
             catch (UnsupportedEncodingException e) { return '※'; } } }

    // Якщо символ є двохбайтним - заміняємо його
    else { switch (n) { case 19 -> { return '♙'; }
                        case 24 -> { return '♘'; }
                        case 25 -> { return '♗'; }
                        case 28 -> { return '♖'; }
                        case 29 -> { return '♕'; }
                        case 38 -> { return '♔'; }
                        default -> { return '※'; } } }
}

// ============================================================================
/// Перетворення символу на масив байт
/// @param symbol символ, який потрібно перетворити
/// @return масив байт

private byte[] getBytesByChar (char symbol) {
    
    // Пошук символу в масиві недрукованих символів
    int index = IntStream.range(0, unprintableSymbols.length)
                         .filter(i -> unprintableSymbols[i] == symbol)
                         .findFirst().orElse(-1);
    if (index >= 0) { return new byte[] { (byte) index, 0 }; }
    
    // Обробка особливих символів
    switch (symbol)
      { case '♙' -> { return new byte[] {         19, 32 }; }
        case '♘' -> { return new byte[] {         24, 32 }; }
        case '♗' -> { return new byte[] {         25, 32 }; }
        case '♖' -> { return new byte[] {         28, 32 }; }
        case '♕' -> { return new byte[] {         29, 32 }; }
        case '♔' -> { return new byte[] {         38, 32 }; }
        case '□' -> { return new byte[] { (byte) 173,  0 }; }
        case '■' -> { return new byte[] { (byte) 160,  0 }; }
        case '☑' -> { return new byte[] { (byte) 152,  0 }; }
        case '☒' -> { return new byte[] { (byte) 127,  0 }; } }
    
    // Обробка звичайних символів
    try { byte[] bytes = Character.toString(symbol).getBytes("cp1251");
          return new byte[] { bytes[0], 0 }; }
    catch (UnsupportedEncodingException e) { return new byte[] { 0, 0 }; }
}

// ============================================================================
/// Розшифрування текстового блоку

private String decryptString (byte[] data) {
    
    // Якщо розмір нульовий - повертаємо пустий рядок
    if (data.length == 0) { return ""; }
    
    // Очищення попередніх даних
    buffer = ByteBuffer.wrap(data).order(LITTLE_ENDIAN);
    builder.setLength(0);
    
    byte type;          // тип даних: 1 - текст, 2 - посилання
    int position = 0;   // позиція обробки даних
    byte[] dataPart;    // частина даних
    short size, link;   // розмір даних та номер посилання
    
    // Обробка даних у циклі
    while (position < data.length - 1) {

      // Визначення типу даних
      type = buffer.get(); position++;

      // Тип даних 1 - текст
      if (type == 1) {
        // Визначення розміру даних
        size = buffer.getShort(); position += 2;
        buffer.get();             position += 1;
        // Зчитування текстових даних
        dataPart = new byte[size];
        buffer.get(dataPart); position += size;
        // Посимвольна обробка тексту
        for (byte b : dataPart)
            { int code = Byte.toUnsignedInt(b) - 1;
              builder.append(symbolsTable[code]); }
        // Розрахунок правильного розміру блоку
        int rem = position % DATA_BLOCK_SIZE;
        if (rem != 0) { position += (DATA_BLOCK_SIZE - rem); } }

      // Тип даних 2 - посилання
      else if (type == 2) {
        // Отримання номеру посилання
        link = buffer.getShort(); position += 2;
        buffer.get();             position += 1;
        // Обробка посилання
        builder.append(oLink);
        builder.append(String.valueOf(link + 1));
        builder.append(cLink); }

      // Тип даних 0 - дані закінчилися
      else { break; }

      // Оновлення позиції буферу
      buffer.position(position);
    }

    // Повернення результату
    return builder.toString();
}

// ============================================================================
/// Шифрування текстового блоку

private byte[] encryptString (String text) {

    // Очищення попередніх даних
    parts.clear();

    // Вихідних байтовий потік для запису результату
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // Розділення тексту на значущі частини
    for (String part : text.split(String.valueOf(oLink))) {
      // Якщо частина порожня - пропускаємо її
      if (part.isEmpty()) { continue; }
      // Знаходження позиції початку посилання
      int index = part.indexOf(cLink);
      // Якщо позиція не знайдена - це звичайний текст
      if (index == -1) { parts.add(part); }
      // Якщо позиція знайдена - це посилання
      else { // Отримання посилання
             parts.add(oLink + part.substring(0, index));
             // Отримання залишкового тексту
             if (index + 1 < part.length())
                { parts.add(part.substring(index + 1)); } } }

    // Обробка частин тексту в циклі
    for (String part : parts)
      { // Обробка посилань
        if (part.startsWith(String.valueOf(oLink)))
             { int link = Integer.parseInt(part.substring(1)) - 1;
               buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);
               buffer.put((byte) 0x2);
               buffer.putShort((short) link);
               buffer.put((byte) 0x0); }
        // Обробка звичайного тексту
        else { int bufferSize = 4 + part.length();
               int rem = bufferSize % DATA_BLOCK_SIZE;
               if (rem != 0) { bufferSize += (DATA_BLOCK_SIZE - rem); }
               buffer = ByteBuffer.allocate(bufferSize).order(LITTLE_ENDIAN);
               buffer.put((byte) 0x1);
               buffer.putShort((short) part.length());
               buffer.put((byte) 0x0);
               // Обробка символів у циклі
               for (char symbol : part.toCharArray())
                  { // Перетворення символу на масив байт
                    byte[] bytes = getBytesByChar(symbol);
                    // Пошук коду в масиві байтових кодів
                    int index = IntStream.range(0, codesTable.length)
                                         .filter(i -> Arrays
                                         .equals(codesTable[i], bytes))
                                         .findFirst().orElse(-1) + 1;
                    // Запис результату в буфер
                    buffer.put((byte) index); } }

        // Запис даних у вихідний потік
        baos.writeBytes(buffer.array()); }
    
    // Запис 4 пустих байт після завершення запису даних
    baos.writeBytes(new byte[4]);
    
    // Повернення результату
    return baos.toByteArray();
}

// Кінець класу TextProcessor =================================================

}