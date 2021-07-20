package camp.xit.kiwi.jacod.provider.gsheet;

import camp.xit.jacod.provider.EntryData;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

public class MetaGSheetBatchDataProvider extends GSheetBatchDataProvider {

    private static final String DEFAULT_META_SHEET = "Codelists";
    private static final String DEFAULT_NAME_COLUMN = "NAME";

    private final String metaSheet;
    private List<EntryData> metadata;
    private String nameColumn;


    public MetaGSheetBatchDataProvider(File serviceAccountFile, String spreadSheetId) {
        this(null, serviceAccountFile, spreadSheetId, DEFAULT_HOLD_VALUES_TIMEOUT);
    }


    public MetaGSheetBatchDataProvider(File serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout) {
        this(null, serviceAccountFile, spreadSheetId, holdValuesTimeout);
    }


    public MetaGSheetBatchDataProvider(String name, File serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout) {
        this(name, serviceAccountFile, spreadSheetId, holdValuesTimeout, DEFAULT_META_SHEET, DEFAULT_NAME_COLUMN);
    }


    public MetaGSheetBatchDataProvider(File serviceAccountFile, String spreadSheetId, Duration holdValuesTimeout,
            String metaSheet, String nameColumn) {
        this(null, serviceAccountFile, spreadSheetId, holdValuesTimeout, metaSheet, nameColumn);
    }


    public MetaGSheetBatchDataProvider(String name, File serviceAccountFile, String spreadSheetId,
            Duration holdValuesTimeout, String metaSheet, String nameColumn) {
        super(name, serviceAccountFile, spreadSheetId, holdValuesTimeout);
        this.metaSheet = metaSheet;
        this.metadata = readMetadata();
        this.nameColumn = nameColumn;
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Optional<List<EntryData>> result = empty();
        if (getAllNames().contains(codelist)) {
            result = super.readEntries(codelist, lastReadTime);
        }
        return result;
    }


    protected List<EntryData> readMetadata() {
        return EntryParser.parse(gsheetService.getSheetValues(spreadSheetId, metaSheet));
    }


    @Override
    public final Set<String> getCodelistNames() {
        return getAllNames();
    }


    private Set<String> getAllNames() {
        return metadata.stream().map(e -> e.getSingleValue(nameColumn))
                .filter(v -> v.isPresent()).map(v -> v.get()).collect(toSet());
    }


    public synchronized void reloadMetadata() {
        this.metadata = readMetadata();
    }
}
