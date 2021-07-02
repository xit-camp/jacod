package camp.xit.jacod.model;

import camp.xit.jacod.Embeddable;

@Embeddable
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


    public Address() {
    }


    public Address(String street, String referenceNumber, String registerNumber, String zipCode, String city) {
        this.street = street;
        this.referenceNumber = referenceNumber;
        this.registerNumber = registerNumber;
        this.zipCode = zipCode;
        this.city = city;
    }


    public String getStreet() {
        return street;
    }


    public void setStreet(String street) {
        this.street = street;
    }


    public String getReferenceNumber() {
        return referenceNumber;
    }


    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }


    public String getRegisterNumber() {
        return registerNumber;
    }


    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }


    public String getZipCode() {
        return zipCode;
    }


    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }


    public String getCity() {
        return city;
    }


    public void setCity(String city) {
        this.city = city;
    }


    @Override
    public String toString() {
        return "Address{" + "street=" + street + ", referenceNumber=" + referenceNumber
                + ", registerNumber=" + registerNumber + ", zipCode=" + zipCode + ", city=" + city + '}';
    }
}
