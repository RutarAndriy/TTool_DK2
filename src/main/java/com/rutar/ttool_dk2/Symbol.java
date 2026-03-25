package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.awt.image.*;
import java.nio.charset.*;
import javax.imageio.stream.*;

// ............................................................................
/// Представлення одиночного символу шрифта
/// @author Rutar_Andriy
/// 07.03.2026

public class Symbol {

private char symbol;                           // символьне представлення даних
private short unknown_01;                             // невідомий параметр №1
private int compSize;                     // розмір зашифрованих даних в байтах
private int uncompSize;                  // розмір розшифрованих даних в байтах
private byte compType;          // тип шифрування даних, 0 - відсутнє, 1 - RLE4
private byte unknown_02;                              // невідомий параметр №2
private byte unknown_03;                              // невідомий параметр №3
private byte unknown_04;                              // невідомий параметр №4
private short symbolW;                        // ширина графічних даних символу
private short symbolH;                        // висота графічних даних символу
private byte offsetX;                    // зсув графічних даних по горизонталі
private byte offsetY;                      // зсув графічних даних по вертикалі
private short fullWidth;       // ширина символу із врахуванням відступу справа

private byte[] data;                            // дані у зашифрованому вигляді

// ............................................................................

private final int id;                                  // ідентифікатор символу
private final int fontHeight;                                  // висота шрифту
private final int descSize = 2+2+4+4+1+1+1+1+2+2+1+1+2;     // розмір заголовку
private final int imageType = BufferedImage.TYPE_3BYTE_BGR;   // тип зображення

// ============================================================================
/// Конструктор за замовчуванням
/// @param id ідентифікатор символу
/// @param fontHeight висота шрифту
/// @param data необроблені дані символу

public Symbol (int id, int fontHeight, byte[] data)
    { this.id = id;
      this.data = data;
      this.fontHeight = fontHeight;
      parseData(); }

// ============================================================================
/// Парсинг необроблених даних

private void parseData() {

    ByteBuffer buffer = ByteBuffer.wrap(data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    
    byte[] charByte = new byte[2];
    buffer.get(charByte);
    
    symbol = new String(charByte, Charset.forName("cp1251")).toCharArray()[0];
    
    unknown_01 = buffer.getShort();   // невідомий параметр №1
    compSize   = buffer.getInt();     // розмір зашифрованих даних
    uncompSize = buffer.getInt();     // розмір розшифрованих даних
    compType   = buffer.get();        // тип шифрування даних
    unknown_02 = buffer.get();        // невідомий параметр №2
    unknown_03 = buffer.get();        // невідомий параметр №3
    unknown_04 = buffer.get();        // невідомий параметр №4
    symbolW    = buffer.getShort();   // ширина графічних даних символу
    symbolH    = buffer.getShort();   // висота графічних даних символу
    offsetX    = buffer.get();        // зсув графічних даних по горизонталі
    offsetY    = buffer.get();        // зсув графічних даних по вертикалі
    fullWidth  = buffer.getShort();   // загальна ширина символу
    
}

// ============================================================================
/// Повернення текстового представлення символу
/// @return текстове представлення символу

@Override
public String toString() {
    
    String symb = String.valueOf(symbol);
    if (symbol < 32 || symbol == 160 || symbol == 173) { symb = " "; }
    
    String msg = "Id=%3d, '%s', DataSize=%3d, TotalSize=%3d, Type=%X, "
               + "RenderW=%3d, RenderH=%3d, DeltaX=%2d, DeltaY=%2d, FullW=%3d";
    
    return String.format(msg, id+1, symb, compSize, uncompSize, compType,
                              symbolW, symbolH, offsetX, offsetY, fullWidth);

}

// ============================================================================

public char  getChar()      { return symbol;    }
public byte  getOffsetX()   { return offsetX;   }
public short getFullWidth() { return fullWidth; }

// ============================================================================

public void setChar      (char newSymbol)     { symbol = newSymbol;       }
public void setOffsetX   (byte newOffsetX)    { offsetX = newOffsetX;     }
public void setFullWidth (short newFullWidth) { fullWidth = newFullWidth; }

// ============================================================================
/// Повернення масиву зашифрованих даних
/// @return масив зашифрованих даних

public byte[] getData() { return data; }

// ============================================================================
/// Повернення зображення символу
/// @return зображення символу

public BufferedImage getImage() { 

// Якщо розмір від'ємний або нульовий - повертаємо зображення розміром 1x1
if (symbolW <= 0 || symbolH <= 0)
    { var image = new BufferedImage(1, 1, imageType);
      image.setRGB(0, 0, 0x00ff00);
      return image; }

// Отримання масиву зашифрованих даних для рендерингу
byte[] compData = Arrays.copyOfRange(data, descSize, data.length);

// Оголошення масиву розшифрованих даних для рендерингу
byte[] uncompData = new byte[Math.max(compSize, symbolW * symbolH)];

// Копіювання зашифрованих даних у масив розшифрованих даних
System.arraycopy(compData, 0, uncompData, 0, compData.length);

try { // перетворення байтового масиву на зображення

    // Якщо дані зашифровані - розшифровуємо їх
    if (compType == 1) { decodeRLE4(uncompData); }    
    
    // Ініціалазація потоків для читання бітів
    var bais = new ByteArrayInputStream(uncompData);
    var mciis = new MemoryCacheImageInputStream(bais);
    
    // Оголошення масиву для збереження оброблених даних
    byte[] imageData = new byte[uncompData.length * 2];
    
    // Перетворення даних з 4 біт у 8 біт
    for (int z = 0; z < imageData.length; z++)
        { imageData[z] = (byte) mciis.readBits(4); }
    
    // Створення зображення та отримання доступу до графіки
    var image = new BufferedImage(symbolW, fontHeight, imageType);
    var g = image.getGraphics();
    
    // Замальовування зображення червоним кольором
    g.setColor(Color.red);
    g.fillRect(0, 0, image.getWidth(), image.getHeight());
    
    // Обробка пікселів зображення в циклі
    for (int y = 0; y < symbolH; y++) {
    for (int x = 0; x < symbolW; x++) {
        // Отримання даних прозорості пікселя
        int pixelData = imageData[y * symbolW + x];
        // Перетворення отриманих даних на віддінок зеленого кольору
        int color = new Color(0, pixelData * 16, 0).getRGB();
        // Задання кольору для конкретного пікселя
        image.setRGB(x, y + offsetY, color);
    }        
    }
    
    // Повернення готового зображення
    return image; }

// Якщо відбулася помилка - повертаємо зображення розміром 1x1 
catch (IOException _) { var image = new BufferedImage(1, 1, imageType);
                        image.setRGB(0, 0, 0xff0000);
                        return image; } }

// ============================================================================
/// Ще не реалізовано ...
/// @param image ...

public void setImage (BufferedImage image) {  }

// ============================================================================
/// Розшифровування даних, зашифрованих за допомогою алгоритму RLE4

private void decodeRLE4 (byte[] data) throws IOException {

// Ініціалізація необхідних змінних
int count, value;
var sd = new ByteArrayInputStream(data);
var iis = new MemoryCacheImageInputStream(sd);
var baos = new ByteArrayOutputStream();

// Розшифровування даних в циклі
try (var mcios = new MemoryCacheImageOutputStream(baos)) {
    // Читання даних, поки вони не закінчуться
    while (true) {
        // Читання половини байту
        value = (int) iis.readBits(4);
        // Якщо зчитано 0 - це спеціальна мітка
        if (value == 0) {
            // Читання половини байту - кількість повторюваних даних
            count = (int) iis.readBits(4);
            // Якщо кількість повторюваних даних додатня - продовжуємо обробку
            if (count != 0) {
                // Читання половини байту - значення для повторювання
                value = (int) iis.readBits(4);
                // Записуємо повторюване значення в циклі
                for (int i = 0; i < count; i++)
                    { mcios.writeBits(value, 4); } }
            // Якщо кількість повторюваних даних дорівнює 0 - дані скінчилися
            else { break; } }
        // Якщо зчитано не 0 - записуємо значення у вихідний потік
        else { mcios.writeBits(value, 4); } } }

// Перетворення вихідного байтового потоку в масив байт
var result = baos.toByteArray();

// Перевизначення вхідних даних
System.arraycopy(result, 0, data, 0, result.length);

}

// Кінець класу Symbol ========================================================

}
