package camp.xit.jacod.impl;

import camp.xit.jacod.EntryMapper;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.ReferenceProvider;

public final class ShallowRefProvider implements ReferenceProvider {

    private final EntryMapper mapper;


    public ShallowRefProvider(EntryMapper mapper) {
        this.mapper = mapper;
    }


    @Override
    public CodelistEntry provide(String codelist, String code) {
        Class<? extends CodelistEntry> entryClass = mapper.getEntryClass(codelist).orElse(CodelistEntry.class);
        return CodelistEntryMapper.createShallowInstance(entryClass, code);
    }
}
