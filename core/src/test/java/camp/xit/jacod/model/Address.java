package camp.xit.jacod.model;

import camp.xit.jacod.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String street;
    private String referenceNumber;
    private String registerNumber;
    private String zipCode;
    private String city;


    public String getDisplayValue() {
        return AddressUtil.address(street, registerNumber, referenceNumber, city);
    }


    public String getPostalValue() {
        return AddressUtil.address(street, registerNumber, referenceNumber, city, zipCode);
    }
}
