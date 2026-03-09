package com.rutar.ttool_dk2;

import java.nio.*;
import java.util.*;
import java.nio.charset.*;

// ............................................................................
/// Представлення одиночного символу шрифта
/// @author Rutar_Andriy
/// 07.03.2026

public class Symbol {

private String symbol;                         // рядкове представлення символу
private short unknown_01;                              // невідомий параметр №1
private int dataSize;                   // розмір даних для рендерингу в байтах
private int unknown_02;                                // невідомий параметр №2
private int unknown_03;                                // невідомий параметр №3
private short renderW;                         // ширина символу при рендерингу
private short renderH;                         // висота символу при рендерингу
private byte deltaX;                             // зсув символу по горизонталі
private byte deltaY;                               // зсув символу по вертикалі
private short fullWidth;              // ширина символу із врахуванням відступу

private final int id;                                  // ідентифікатор символу
private final byte[] data;                   // дані у вигляді байтового масиву
private final int descSize = 2+2+4+4+4+2+2+1+1+2;   // розмір загаловку символу

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
    
    symbol     = new String(charByte, Charset.forName("cp1251"));
    unknown_01 = buffer.getShort();
    dataSize   = buffer.getInt();
    unknown_02 = buffer.getInt();
    unknown_03 = buffer.getInt();
    
    renderW   = buffer.getShort();
    renderH   = buffer.getShort();
    deltaX    = buffer.get();
    deltaY    = buffer.get();
    fullWidth = buffer.getShort();
    
}

// ============================================================================

@Override
public String toString() {
    
    String msg = "Id=%3d, '%s', DataSize=%3d, RenderW=%3d, RenderH=%3d, " +
                 "DeltaX=%2d, DeltaY=%2d, FullW=%3d";
    
    return String.format(msg, id+1, symbol, dataSize, renderW, renderH, 
                                    deltaX, deltaY, fullWidth);

}

// ============================================================================

public byte[] getData() { return data; }

// ============================================================================

public byte[] getRenderData()
    { return Arrays.copyOfRange(data, descSize, data.length); }

// Кінець класу Symbol ========================================================

}
