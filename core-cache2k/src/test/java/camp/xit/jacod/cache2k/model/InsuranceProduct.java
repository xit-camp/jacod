package camp.xit.jacod.cache2k.model;

import camp.xit.jacod.EntryRef;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.CodelistEnum;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class InsuranceProduct extends CodelistEntry {

    public enum Type {
        A, B, NONE
    }

    private Type type;

    @EntryRef("InsuranceCompany")
    private CodelistEntry company;
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
}
