package camp.xit.jacoa.provider.xlsx;

import camp.xit.jacoa.provider.CodelistNotChangedException;
import camp.xit.jacoa.provider.DataProvider;
import camp.xit.jacoa.provider.EntryData;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;

/**
 *
 * @author hlavki
 */
public class CSVDataProvider implements DataProvider {

    private final Path path;
    private final CsvParserSettings parserSettings;
    private final Set<String> codelistNames;


    public CSVDataProvider(Path csvDirectory) {
        this.path = csvDirectory;
        this.parserSettings = new CsvParserSettings();
        this.codelistNames = getCodelistNames();
    }


    public CSVDataProvider(String csvDirectory) {
        this.path = Paths.get(csvDirectory);
        this.parserSettings = new CsvParserSettings();
        this.codelistNames = getCodelistNames();
    }


    public CSVDataProvider(String csvDirectory, CsvParserSettings parserSettings) {
        path = Paths.get(csvDirectory);
        this.parserSettings = parserSettings;
        this.codelistNames = getCodelistNames();
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
        CsvParser parser = new CsvParser(parserSettings);
        parser.beginParsing(path.resolve(codelist + ".csv").toFile());
        String[] headers = null;
        String[] row;
        int lineNum = 0;
        List<EntryData> result = new ArrayList<>();
        while ((row = parser.parseNext()) != null) {
            if (++lineNum == 1) {
                headers = row;
            } else {
                EntryData entry = new EntryData();
                for (int i = 0; i < headers.length; i++) {
                    String value = row[i];
                    if (value != null && value.isBlank()) value = null;
                    entry.addField(headers[i], value);
                }
                result.add(entry);
            }
        }
        return Optional.ofNullable(result);
    }


    @Override
    public Set<String> readAllNames() {
        return codelistNames;
    }


    private Set<String> getCodelistNames() {
        File[] files = path.toFile().listFiles(f -> f.getName().endsWith(".csv"));
        return Stream.of(files).map(f -> stripExtention(f)).collect(toSet());
    }


    private String stripExtention(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
