package camp.xit.jacod.provider.gsheet;

import camp.xit.jacod.provider.EntryData;
import java.io.File;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

public final class MetaGSheetDataProvider extends GSheetDataProvider {

    private static final String DEFAULT_META_SHEET = "Codelists";
    private static final String DEFAULT_NAME_COLUMN = "NAME";

    private final String metaSheet;
    private List<EntryData> metadata;
    private String nameColumn;


    public MetaGSheetDataProvider(String serviceAccountFile, String spreadSheetId) {
        this(serviceAccountFile, spreadSheetId, DEFAULT_META_SHEET, DEFAULT_NAME_COLUMN);
    }


    public MetaGSheetDataProvider(File serviceAccountFile, String spreadSheetId) {
        this(serviceAccountFile, spreadSheetId, DEFAULT_META_SHEET, DEFAULT_NAME_COLUMN);
    }


    public MetaGSheetDataProvider(String serviceAccountFile, String spreadSheetId, String metaSheet, String nameColumn) {
        this(new File(serviceAccountFile), spreadSheetId, metaSheet, nameColumn);
    }


    public MetaGSheetDataProvider(File serviceAccountFile, String spreadSheetId, String metaSheet, String nameColumn) {
        super(serviceAccountFile, spreadSheetId);
        this.metaSheet = metaSheet;
        this.metadata = readMetadata();
        this.nameColumn = nameColumn;
    }


    public MetaGSheetDataProvider(String name, String serviceAccountFile, String spreadSheetId, String metaSheet, String nameColumn) {
        this(name, new File(serviceAccountFile), spreadSheetId, metaSheet, nameColumn);
    }


    public MetaGSheetDataProvider(String name, File serviceAccountFile, String spreadSheetId, String metaSheet, String nameColumn) {
        super(name, serviceAccountFile, spreadSheetId);
        this.metaSheet = metaSheet;
        this.metadata = readMetadata();
        this.nameColumn = nameColumn;
    }


    public synchronized void reloadMetadata() {
        this.metadata = readMetadata();
    }


    protected List<EntryData> readMetadata() {
        return GSheetEntryParser.parse(gsheetService.getSheetValues(spreadSheetId, metaSheet));
    }


    @Override
    public final Set<String> getCodelistNames() {
        return getAllNames();
    }


    private Set<String> getAllNames() {
        return metadata.stream().map(e -> e.getSingleValue(nameColumn))
                .filter(v -> v.isPresent()).map(v -> v.get()).collect(toSet());
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Optional<List<EntryData>> result = empty();
        if (getAllNames().contains(codelist)) {
            result = super.readEntries(codelist, lastReadTime);
        }
        return result;
    }
}
