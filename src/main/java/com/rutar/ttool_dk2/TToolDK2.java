package com.rutar.ttool_dk2;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.font.*;
import javax.imageio.*;
import java.util.jar.*;
import java.awt.event.*;
import java.awt.image.*;
import java.nio.charset.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.formdev.flatlaf.*;
import com.rutar.ua_translator.*;
import com.formdev.flatlaf.themes.*;

import static javax.swing.JOptionPane.*;
import static javax.swing.JFileChooser.*;

// ............................................................................
/// Головний клас програми
/// @author Rutar_Andriy
/// 07.03.2026

public class TToolDK2 extends JFrame {

private File inputFile;                                         // вхідний файл
private File outputFile;                                       // вихідний файл

private final JFileChooser fileOpen;           // відкривання/збереження файлів
private final JFileChooser fntCompile;                  // компілювання шрифтів
private final JFileChooser fntDecompile;              // декомпілювання шрифтів
private final JFileChooser rawUnpack;                    // розпакування файлів
private final JFileChooser rawPack;                       // запакування файлів

private File tmpFile;                                       // допоміжна змінна
private String appDescription;                                 // опис програми
private DefaultTableModel tableModel;              // стандартна модель таблиці

private boolean dataWasChanged = false;        // якщо true - дані були змінені
private boolean reactOnChange = true;                       // допоміжна змінна

private final Font strikeFont;                             // закреслений шрифт

// ............................................................................

public static final ArrayList<Integer> editedList
              = new ArrayList<>();            // масив індексів змінених рядків

public static String fileExt;                    // розширення відкритого файлу
public static boolean debug = false; // якщо true - увімк. режим налагоджування

// ============================================================================
/// Конструктор за замовчуванням

public TToolDK2() {

initComponents();
initAppIcons();

fileOpen     = Utils.getFileChooser(FILES_ONLY, Map.of
                                   ("txt", "DK2 файли локалізації",
                                    "dat", "DK2 таблиця символів"));
fntCompile   = Utils.getFileChooser(DIRECTORIES_ONLY,
                                    "bf4", "DK2 файли шрифтів");
fntDecompile = Utils.getFileChooser(FILES_ONLY,
                                    "bf4", "DK2 файли шрифтів");
rawPack      = Utils.getFileChooser(FILES_ONLY,
                                    "bmp", "DK2 розпаковані файли");
rawUnpack    = Utils.getFileChooser(FILES_ONLY,
                                    "raw", "DK2 запаковані файли");

// Ініціалізація закресленого шрифта
Map<TextAttribute, Object> attr = new HashMap<>(mni_about.getFont()
                                                         .getAttributes());
attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
strikeFont = mni_about.getFont().deriveFont(attr);

}

// ============================================================================
/// Головний метод програми
/// @param args масив переданих параметрів

public static void main (String args[]) {
    
    if (args.length > 0 &&
        args[0].equals("--debug")) { debug = true; }
    
    // ........................................................................
    
    UATranslator.init();
    UIManager.put("FileChooser.readOnly", true);

    JFrame .setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    
    FlatLaf.registerCustomDefaultsSource("com.rutar.ttool_dk2.themes");

    try { FlatMacDarkLaf.setup(); }
    catch (Exception e) {}
    
    // ........................................................................
    
    EventQueue.invokeLater(() -> {
        new TToolDK2().setVisible(true);
    });
}

// ============================================================================
/// Відкривання файлів

private void showOpenDialog() {

// Дані змінилися - запитуємо чи відкривати новий файл
if (dataWasChanged) { 

String saveDataQuestion = """
    У відкритому файлі присутні зміни. При відкриванні
    нового файлу вони будуть втрачені. Бажаєте продовжити?
    """;

int answer = showConfirmDialog(this, saveDataQuestion,
                              "Повідомлення", YES_NO_OPTION);

if (answer != YES_OPTION) { return; }

}

// ............................................................................

int result = fileOpen.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = fileOpen.getSelectedFile();

String[] split = fileOpen.getSelectedFile().getName().split("\\.");
fileExt = split[split.length - 1].toLowerCase();

switch (fileExt) { case "txt" -> openTextFile();
                   case "dat" -> openDatFile(); }

updateAppTitle();

}

// ============================================================================
/// Відкривання *.txt файлів

private void openTextFile() {

prepareNewTable();
dataWasChanged = false;

// Читання ігрових файлів
try { new TextProcessor().read(inputFile, tbl_main);
      finalizeNewTable(); }

// ............................................................................

catch (IOException e)
    { showMessageDialog(this, "При обробленні файлу відбулася критична " +
                              "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Відкривання *.dat файлів

private void openDatFile() {

prepareNewTable();
dataWasChanged = false;

// Читання ігрових файлів
try { new DatProcessor().read(inputFile, tbl_main);
      finalizeNewTable(); }

// ............................................................................

catch (IOException e)
    { showMessageDialog(this, "При обробленні файлу відбулася критична " +
                              "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Збереження файлів

private void showSaveDialog() {

fileOpen.setSelectedFile(inputFile);
int result = fileOpen.showSaveDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

switch (fileExt) { case "txt" -> saveTextFile(); }

updateAppTitle();

}

// ============================================================================
/// Збереження *.txt файлів

private void saveTextFile() {

outputFile = fileOpen.getSelectedFile();

try {

new TextProcessor().write(outputFile, tbl_main);
dataWasChanged = false;
updateAppTitle();

showMessageDialog(this, "Файл " + outputFile.getName() + " успішно збережено",
                        "Повідомлення", INFORMATION_MESSAGE); }

// ............................................................................

catch (HeadlessException | IOException _)
    { showMessageDialog(this, "При збереженні файлу відбулася критична "
                            + "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Відображення інформації про програму

private void showInfoDialog() {

// Отримуємо текст опису програми
if (appDescription == null) {

URL descriptionUrl = getClass().getResource("others/appDescription.txt");
URL channelUrl     = getClass().getResource("others/channelURL.txt");
URL manifestUrl    = getClass().getClassLoader()
                    .getResource("META-INF/MANIFEST.MF");

try (InputStream desc = descriptionUrl.openStream();
     InputStream link = channelUrl    .openStream();
     InputStream data = manifestUrl   .openStream()) {

Attributes attributes = new Manifest(data).getMainAttributes();
    
String channelURL = new String(link.readAllBytes(), StandardCharsets.UTF_8);
String appVersion = attributes.getValue("Version");
String buildDate  = attributes.getValue("Build-Date");

appVersion = (appVersion == null) ? "0.0.1" : appVersion;
buildDate  = (buildDate  == null) ? "25.04.1995" : buildDate.split(" ")[0];

appDescription = new String(desc.readAllBytes(), StandardCharsets.UTF_8)
                    .formatted(channelURL, appVersion, buildDate); }

catch (IOException _) {} }

// ............................................................................

JEditorPane pane = new JEditorPane("text/html", appDescription);
pane.setEditable(false);
pane.setFocusable(false);

pane.addHyperlinkListener((HyperlinkEvent e) -> {
    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
        try { Desktop.getDesktop().browse(e.getURL().toURI()); }
        catch (IOException | URISyntaxException _) { }
    }
});

showMessageDialog(this, pane, "Про програму", INFORMATION_MESSAGE);

}

// ============================================================================
/// Відображення вікна пошуку інформації

private void showSearchDialog()
    { new SearchDialog(this).setVisible(true); }

// ============================================================================
/// Відображення вікна підтвердження виходу

private void showExitDialog() {

// Якщо дані не змінювалися - просто виходимо
if (!dataWasChanged) { System.exit(0); }

String saveDataQuestion = """
    Ви бажаєте вийти з програми?
    Усі незбережені дані буде втрачено
    """;

int answer = showConfirmDialog(this, saveDataQuestion,
                              "Підтвердження виходу", YES_NO_OPTION);

if (answer == YES_OPTION) { System.exit(0); }

}

// ============================================================================
/// Вибір шрифту для розпакування

private void showDecompileFontDialog() {

    int result = fntDecompile.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) { return; }

    inputFile = fntDecompile.getSelectedFile();
    new FontProcessor(this).decompile(inputFile);

}

// ============================================================================
/// decompile розпакованого шрифту для пакування

private void showCompileFontDialog() {

    tmpFile = Utils.getLastDir(fntDecompile);
    if (tmpFile != null) { fntCompile.setCurrentDirectory(tmpFile); }

    int result = fntCompile.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) { return; }

    inputFile = fntCompile.getSelectedFile();
    new FontProcessor(this).compile(inputFile);

}

// ============================================================================
/// Вибір даних для розпакування

private void showUnpackRawDialog() {

    int result = rawUnpack.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) { return; }

    inputFile = rawUnpack.getSelectedFile();
    new RawProcessor(this).unpack(inputFile);

}

// ============================================================================
/// Вибір розпакованих даних для пакування

private void showPackRawDialog() {

    tmpFile = Utils.getLastDir(rawUnpack);
    if (tmpFile != null) { rawPack.setCurrentDirectory(tmpFile); }

    int result = rawPack.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) { return; }

    inputFile = rawPack.getSelectedFile();
    new RawProcessor(this).pack(inputFile);

}

// ============================================================================
/// Попередня ініціалізація нової таблиці

private void prepareNewTable() {

dataWasChanged = false;
inputFile = fileOpen.getSelectedFile();
sp_table.getVerticalScrollBar().setValue(0);

tableModel = new DefaultTableModel() {
    @Override
    public boolean isCellEditable (int row, int column) {
        switch (fileExt) {
            case "dat" -> { return column >= 1; }
            default    -> { return column >= 2; } } } };

tbl_main.setModel(tableModel);

switch (fileExt) {
    case "txt" -> { tableModel.addColumn("№");
                    tableModel.addColumn("Ключ");
                    tableModel.addColumn("Значення"); }
    case "dat" -> { tableModel.addColumn("№");
                    tableModel.addColumn("Символ");
                    tableModel.addColumn("Пробіл після символу"); } } }

// ============================================================================
/// Завершальна ініціалізація нової таблиці

private void finalizeNewTable() {

CellRender centerRender = new CellRender();
centerRender.setHorizontalAlignment(SwingConstants.CENTER);

switch (fileExt)
    { case "txt" -> { setColumnParams(centerRender, 45, 175, 175); }
      case "dat" -> { setColumnParams(centerRender, 50, 250, 250); } }

updateTableInfo();

// ............................................................................

mni_find.setEnabled(true);
tableModel.addTableModelListener((TableModelEvent evt) -> {
    updateTableData(evt);
    updateAppTitle();
});

}

// ============================================================================

private void setColumnParams (CellRender render, int ... columnSizes) {

TableColumn tColumn;
boolean newRender, isResizable;

for (int z = 0; z < columnSizes.length; z++) {

    switch (fileExt)
        { case "dat" -> { newRender = z > 2; isResizable = z > 1; }
          default    -> { newRender = z > 1; isResizable = z > 1; } }
    
    tColumn = tbl_main.getColumnModel().getColumn(z);
    tColumn.setCellRenderer(!newRender ? render : new CellRender());
    tColumn.setPreferredWidth(columnSizes[z]);
    tColumn.setResizable(isResizable);

}

// ............................................................................

SwingUtilities.invokeLater(() -> {

    int totalW = 0;
    var cModel = tbl_main.getColumnModel();
    int viewportW = sp_table.getViewport().getWidth();
    
    for (int q = 0; q < tbl_main.getColumnCount(); q++)
        { totalW += cModel.getColumn(q).getPreferredWidth(); }
    
    if (totalW < viewportW)
        { var lastColumn = cModel.getColumn(tbl_main.getColumnCount() - 1);
          int prefW = viewportW - totalW + lastColumn.getPreferredWidth();
          lastColumn.setPreferredWidth(prefW); } });

}

// ============================================================================
/// Оновлення даних в таблиці

private void updateTableData (TableModelEvent e) {

    if (!reactOnChange) { return; }

    int rowId = e.getFirstRow();
    mni_save.setEnabled(true);
    dataWasChanged = true;

    if (!fileExt.equals("txt"))      { return; }
    if (!editedList.contains(rowId)) { editedList.add(rowId); }

    String key   = (String) tbl_main.getValueAt(rowId, 1);
    String value = (String) tbl_main.getValueAt(rowId, 2);

    reactOnChange = false;
    tbl_main.setValueAt("✓",                     rowId, 0);
    tbl_main.setValueAt("S = " + value.length(), rowId, 1);
    reactOnChange = true;

}

// ============================================================================
/// Оновлення інформації про таблицю

private void updateTableInfo() {

    String tmp;
        
    tmp = lbl_rowCount.getText();
    tmp = tmp.substring(0, tmp.indexOf(":") + 1) + " "
                      + tableModel.getRowCount();
    lbl_rowCount.setText(tmp);

    tmp = lbl_colCount.getText();
    tmp = tmp.substring(0, tmp.indexOf(":") + 1) + " "
                      + tableModel.getColumnCount();
    lbl_colCount.setText(tmp);
    
}

// ============================================================================
/// Оновлення заголовку головного вікна

private void updateAppTitle() {
    
    String newTitle = !dataWasChanged ? inputFile.getName() :
                                 "* " + inputFile.getName() + " *";
    
    if (!getTitle().equals(newTitle)) { setTitle(newTitle); }
}

// ============================================================================
/// Встановлення іконок для головного вікна

private void initAppIcons() {

    BufferedImage icon;
    ArrayList<Image> appIcons = new ArrayList<>();

    try {
        
    for (String resource : new String[] { "icon_16.png",
                                          "icon_32.png" }) {
        resource = "icons/" + resource;
        icon = ImageIO.read(getClass().getResourceAsStream(resource));
        appIcons.add(icon); }
    
    setIconImages(appIcons); }
    
    catch (IOException _) { }
    
}

// ============================================================================
/// Цей метод викликається з конструктора для ініціалізації форми.
/// УВАГА: НЕ змінюйте цей код. Вміст цього методу завжди 
/// перезапишеться редактором форм

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sp_table = new JScrollPane();
        tbl_main = new JTable();
        pnl_footer = new JPanel();
        lbl_colCount = new JLabel();
        lbl_rowCount = new JLabel();
        mnb_main = new JMenuBar();
        mn_file = new JMenu();
        mni_open = new JMenuItem();
        mni_save = new JMenuItem();
        sep_one = new JPopupMenu.Separator();
        mni_find = new JMenuItem();
        sep_two = new JPopupMenu.Separator();
        mni_exit = new JMenuItem();
        mn_edit = new JMenu();
        mni_fntDecompile = new JMenuItem();
        mni_fntCompile = new JMenuItem();
        sep_three = new JPopupMenu.Separator();
        mni_rawUnpack = new JMenuItem();
        mni_rawPack = new JMenuItem();
        sep_four = new JPopupMenu.Separator();
        mni_extProcessing = new JMenuItem();
        mn_info = new JMenu();
        mni_about = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("TTool_DK2");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                onWindowClose(evt);
            }
        });

        tbl_main.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbl_main.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl_main.setAutoscrolls(false);
        tbl_main.setIntercellSpacing(new Dimension(2, 2));
        tbl_main.setRowSelectionAllowed(false);
        tbl_main.setShowGrid(true);
        tbl_main.getTableHeader().setReorderingAllowed(false);
        tbl_main.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                onTableClick(evt);
            }
        });
        sp_table.setViewportView(tbl_main);

        pnl_footer.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));

        lbl_colCount.setText("Кількість стовбців: 0");
        pnl_footer.add(lbl_colCount);

        lbl_rowCount.setText("Кількість рядків: 0");
        pnl_footer.add(lbl_rowCount);

        mn_file.setText("Файл");

        mni_open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        mni_open.setText("Відкрити");
        mni_open.setActionCommand("open");
        mni_open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_open);

        mni_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        mni_save.setText("Зберегти");
        mni_save.setActionCommand("save");
        mni_save.setEnabled(false);
        mni_save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_save);
        mn_file.add(sep_one);

        mni_find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        mni_find.setText("Пошук");
        mni_find.setActionCommand("find");
        mni_find.setEnabled(false);
        mni_find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_find);
        mn_file.add(sep_two);

        mni_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        mni_exit.setText("Вихід");
        mni_exit.setActionCommand("exit");
        mni_exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_exit);

        mnb_main.add(mn_file);

        mn_edit.setText("Правка");

        mni_fntDecompile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        mni_fntDecompile.setText("Розпакувати шрифт");
        mni_fntDecompile.setActionCommand("decompileFont");
        mni_fntDecompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_fntDecompile);

        mni_fntCompile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        mni_fntCompile.setText("Запакувати шрифт");
        mni_fntCompile.setActionCommand("compileFont");
        mni_fntCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_fntCompile);
        mn_edit.add(sep_three);

        mni_rawUnpack.setText("Розпакувати файли");
        mni_rawUnpack.setActionCommand("unpackRaw");
        mni_rawUnpack.setEnabled(false);
        mni_rawUnpack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_rawUnpack);

        mni_rawPack.setText("Запакувати файли");
        mni_rawPack.setActionCommand("packRaw");
        mni_rawPack.setEnabled(false);
        mni_rawPack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_rawPack);
        mn_edit.add(sep_four);

        mni_extProcessing.setText("Розширена обробка");
        mni_extProcessing.setActionCommand("procExtended");
        mni_extProcessing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_extProcessing);

        mnb_main.add(mn_edit);

        mn_info.setText("Інфо");

        mni_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        mni_about.setText("Про програму");
        mni_about.setActionCommand("info");
        mni_about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_info.add(mni_about);

        mnb_main.add(mn_info);

        setJMenuBar(mnb_main);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sp_table, GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .addComponent(pnl_footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sp_table, GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

// ============================================================================
/// Прослуховування пунктів меню програми

    private void onMenuClick(ActionEvent evt) {//GEN-FIRST:event_onMenuClick

    switch (evt.getActionCommand()) {

        case "open" -> showOpenDialog();
        case "save" -> showSaveDialog();
        case "find" -> showSearchDialog();
        case "exit" -> showExitDialog();
        case "info" -> showInfoDialog();

        case "decompileFont" -> showDecompileFontDialog();
        case "compileFont"   -> showCompileFontDialog();
        case "unpackRaw"     -> showUnpackRawDialog();
        case "packRaw"       -> showPackRawDialog();

        case "procExtended"  -> isMenuSelected(mni_extProcessing, true);

    }   
    }//GEN-LAST:event_onMenuClick

// ============================================================================
/// Отримання стану пункту меню
/// @param item пункт меню
/// @param flip якщо true - зміна стану меню на протилежний
/// @return якщо true - пункт меню не закреслений

private boolean isMenuSelected (JMenuItem item, boolean flip) {

    // Визначення, чи пункт меню не закреслений
    boolean isSelected = !item.getFont().equals(strikeFont);
    
    // Зміна стану пункту меню на протилежний
    if (flip) { item.setFont(isSelected ? strikeFont : null); }
    
    return flip ? !isSelected : isSelected;

}

// ============================================================================
/// Прослуховування закривання вікна

    private void onWindowClose(WindowEvent evt) {//GEN-FIRST:event_onWindowClose
        showExitDialog();
    }//GEN-LAST:event_onWindowClose

// ============================================================================
/// Прослуховування натискань у таблиці

    private void onTableClick(MouseEvent evt) {                              
        // IO.println("Clicked");
    }                                                   

// ============================================================================
/// Список усіх об'явлених змінних

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel lbl_colCount;
    private JLabel lbl_rowCount;
    private JMenu mn_edit;
    private JMenu mn_file;
    private JMenu mn_info;
    private JMenuBar mnb_main;
    private JMenuItem mni_about;
    private JMenuItem mni_exit;
    private JMenuItem mni_extProcessing;
    private JMenuItem mni_find;
    private JMenuItem mni_fntCompile;
    private JMenuItem mni_fntDecompile;
    private JMenuItem mni_open;
    private JMenuItem mni_rawPack;
    private JMenuItem mni_rawUnpack;
    private JMenuItem mni_save;
    private JPanel pnl_footer;
    private JPopupMenu.Separator sep_four;
    private JPopupMenu.Separator sep_one;
    private JPopupMenu.Separator sep_three;
    private JPopupMenu.Separator sep_two;
    private JScrollPane sp_table;
    public JTable tbl_main;
    // End of variables declaration//GEN-END:variables

// Кінець класу TToolDK2 ======================================================

}
