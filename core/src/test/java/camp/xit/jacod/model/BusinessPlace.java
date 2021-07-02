package camp.xit.jacod.model;

import java.util.List;

public class BusinessPlace extends CodelistEntry {

    private List<BonusType> allowedBonuses;
    private Address businessAddress;
    private LegalSubject company;


    public List<BonusType> getAllowedBonuses() {
        return allowedBonuses;
    }


    public void setAllowedBonuses(List<BonusType> allowedBonuses) {
        this.allowedBonuses = allowedBonuses;
    }


    public Address getBusinessAddress() {
        return businessAddress;
    }


    public void setBusinessAddress(Address businessAddress) {
        this.businessAddress = businessAddress;
    }


    public LegalSubject getCompany() {
        return company;
    }


    public void setCompany(LegalSubject company) {
        this.company = company;
    }


    @Override
    public String toString() {
        return "BusinessPlace{" + super.toStringAttrs() + ", allowedBonuses=" + allowedBonuses
                + ", businessAddress=" + businessAddress + ", company=" + company + '}';
    }
}
