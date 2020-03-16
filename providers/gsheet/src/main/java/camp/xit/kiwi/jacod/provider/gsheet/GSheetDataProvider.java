package camp.xit.kiwi.jacod.provider.gsheet;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import camp.xit.kiwi.jacod.provider.gsheet.service.GSheetService;
import camp.xit.kiwi.jacod.provider.gsheet.service.GoogleApiException;
import camp.xit.kiwi.jacod.provider.gsheet.service.NotFoundException;
import camp.xit.kiwi.jacod.provider.gsheet.service.RangeValue;
import camp.xit.kiwi.jacod.provider.gsheet.service.SpreadSheet;
import java.io.File;
import java.nio.file.Path;
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
            RangeValue value = gsheetService.getSheetValues(spreadSheetId, codelist);
            return ofNullable(EntryParser.parse(value));
        } catch (NotFoundException e) {
            log.warn("Codelist {} not found!", codelist);
        } catch (GoogleApiException e) {
            log.warn("Error occured while fetching codelist " + codelist, e);
        }
        return result;
    }
}
