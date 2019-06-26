package camp.xit.jacoa.model;

import camp.xit.jacoa.Embeddable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {

    /**
     * Email
     */
    private List<String> emails;
    /**
     * Mobilné číslo
     */
    private List<String> mobiles;
    /**
     * Telefón
     */
    private List<String> telephones;


    public ContactInfo(String email, String mobile, String telephone) {
        this.emails = email != null && !email.isBlank() ? List.of(email) : null;
        this.mobiles = mobile != null && !mobile.isBlank() ? List.of(mobile) : null;
        this.telephones = telephone != null && !telephone.isBlank() ? List.of(telephone) : null;
    }


    @Deprecated
    public String getEmail() {
        return emails != null && !emails.isEmpty() ? emails.get(0) : null;
    }


    @Deprecated
    public String getMobile() {
        return mobiles != null && !mobiles.isEmpty() ? mobiles.get(0) : null;
    }


    @Deprecated
    public String getTelephone() {
        return telephones != null && !telephones.isEmpty() ? telephones.get(0) : null;
    }
}
