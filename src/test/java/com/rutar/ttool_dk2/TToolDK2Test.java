package com.rutar.ttool_dk2;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// ............................................................................
/// Базове тестування програми
/// @author Rutar_Andriy
/// 07.03.2026

@DisplayName("TToolDK2Test class")
public class TToolDK2Test {

// ============================================================================

@Test
@DisplayName("Should pass")
void shouldAnswerWithTrue()
  { assertTrue(true); }

// ============================================================================

@Test
@DisplayName("File .empty exist")
void fileEmptyExist()
  { assertNotNull(getClass().getResource(".empty")); }

// ============================================================================
    
// @Test
// @Disabled("skipped")
// @DisplayName("Should skip")
// void shouldSkip()
//   { fail("This error will be skipped"); }

// ============================================================================

// @Test
// @DisplayName("Should fail")
// void shouldFail()
//   { fail("Some error ..."); }

// Кінець класу TToolDK2Test ==================================================

}
