package com.rutar.ttool_dk2;

import java.io.*;

// ............................................................................
/// Читання масиву байт побітно
/// @author Rutar_Andriy
/// 11.03.2026

public class BitInputStream {

private long buffer = 0;                                     // накопичені біти
private int bitCount = 0;                           // кількість бітів у буфері

private final ByteArrayInputStream bais;              // вхідний байтовий потік

// ============================================================================
/// Конструктор за замовчуванням
/// @param data вхідний масив даних

public BitInputStream (byte[] data)
  { bais = new ByteArrayInputStream(data); }

// ============================================================================
/// Зчитування n-кількості біт
/// @param n кількість біт для зчитування
/// @return числове представлення зчитаних даних
/// @throws IOException якщо відбулася помилка читання даних

public long readBits (int n) throws IOException {

    if (n < 0 || n > 64)
      { throw new IllegalArgumentException("n must be 0..64"); }

    while (bitCount < n) {

      int nextByte = bais.read();
      if (nextByte == -1) { throw new IOException("End of stream"); }

      buffer = (buffer << 8) | nextByte;
      bitCount += 8;
    }

    int shift = bitCount - n;

    long result = (buffer >> shift) & ((1L << n) - 1);

    bitCount -= n;
    buffer &= (1L << bitCount) - 1;

    return result;
}

// ============================================================================
/// Зчитування одного біту
/// @return числове представлення зчитаного біту
/// @throws IOException якщо відбулася помилка читання даних

public long readOneBit() throws IOException
  { return readBits(1); }

// Кінець класу BitInputStream ================================================

}