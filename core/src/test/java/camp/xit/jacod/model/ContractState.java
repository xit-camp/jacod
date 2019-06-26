package camp.xit.jacod.model;

import camp.xit.jacod.model.CodelistEnum;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.EntryRef;
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
