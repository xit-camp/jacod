package camp.xit.jacod.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
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
}
