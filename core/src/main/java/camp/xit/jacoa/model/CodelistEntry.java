package camp.xit.jacoa.model;

import camp.xit.jacoa.DateUtil;
import camp.xit.jacoa.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Základná trieda číselníkovej hodnoty. Číselník je definovaný množinou týchto hodnôt.
 *
 */
@Getter
@Setter
@ToString
public class CodelistEntry {

    /**
     * Jednoznačný identifikátor číselníkovej hodnoty. Je zároveň aj primárny kľúč.
     *
     * @documentationExample DATA_TO_SRBI
     */
    @NotNull
    private String code;
    /**
     * Textový popis hodnoty
     *
     * @documentationExample Súhlasím s poskytovaním údajov do Spoločného registra bankových informácií
     */
    @NotNull
    private String name;
    /**
     * Hodnota určujúca poradie napr. na UI. Ak aplikácia potrebuje iné radenie, musí si toto zabezpečiť sama.
     *
     * @documentationExample 10
     */
    private Integer order;
    /**
     * Dátum určujúci validitu hodnoty od
     *
     * @documentationExample 1900-01-01
     */
    @NotNull
    private LocalDate validFrom;
    /**
     * Dátum určujúci validitu hodnoty do
     *
     * @documentationExample 9999-12-31
     */
    @NotNull(defaultValue = "9999-12-31")
    private LocalDate validTo;
    /**
     * Default hodnota pri zobrazení číselníka
     */
    private Boolean selected;


    public CodelistEntry() {
    }


    public CodelistEntry(String code) {
        this.code = code;
    }


    public CodelistEntry(CodelistEnum<? extends CodelistEntry> codeEnum) {
        this(codeEnum.toString());
    }


    public CodelistEntry(String code, String name, Integer order, LocalDate validFrom, LocalDate validTo, Boolean selected) {
        this.code = code;
        this.name = name;
        this.order = order;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.selected = selected;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.code);
        return hash;
    }


    public boolean equals(CodelistEnum<? extends CodelistEntry> code) {
        if (code == null) {
            return false;
        }
        return Objects.equals(this.code, String.valueOf(code));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CodelistEntry other = (CodelistEntry) obj;
        return Objects.equals(this.code, other.code);
    }


    public boolean isValid() {
        return DateUtil.isValid(validFrom, validTo);
    }
}
