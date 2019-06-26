package camp.xit.jacoa.model;

import camp.xit.jacoa.Embeddable;
import java.time.LocalDate;
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
public class BonusTypeAssociation {

    private BonusType bonusType;
    private LocalDate validFrom;
    private LocalDate validTo;
}
