package camp.xit.jacod.model;

import camp.xit.jacod.Embeddable;
import java.util.List;

@Embeddable
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


    public List<String> getEmails() {
        return emails;
    }


    public void setEmails(List<String> emails) {
        this.emails = emails;
    }


    public List<String> getMobiles() {
        return mobiles;
    }


    public void setMobiles(List<String> mobiles) {
        this.mobiles = mobiles;
    }


    public List<String> getTelephones() {
        return telephones;
    }


    public void setTelephones(List<String> telephones) {
        this.telephones = telephones;
    }


    @Override
    public String toString() {
        return "ContactInfo{" + "emails=" + emails + ", mobiles=" + mobiles + ", telephones=" + telephones + '}';
    }
}
