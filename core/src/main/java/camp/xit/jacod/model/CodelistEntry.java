package camp.xit.jacod.model;

import camp.xit.jacod.BaseEntry;
import camp.xit.jacod.DateUtil;
import camp.xit.jacod.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Základná trieda číselníkovej hodnoty. Číselník je definovaný množinou týchto hodnôt.
 */
@Getter
@Setter
@BaseEntry
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CodelistEntry implements Serializable {

    private static final long serialVersionUID = 1L;

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


    public CodelistEntry(String code) {
        this.code = code;
    }


    public CodelistEntry(CodelistEnum<? extends CodelistEntry> codeEnum) {
        this(codeEnum.toString());
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


    @Override
    public String toString() {
        return "CodelistEntry{" + toStringAttrs() + '}';
    }


    public String toStringAttrs() {
        return "code=" + code + ", name=" + name + ", order=" + order + ", validFrom="
                + validFrom + ", validTo=" + validTo + ", selected=" + selected;
    }
}
