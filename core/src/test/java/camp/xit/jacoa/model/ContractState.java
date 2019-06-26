package camp.xit.jacoa.model;

import camp.xit.jacoa.model.CodelistEnum;
import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.EntryRef;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ContractState extends CodelistEntry {

    private String description;

    @EntryRef("ContractStateAgent")
    private CodelistEntry stateToAgent;

    @EntryRef("ContractStateCustomer")
    private CodelistEntry stateToCustomer;


    protected ContractState() {
    }


    public ContractState(String code) {
        super(code);
    }


    public ContractState(CodelistEnum<ContractState> codeEnum) {
        super(codeEnum);
    }
}
