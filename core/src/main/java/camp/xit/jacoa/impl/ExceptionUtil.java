package camp.xit.jacoa.impl;

import camp.xit.jacoa.CodelistNotFoundException;
import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.provider.DataProvider;

final class ExceptionUtil {

    private ExceptionUtil() {
    }


    static CodelistNotFoundException notFoundException(String codelist, DataProvider provider, CodelistEntryMapper mapper) {
        String msg = getNotFoundMessage(codelist, provider, mapper);
        return new CodelistNotFoundException(codelist, msg);
    }


    static CodelistNotFoundException notFoundException(Class<? extends CodelistEntry> codelistClass,
            DataProvider provider, CodelistEntryMapper mapper) {
        String msg = getNotFoundMessage(codelistClass, provider, mapper);
        return new CodelistNotFoundException(codelistClass, msg);
    }


    private static String getNotFoundMessage(String codelist, DataProvider provider, CodelistEntryMapper mapper) {
        StringBuilder sb = new StringBuilder();
        sb.append("Codelist ").append(codelist).append(" not found\n\n");
        sb.append(mapper.mappingToString(codelist, provider.getClass()));
        return sb.toString();
    }


    private static String getNotFoundMessage(Class<? extends CodelistEntry> codelistClass, DataProvider provider,
            CodelistEntryMapper mapper) {
        StringBuilder sb = new StringBuilder();
        sb.append("Codelist ").append(codelistClass.getSimpleName()).append(" not found\n\n");
        sb.append(mapper.mappingToString(codelistClass, provider.getClass()));
        return sb.toString();
    }
}
