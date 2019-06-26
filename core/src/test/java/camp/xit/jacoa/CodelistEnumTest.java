package camp.xit.jacoa;

import camp.xit.jacoa.CodelistClient;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.ContractState;
import camp.xit.jacoa.model.InsuranceProduct;
import camp.xit.jacoa.test.CodelistClientExtension;
import camp.xit.jacoa.test.CodelistClientExtension.CsvClient;
import camp.xit.jacoa.test.model.ContractStates;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CodelistClientExtension.class)
public class CodelistEnumTest {

    @Test
    void custom(@CsvClient CodelistClient client) {
        ContractState activeState = client.getEntry(ContractStates.INACTIV);
        assertThat(activeState.getCode(), is(ContractStates.INACTIV.toString()));

        Codelist<ContractState> csc = client.getCodelist(ContractState.class);
        assertThat(csc.getEntry(ContractStates.INACTIV), is(activeState));
    }


    @Test
    void advanced(@CsvClient CodelistClient client) {
        InsuranceProduct ipa = client.getEntry(InsuranceProducts.A_02);
        assertThat(ipa.getCode(), is(InsuranceProducts.A_02.toString()));
        assertNotNull(ipa.getRate());
        assertThat(ipa.getRate().toString(), is("0.3"));

        Codelist<InsuranceProduct> ipc = client.getCodelist(InsuranceProduct.class);
        assertThat(ipc.getEntry(InsuranceProducts.A_02).getRate(), is(ipa.getRate()));
    }
}
