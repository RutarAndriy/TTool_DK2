#pragma magic [ 46 34 46 42 ] @ 0x00
#pragma endian little

import std.io;

struct Header {
    u32 magic;
    u8  min_symbol_width;
    u8  max_symbol_height;
    u16 symbols_count;
};

struct SymbolEntry {
    char16 symbol;
    u16 unk_01;
    u32 data_size;
    u32 total_size;
    u8  data_type;
    u24 unk_02;
    u16 render_w;
    u16 render_h;
    u8  offset_x;
    u8  offset_y;
    u16 full_w;
    u8 data[data_size];
};

struct F4FB {
    Header header;
    u32 offsets[header.symbols_count];
    SymbolEntry entrys[header.symbols_count];
};

F4FB f4fb @ 0x00;