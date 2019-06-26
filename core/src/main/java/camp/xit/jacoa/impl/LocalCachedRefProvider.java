package camp.xit.jacoa.impl;

import camp.xit.jacoa.CodelistClient;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.provider.ReferenceProvider;
import java.util.HashMap;
import java.util.Map;

public final class LocalCachedRefProvider implements ReferenceProvider {

    private final CodelistClient cc;
    private final Map<String, Codelist<? extends CodelistEntry>> localCache;


    public LocalCachedRefProvider(CodelistClient cc) {
        this.cc = cc;
        this.localCache = new HashMap<>();
    }


    @Override
    public CodelistEntry provide(String codelist, String code) {
        Codelist<? extends CodelistEntry> cl = localCache.get(codelist);
        if (cl == null) {
            cl = cc.getCodelist(codelist);
            localCache.put(codelist, cl);
        }
        return cl.getEntry(code);
    }
}
