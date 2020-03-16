package camp.xit.jacod.cache2k.test;

import camp.xit.jacod.provider.CodelistNotChangedException;
import camp.xit.jacod.provider.EntryData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvErrorDataProvider extends SimpleCsvDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CsvErrorDataProvider.class);

    private boolean down = false;


    @Override
    public Optional<List<EntryData>> readEntries(String entryName, long lastReadTime) {
        if (down) {
            return Optional.empty();
        }
        if (lastReadTime < 0) {
            return parseCsv(entryName);
        } else {
            throw new CodelistNotChangedException(entryName);
        }
    }


    @Override
    public Set<String> readAllNames() {
        return Collections.emptySet();
    }


    private Optional<List<EntryData>> parseCsv(String entryName) {
        List<EntryData> result = null;
        List<String> names = new ArrayList<>();
        URL resourceUrl = CsvErrorDataProvider.class.getResource("/csv/" + entryName + ".csv");
        if (resourceUrl != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream()))) {
                String line;
                int lineNum = 0;
                result = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    List<String> values = CsvReader.parseLine(line);
                    if (++lineNum == 1) {
                        names = values.stream().map(v -> v.trim()).collect(Collectors.toList());
                    } else {
                        EntryData entry = new EntryData();
                        for (int i = 0; i < names.size(); i++) {
                            String value = values.get(i);
                            if (value != null && value.isBlank()) value = null;
                            entry.addField(names.get(i), value);
                        }
                        result.add(entry);
                    }
                }
            } catch (IOException e) {
                LOG.error("Cannot read CSV " + entryName, e);
            }
        }
        return Optional.ofNullable(result);
    }


    public void setDown() {
        this.down = true;
    }


    public void setUp() {
        this.down = false;
    }
}
