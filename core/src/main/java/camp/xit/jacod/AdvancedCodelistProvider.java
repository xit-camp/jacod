package camp.xit.jacod;

import camp.xit.jacod.model.CodelistEntry;
import java.util.Collection;

public interface AdvancedCodelistProvider {

    Collection<Class<? extends CodelistEntry>> getAdvancedCodelists();
}
