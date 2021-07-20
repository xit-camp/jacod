package camp.xit.jacod.model;

import camp.xit.jacod.Embeddable;

@Embeddable
public class LegalSubject {

    private String name;
    private String ico;
    private String dic;
    private String icDph;
    private String centralRegister;
    private Boolean taxPayer;
    private Address businessAddress;


    public LegalSubject() {
    }


    public LegalSubject(String name, String ico, String dic, String icDph, String centralRegister, Boolean taxPayer, Address businessAddress) {
        this.name = name;
        this.ico = ico;
        this.dic = dic;
        this.icDph = icDph;
        this.centralRegister = centralRegister;
        this.taxPayer = taxPayer;
        this.businessAddress = businessAddress;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getIco() {
        return ico;
    }


    public void setIco(String ico) {
        this.ico = ico;
    }


    public String getDic() {
        return dic;
    }


    public void setDic(String dic) {
        this.dic = dic;
    }


    public String getIcDph() {
        return icDph;
    }


    public void setIcDph(String icDph) {
        this.icDph = icDph;
    }


    public String getCentralRegister() {
        return centralRegister;
    }


    public void setCentralRegister(String centralRegister) {
        this.centralRegister = centralRegister;
    }


    public Boolean getTaxPayer() {
        return taxPayer;
    }


    public void setTaxPayer(Boolean taxPayer) {
        this.taxPayer = taxPayer;
    }


    public Address getBusinessAddress() {
        return businessAddress;
    }


    public void setBusinessAddress(Address businessAddress) {
        this.businessAddress = businessAddress;
    }


    @Override
    public String toString() {
        return "LegalSubject{" + "name=" + name + ", ico=" + ico + ", dic=" + dic + ", icDph=" + icDph
                + ", centralRegister=" + centralRegister + ", taxPayer=" + taxPayer + ", businessAddress=" + businessAddress + '}';
    }
}
