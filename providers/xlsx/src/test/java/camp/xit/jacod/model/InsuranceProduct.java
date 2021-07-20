package camp.xit.jacod.model;

import camp.xit.jacod.EntryRef;
import java.math.BigDecimal;
import java.util.List;

public class InsuranceProduct extends CodelistEntry {

    public enum Type {
        A, B, NONE
    }

    private Type type;

    @EntryRef("InsuranceCompany")
    private CodelistEntry company;
    /**
     * Poistná sadzba
     */
    private BigDecimal rate;
    private Integer ageFrom;
    private Integer ageTo;
    private Integer ageAtRepayment;
    private String description;
    @EntryRef("InsuranceCalculationType")
    private CodelistEntry insuranceCalculationType;

    @EntryRef("IncomeSource")
    private List<CodelistEntry> applicableIncomeSources;


    public InsuranceProduct() {
    }


    public InsuranceProduct(String code) {
        super(code);
    }


    public InsuranceProduct(CodelistEnum<InsuranceProduct> codeEnum) {
        super(codeEnum.toString());
    }


    public InsuranceProduct(String code, Type type) {
        super(code);
        this.type = type;
    }


    public Type getType() {
        return type;
    }


    public void setType(Type type) {
        this.type = type;
    }


    public CodelistEntry getCompany() {
        return company;
    }


    public void setCompany(CodelistEntry company) {
        this.company = company;
    }


    public BigDecimal getRate() {
        return rate;
    }


    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }


    public Integer getAgeFrom() {
        return ageFrom;
    }


    public void setAgeFrom(Integer ageFrom) {
        this.ageFrom = ageFrom;
    }


    public Integer getAgeTo() {
        return ageTo;
    }


    public void setAgeTo(Integer ageTo) {
        this.ageTo = ageTo;
    }


    public Integer getAgeAtRepayment() {
        return ageAtRepayment;
    }


    public void setAgeAtRepayment(Integer ageAtRepayment) {
        this.ageAtRepayment = ageAtRepayment;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public CodelistEntry getInsuranceCalculationType() {
        return insuranceCalculationType;
    }


    public void setInsuranceCalculationType(CodelistEntry insuranceCalculationType) {
        this.insuranceCalculationType = insuranceCalculationType;
    }


    public List<CodelistEntry> getApplicableIncomeSources() {
        return applicableIncomeSources;
    }


    public void setApplicableIncomeSources(List<CodelistEntry> applicableIncomeSources) {
        this.applicableIncomeSources = applicableIncomeSources;
    }


    @Override
    public String toString() {
        return "InsuranceProduct{" + "type=" + type + ", company=" + company + ", rate=" + rate
                + ", ageFrom=" + ageFrom + ", ageTo=" + ageTo + ", ageAtRepayment=" + ageAtRepayment
                + ", description=" + description + ", insuranceCalculationType=" + insuranceCalculationType
                + ", applicableIncomeSources=" + applicableIncomeSources + '}';
    }
}
