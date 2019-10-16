package camp.xit.kiwi.jacod.provider.gsheet;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import camp.xit.kiwi.jacod.provider.gsheet.service.GSheetService;
import camp.xit.kiwi.jacod.provider.gsheet.service.GoogleApiException;
import camp.xit.kiwi.jacod.provider.gsheet.service.NotFoundException;
import camp.xit.kiwi.jacod.provider.gsheet.service.RangeValue;
import camp.xit.kiwi.jacod.provider.gsheet.service.SpreadSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GSheetDataProvider implements DataProvider {

    protected final GSheetService gsheetService;
    protected final String spreadSheetId;


    public GSheetDataProvider(String serviceAccountFile, String spreadSheetId) {
        this(null, serviceAccountFile, spreadSheetId);
    }


    public GSheetDataProvider(File serviceAccountFile, String spreadSheetId) {
        this(null, serviceAccountFile, spreadSheetId);
    }


    public GSheetDataProvider(Path serviceAccountFile, String spreadSheetId) {
        this(null, serviceAccountFile, spreadSheetId);
    }


    public GSheetDataProvider(String name, String serviceAccountFile, String spreadSheetId) {
        this(name, new File(serviceAccountFile), spreadSheetId);
    }


    public GSheetDataProvider(String name, Path serviceAccountFile, String spreadSheetId) {
        this(name, serviceAccountFile.toFile(), spreadSheetId);
    }


    public GSheetDataProvider(String name, File serviceAccountFile, String spreadSheetId) {
        this.gsheetService = new GSheetService(serviceAccountFile);
        this.spreadSheetId = spreadSheetId;
    }


    @Override
    public Set<String> readAllNames() {
        SpreadSheet spreadSheet = gsheetService.getSpreadSheet(spreadSheetId);
        return spreadSheet.getSheets().stream().map(s -> s.getProperties().getTitle()).collect(toSet());
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Optional<List<EntryData>> result = empty();
        try {
            RangeValue value = gsheetService.readSheetValues(spreadSheetId, codelist);
            return ofNullable(parseEntries(value));
        } catch (NotFoundException e) {
            log.warn("Codelist {} not found!", codelist);
        } catch (GoogleApiException e) {
            log.warn("Error occured while fetching codelist " + codelist, e);
        }
        return result;
    }


    protected List<EntryData> parseEntries(RangeValue sheetValues) {
        JsonNode valuesNode = sheetValues.getValues();
        ArrayNode fieldNamesNode = null;
        List<EntryData> result = new ArrayList<>();
        if (valuesNode.isArray()) {
            int rowNum = 0;
            for (JsonNode rowNode : valuesNode) {
                if (rowNode.isArray() && rowNum == 0) {
                    fieldNamesNode = (ArrayNode) rowNode;
                } else if (rowNode.isArray()) {
                    ArrayNode arrRowNode = (ArrayNode) rowNode;
                    EntryData data = new EntryData();
                    for (int idx = 0; idx < fieldNamesNode.size(); idx++) {
                        String key = fieldNamesNode.get(idx).asText();
                        String value = arrRowNode.get(idx).asText();
                        if (value.isEmpty()) value = null;
                        data.addField(key, value);
                    }
                    result.add(data);
                }
                rowNum++;
            }
        }
        return result;
    }
}
