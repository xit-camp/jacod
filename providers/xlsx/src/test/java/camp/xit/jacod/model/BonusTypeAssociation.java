package camp.xit.jacod.model;

import camp.xit.jacod.Embeddable;
import java.time.LocalDate;

@Embeddable
public class BonusTypeAssociation {

    private BonusType bonusType;
    private LocalDate validFrom;
    private LocalDate validTo;


    public BonusTypeAssociation() {
    }


    public BonusTypeAssociation(BonusType bonusType, LocalDate validFrom, LocalDate validTo) {
        this.bonusType = bonusType;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }


    public BonusType getBonusType() {
        return bonusType;
    }


    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }


    public LocalDate getValidFrom() {
        return validFrom;
    }


    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }


    public LocalDate getValidTo() {
        return validTo;
    }


    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }


    @Override
    public String toString() {
        return "BonusTypeAssociation{" + "bonusType=" + bonusType + ", validFrom=" + validFrom + ", validTo=" + validTo + '}';
    }
}
