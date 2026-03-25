package camp.xit.jacod.provider;

import camp.xit.jacod.model.CodelistEntry;

public interface ReferenceProvider {

    CodelistEntry provide(String codelist, String code);
}
