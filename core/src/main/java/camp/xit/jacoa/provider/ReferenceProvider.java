package camp.xit.jacoa.provider;

import camp.xit.jacoa.model.CodelistEntry;

public interface ReferenceProvider {

    CodelistEntry provide(String codelist, String code);
}
