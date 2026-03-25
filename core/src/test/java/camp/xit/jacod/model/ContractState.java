package camp.xit.jacod.model;

import camp.xit.jacod.EntryRef;

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


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public CodelistEntry getStateToAgent() {
        return stateToAgent;
    }


    public void setStateToAgent(CodelistEntry stateToAgent) {
        this.stateToAgent = stateToAgent;
    }


    public CodelistEntry getStateToCustomer() {
        return stateToCustomer;
    }


    public void setStateToCustomer(CodelistEntry stateToCustomer) {
        this.stateToCustomer = stateToCustomer;
    }


    @Override
    public String toString() {
        return "ContractState{" + super.toStringAttrs() + ", description=" + description
                + ", stateToAgent=" + stateToAgent + ", stateToCustomer=" + stateToCustomer + '}';
    }
}
