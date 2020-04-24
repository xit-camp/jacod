package camp.xit.jacod.spring.cache.test;

import camp.xit.jacod.provider.CodelistNotChangedException;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import java.io.BufferedReader;
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

public class SimpleCsvDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCsvDataProvider.class);


    @Override
    public Optional<List<EntryData>> readEntries(String entryName, long lastReadTime) {
        if (lastReadTime < 0) {
            return parseCsv(entryName);
        } else {
            throw new CodelistNotChangedException(entryName);
        }
    }


    @Override
    public Set<String> getCodelistNames() {
        return Collections.emptySet();
    }


    private Optional<List<EntryData>> parseCsv(String entryName) {
        List<EntryData> result = null;
        List<String> names = new ArrayList<>();
        URL resourceUrl = SimpleCsvDataProvider.class.getResource("/csv/" + entryName + ".csv");
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
            } catch (Exception e) {
                LOG.error("Cannot read CSV " + entryName, e);
            }
        }
        return Optional.ofNullable(result);
    }
}
