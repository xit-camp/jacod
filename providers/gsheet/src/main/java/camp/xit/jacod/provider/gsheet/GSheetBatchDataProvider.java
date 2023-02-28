package camp.xit.jacod.provider.gsheet;

import camp.xit.jacod.provider.BatchDataProvider;
import camp.xit.jacod.provider.EntryData;
import camp.xit.jacod.provider.gsheet.service.GSheetService;
import camp.xit.jacod.provider.gsheet.service.RangeValue;
import camp.xit.jacod.provider.gsheet.service.SpreadSheet;
import camp.xit.jacod.provider.gsheet.service.ValueRanges;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

public class GSheetBatchDataProvider extends BatchDataProvider {

    protected final GSheetService gsheetService;
    protected final String spreadSheetId;


    public GSheetBatchDataProvider(File serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout) {
        this(null, new GSheetService(serviceAccountFile), spreadSheetId, holdValuesTimeout);
    }


    public GSheetBatchDataProvider(File serviceAccountFile, String spreadSheetId) {
        this(null, serviceAccountFile, spreadSheetId);
    }


    public GSheetBatchDataProvider(InputStream serviceAccount, String spreadSheetId) {
        this(null, serviceAccount, spreadSheetId);
    }


    public GSheetBatchDataProvider(Path serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout) {
        this(null, serviceAccountFile, spreadSheetId, holdValuesTimeout);
    }


    public GSheetBatchDataProvider(Path serviceAccountFile, String spreadSheetId) {
        this(null, serviceAccountFile, spreadSheetId);
    }


    public GSheetBatchDataProvider(String name, InputStream serviceAccount, String spreadSheetId) {
        this(name, serviceAccount, spreadSheetId, DEFAULT_HOLD_VALUES_TIMEOUT);
    }


    public GSheetBatchDataProvider(String name, InputStream serviceAccount, String spreadSheetId, Duration holdValuesTimeout) {
        this(name, new GSheetService(serviceAccount), spreadSheetId, holdValuesTimeout);
    }


    public GSheetBatchDataProvider(String name, Path serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout) {
        this(name, serviceAccountFile.toFile(), spreadSheetId, holdValuesTimeout);
    }


    public GSheetBatchDataProvider(String name, Path serviceAccountFile, String spreadSheetId) {
        this(name, serviceAccountFile.toFile(), spreadSheetId);
    }


    public GSheetBatchDataProvider(String name, File serviceAccountFile, String spreadSheetId) {
        this(name, new GSheetService(serviceAccountFile), spreadSheetId, DEFAULT_HOLD_VALUES_TIMEOUT);
    }


    public GSheetBatchDataProvider(String name, File serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout) {
        this(name, new GSheetService(serviceAccountFile), spreadSheetId, holdValuesTimeout);
    }


    public GSheetBatchDataProvider(String name, GSheetService gsheetService, String spreadSheetId, Duration holdValuesTimeout) {
        super(holdValuesTimeout);
        this.gsheetService = gsheetService;
        this.spreadSheetId = spreadSheetId;
    }


    @Override
    public Set<String> getCodelistNames() {
        SpreadSheet spreadSheet = gsheetService.getSpreadSheet(spreadSheetId);
        return spreadSheet.getSheets().stream().map(s -> s.getProperties().getTitle()).collect(toSet());
    }


    @Override
    protected Map<String, List<EntryData>> readEntriesBatch() {
        ValueRanges ranges = gsheetService.getValuesBatch(spreadSheetId, getCodelistNames());
        Map<String, List<EntryData>> result = new HashMap();
        for (RangeValue range : ranges.getValueRanges()) {
            result.put(getNameFromRange(range.getRange()), GSheetEntryParser.parse(range));
        }
        return result;
    }


    private String getNameFromRange(String range) {
        String[] values = range.split("\\!");
        return values.length == 2 ? values[0] : range;
    }
}
