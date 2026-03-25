package camp.xit.jacod.model;

public class PaymentDeferment extends CodelistEntry {

    private Integer months;


    public PaymentDeferment() {
    }


    public PaymentDeferment(String code) {
        super(code);
    }


    public PaymentDeferment(CodelistEnum<PaymentDeferment> codeEnum) {
        super(codeEnum.toString());
    }


    public PaymentDeferment(String code, Integer months) {
        super(code);
        this.months = months;
    }


    public Integer getMonths() {
        return months;
    }


    public void setMonths(Integer months) {
        this.months = months;
    }


    @Override
    public String toString() {
        return "PaymentDeferment{" + super.toStringAttrs() + ", months=" + months + '}';
    }
}
