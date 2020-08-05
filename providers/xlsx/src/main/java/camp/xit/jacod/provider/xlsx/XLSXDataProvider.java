package camp.xit.jacod.provider.xlsx;

import camp.xit.jacod.provider.CodelistNotChangedException;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author hlavki
 */
public class XLSXDataProvider implements DataProvider, Closeable {

    protected XSSFWorkbook workbook;
    private final String name;

    protected XLSXDataProvider(String resource) {
        this(null, resource);
    }

    public XLSXDataProvider(InputStream in) {
        this(null, in);
    }

    public XLSXDataProvider(String name, String resource) {
        this.name = name;
        if (resource != null) this.workbook = loadResource(resource);
    }

    public XLSXDataProvider(String name, InputStream in) {
        this.name = name;
        this.workbook = getWorkbook(in);
    }

    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        if (lastReadTime == -1) {
            return parseXlsx(codelist);
        } else {
            throw new CodelistNotChangedException(codelist);
        }
    }

    protected Optional<List<EntryData>> parseXlsx(String codelist) {
        XSSFSheet sheet = workbook.getSheet(codelist);
        List<EntryData> result = null;
        DataFormatter dataFormat = new DataFormatter();
        dataFormat.setDefaultNumberFormat(new DecimalFormat("#.##"));
        if (sheet != null) {
            result = new ArrayList<>();
            Row firstRow = sheet.getRow(0);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (!isRowEmpty(row)) {
                    EntryData entryData = new EntryData();
                    for (int colNum = 0; colNum < firstRow.getLastCellNum(); colNum++) {
                        Cell cell = row.getCell(colNum);
                        String value = dataFormat.formatCellValue(cell);
                        if (!isCellEmpty(cell) && cell.getCellType() == CellType.NUMERIC) {
                            value = String.valueOf(cell.getNumericCellValue());
                        }
                        if (isCellEmpty(cell)) value = null;
                        entryData.addField(firstRow.getCell(colNum).getStringCellValue(), value);
                    }
                    result.add(entryData);
                } else break;
            }
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Set<String> readAllNames() {
        Set<String> sheets = new HashSet<>();
        workbook.forEach(sheet -> sheets.add(sheet.getSheetName()));
        return sheets;
    }

    protected XSSFWorkbook getWorkbook(InputStream in) {
        try {
            return new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't load xlsx workbook", e);
        }
    }

    protected boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (!isCellEmpty(cell)) return false;
        }
        return true;
    }

    protected boolean isCellEmpty(Cell cell) {
        return (cell == null || cell.getCellType() == CellType.BLANK);
    }

    @Override
    public void close() throws IOException {
        this.workbook.close();
    }

    @Override
    public String getName() {
        return name != null ? name : DataProvider.super.getName();
    }

    private XSSFWorkbook loadResource(String resource) {
        try (InputStream in = XLSXDataProvider.class.getResourceAsStream(resource)) {
            return getWorkbook(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't load xlsx workbook", e);
        }
    }
}
