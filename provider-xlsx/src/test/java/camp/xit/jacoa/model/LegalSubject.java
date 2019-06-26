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
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LegalSubject {

    private String name;
    private String ico;
    private String dic;
    private String icDph;
    private String centralRegister;
    private Boolean taxPayer;
    private Address businessAddress;
}
