package camp.xit.jacod.model;

import java.math.BigDecimal;

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


    public Integer getInstalmentCountFrom() {
        return instalmentCountFrom;
    }


    public void setInstalmentCountFrom(Integer instalmentCountFrom) {
        this.instalmentCountFrom = instalmentCountFrom;
    }


    public BigDecimal getLoanAmountFrom() {
        return loanAmountFrom;
    }


    public void setLoanAmountFrom(BigDecimal loanAmountFrom) {
        this.loanAmountFrom = loanAmountFrom;
    }


    public BigDecimal getGoodsPriceFrom() {
        return goodsPriceFrom;
    }


    public void setGoodsPriceFrom(BigDecimal goodsPriceFrom) {
        this.goodsPriceFrom = goodsPriceFrom;
    }


    @Override
    public String toString() {
        return "BonusType{" + super.toStringAttrs() + ", instalmentCountFrom=" + instalmentCountFrom
                + ", loanAmountFrom=" + loanAmountFrom + ", goodsPriceFrom=" + goodsPriceFrom + '}';
    }
}
