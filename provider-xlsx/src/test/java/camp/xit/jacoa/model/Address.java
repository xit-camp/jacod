package camp.xit.jacoa.model;

import camp.xit.jacoa.Embeddable;
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

    /**
     * Ulica
     */
    private String street;
    /**
     * Orientačné číslo domu
     */
    private String referenceNumber;
    /**
     * Súpisné číslo domu
     */
    private String registerNumber;
    /**
     * PSČ (bez medzier)
     */
    private String zipCode;
    /**
     * Mesto
     */
    private String city;
}
