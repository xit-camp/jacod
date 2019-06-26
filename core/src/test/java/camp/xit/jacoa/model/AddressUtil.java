package camp.xit.jacoa.model;

import java.util.StringJoiner;

class AddressUtil {

    private AddressUtil() {
    }


    public static String address(String street, String registerNumber, String referenceNumber, String city) {
        StringJoiner sj = new StringJoiner(" ");

        StringJoiner sj2 = new StringJoiner("/");
        join(sj2, registerNumber);
        join(sj2, referenceNumber);

        join(sj, street);
        join(sj, sj2.toString());
        join(sj, city);

        return sj.toString();
    }


    public static String address(String street, String registerNumber, String referenceNumber, String city, String zipCode) {
        StringJoiner sj = new StringJoiner(" ");

        StringJoiner sj2 = new StringJoiner("/");
        join(sj2, registerNumber);
        join(sj2, referenceNumber);

        join(sj, street);
        join(sj, sj2.toString() + ",");
        join(sj, city);
        join(sj, zipCode);

        return sj.toString();
    }


    /**
     * doplní {@code newElement} do {@code stringJoiner}, ak nieje prázdny alebo {@code null}
     *
     * @param stringJoiner
     * @param strValue
     * @return
     */
    private static StringJoiner join(StringJoiner stringJoiner, String strValue) {
        if (strValue != null && !strValue.isBlank()) {
            stringJoiner.add(strValue);
        }
        return stringJoiner;
    }
}
