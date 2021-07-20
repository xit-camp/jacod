package camp.xit.jacod.provider.csv;

import camp.xit.jacod.provider.CodelistNotChangedException;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static java.util.Collections.emptySet;
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
        this.codelistNames = readCodelistNames();
    }


    public CSVDataProvider(String csvDirectory) {
        this.path = Paths.get(csvDirectory);
        this.parserSettings = new CsvParserSettings();
        this.codelistNames = readCodelistNames();
    }


    public CSVDataProvider(String csvDirectory, CsvParserSettings parserSettings) {
        path = Paths.get(csvDirectory);
        this.parserSettings = parserSettings;
        this.codelistNames = readCodelistNames();
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        if (lastReadTime == -1) {
            return parseCsv(codelist);
        } else {
            throw new CodelistNotChangedException(codelist);
        }
    }


    protected Optional<List<EntryData>> parseCsv(String codelist) {
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
    public Set<String> getCodelistNames() {
        return codelistNames;
    }


    private Set<String> readCodelistNames() {
        File[] files = path.toFile().listFiles(f -> f.getName().endsWith(".csv"));
        return files == null ? emptySet() : Stream.of(files).map(f -> stripExtention(f)).collect(toSet());
    }


    private String stripExtention(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
