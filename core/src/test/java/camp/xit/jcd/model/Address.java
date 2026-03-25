package camp.xit.jcd.model;

import camp.xit.jacod.annotation.Embeddable;

@Embeddable
public class Address {

    private String street;
    private String referenceNumber;
    private String registerNumber;
    private String zipCode;
    private String city;


    public Address() {
    }


    public Address(String street, String referenceNumber, String registerNumber, String zipCode, String city) {
        this.street = street;
        this.referenceNumber = referenceNumber;
        this.registerNumber = registerNumber;
        this.zipCode = zipCode;
        this.city = city;
    }


    public String getDisplayValue() {
        return AddressUtil.address(street, registerNumber, referenceNumber, city);
    }


    public String getPostalValue() {
        return AddressUtil.address(street, registerNumber, referenceNumber, city, zipCode);
    }


    @Override
    public String toString() {
        return "Address{" + "street=" + street + ", referenceNumber=" + referenceNumber
                + ", registerNumber=" + registerNumber + ", zipCode=" + zipCode + ", city=" + city + '}';
    }

}
