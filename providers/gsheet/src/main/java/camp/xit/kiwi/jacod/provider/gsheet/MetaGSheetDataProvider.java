package camp.xit.kiwi.jacod.provider.gsheet;

import camp.xit.jacod.provider.EntryData;
import static camp.xit.kiwi.jacod.provider.gsheet.service.MajorDimension.COLUMNS;
import camp.xit.kiwi.jacod.provider.gsheet.service.RangeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MetaGSheetDataProvider extends GSheetDataProvider {

    private static final String DEFAULT_META_SHEET = "Codelists";

    private final String metaSheet;
    private Set<String> codelists;


    public MetaGSheetDataProvider(String serviceAccountFile, String spreadSheetId) {
        this(serviceAccountFile, spreadSheetId, DEFAULT_META_SHEET);
    }


    public MetaGSheetDataProvider(File serviceAccountFile, String spreadSheetId) {
        this(serviceAccountFile, spreadSheetId, DEFAULT_META_SHEET);
    }


    public MetaGSheetDataProvider(String serviceAccountFile, String spreadSheetId, String metaSheet) {
        this(new File(serviceAccountFile), spreadSheetId, metaSheet);
    }


    public MetaGSheetDataProvider(File serviceAccountFile, String spreadSheetId, String metaSheet) {
        super(serviceAccountFile, spreadSheetId);
        this.metaSheet = metaSheet;
        this.codelists = readAllNames();
    }


    public MetaGSheetDataProvider(String name, String serviceAccountFile, String spreadSheetId, String metaSheet) {
        this(name, new File(serviceAccountFile), spreadSheetId, metaSheet);
    }


    public MetaGSheetDataProvider(String name, File serviceAccountFile, String spreadSheetId, String metaSheet) {
        super(name, serviceAccountFile, spreadSheetId);
        this.metaSheet = metaSheet;
        this.codelists = readAllNames();
    }


    public synchronized void reloadMetadata() {
        this.codelists = readAllNames();
    }


    @Override
    public final Set<String> readAllNames() {
        RangeValue value = gsheetService.readSheetValues(spreadSheetId, metaSheet + "!A1:A1024", COLUMNS);
        Optional<JsonNode> valuesNode = ofNullable(value.getValues());
        Optional<Iterator<JsonNode>> iteratorOpt = valuesNode
                .filter(v -> v.isArray()).map(v -> ((ArrayNode) v).get(0))
                .filter(v -> v.isArray()).map(v -> ((ArrayNode) v).iterator());
        Set<String> result = new HashSet<>();
        if (iteratorOpt.isPresent()) {
            Iterator<JsonNode> it = iteratorOpt.get();
            int idx = 0;
            while (it.hasNext()) {
                if (idx++ > 0) {
                    result.add(it.next().asText());
                }
            }
        }
        return result;
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Optional<List<EntryData>> result = empty();
        if (codelists.contains(codelist)) {
            result = super.readEntries(codelist, lastReadTime);
        }
        return result;
    }
}
