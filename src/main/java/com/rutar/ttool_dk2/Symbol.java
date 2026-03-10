package com.rutar.ttool_dk2;

import java.nio.*;
import java.util.*;
import java.nio.charset.*;

// ............................................................................
/// Представлення одиночного символу шрифта
/// @author Rutar_Andriy
/// 07.03.2026

public class Symbol {

private char symbol;                           // символьне представлення даних
private short unknown_01;                              // невідомий параметр №1
private int dataSize;      // розмір зашифрованих даних для рендерингу в байтах
private int totalSize;    // розмір розшифрованих даних для рендерингу в байтах
private byte dataType;                                             // тип даних
private byte unknown_02;                               // невідомий параметр №2
private byte unknown_03;                               // невідомий параметр №3
private byte unknown_04;                               // невідомий параметр №4
private short renderW;                         // ширина символу при рендерингу
private short renderH;                         // висота символу при рендерингу
private byte offsetX;                            // зсув символу по горизонталі
private byte offsetY;                              // зсув символу по вертикалі
private short fullWidth;              // ширина символу із врахуванням відступу

private final int id;                                  // ідентифікатор символу
private final byte[] data;                   // дані у вигляді байтового масиву
private final int descSize = 2+2+4+4+1+1+1+1+2+2+1+1+2;     // розмір заголовку

// ============================================================================

public Symbol (int id, byte[] data) { this.id = id;
                                      this.data = data;
                                      parseData(); }

// ============================================================================

private void parseData() {

    ByteBuffer buffer = ByteBuffer.wrap(data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    
    byte[] charByte = new byte[2];
    buffer.get(charByte);
    
    symbol = new String(charByte, Charset.forName("cp1251")).toCharArray()[0];
    
    unknown_01 = buffer.getShort();
    dataSize   = buffer.getInt();
    totalSize  = buffer.getInt();
    dataType   = buffer.get();
    unknown_02 = buffer.get();
    unknown_03 = buffer.get();
    unknown_04 = buffer.get();
    renderW    = buffer.getShort();
    renderH    = buffer.getShort();
    offsetX    = buffer.get();
    offsetY    = buffer.get();
    fullWidth  = buffer.getShort();
    
}

// ============================================================================

@Override
public String toString() {
    
    String symb = String.valueOf(symbol);
    if (symbol < 32 || symbol == 173) { symb = " "; }
    
    String msg = "Id=%3d, '%s', DataSize=%3d, TotalSize=%3d, Type=%X, "
               + "RenderW=%3d, RenderH=%3d, DeltaX=%2d, DeltaY=%2d, FullW=%3d";
    
    return String.format(msg, id+1, symb, dataSize, totalSize, dataType,
                              renderW, renderH, offsetX, offsetY, fullWidth);

}

// ============================================================================

public byte[] getData() { return data; }

// ============================================================================

public byte[] getRenderData()
    { return Arrays.copyOfRange(data, descSize, data.length); }

// ============================================================================

public char getChar() { return symbol; }

// Кінець класу Symbol ========================================================

}
