package camp.xit.jacod.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class BonusType extends CodelistEntry {

    /**
     * Minimálny počet splátok
     */
    private Integer instalmentCountFrom;
    /**
     * Minimálna výška úveru
     */
    private BigDecimal loanAmountFrom;
    /**
     * Minimálna kúpna cena tovaru / služieb
     */
    private BigDecimal goodsPriceFrom;


    public BonusType() {
    }


    public BonusType(String code) {
        super(code);
    }


    public BonusType(CodelistEnum<BonusType> codeEnum) {
        super(codeEnum);
    }
}
