package camp.xit.jacod.model;

import camp.xit.jacod.model.CodelistEntry;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class BusinessPlace extends CodelistEntry {

    private List<BonusType> allowedBonuses;
    private Address businessAddress;
    private LegalSubject company;
}
