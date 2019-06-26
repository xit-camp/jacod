# Kiwi Codelist client API

## Inštalácia

**Using maven**

```xml
<dependency>
    <groupId>camp.xit.codelist</groupId>
    <artifactId>codelist-client</artifactId>
    <version>1.0</version>
</dependency>
```

## Použitie

### Základný číselník

```java
CodelistClient cl = new CodelistClient.Builder().getClient();
Codelist title = cl.getCodelist("MarketingMark");
title.getEntry("DrSC.");
title.stream().filter(e -> e.getCode().contains("Dr")).forEach(System.out::println);
```

### Odvodený číselník

```java
CodelistClient cl = new CodelistClient.Builder().getClient();
Codelist<ApplicationProcessingStep> aps = cl.getCodelist(ApplicationProcessingStep.class);
```

### Vytvorenie odvodeného číselníka

Číselník je definovaný triedou [CodelistEntry](src/main/java/camp/xit/kiwi/codelist/client/model/CodelistEntry.java).
Každý odvodený číselník musí dediť od tejto triedy.

Napr.

```java
public class PresentedPaperSection extends CodelistEntry {

    private Boolean allMandatory;
    private String description;
}
```

Ak číselník definuje referenciu na iný číselník, ktorý je základný čiselník, tak je potrebné definovať referenciu
na tento číselník pomocou anotacie `@EntryRef`.

Napr.

```java
public class InsuranceProduct extends CodelistEntry {


    @EntryRef("InsuranceCompany")
    private CodelistEntry company;
}
```

Toto pravidlo platí aj pre všetky referencie typu kolekcia. Napr.

```java
public class InsuranceProduct extends CodelistEntry {


    @EntryRef("InsuranceCompany")
    private List<CodelistEntry> companies;
}
```
### Vložené objekty

Každý odvodený číselník môže definovať "vložené" objekty. Trieda definujúca vložený objekt, musí deklarovať anotáciu
`@Embeddable`. Vložený objekt nemusí dediť od triedy CodelistEntry a môže asociovať iný vložený objekt resp. číselník.
Data zo zdrojového systému potom musia obsahovať klúče podla prefixu nadradenej property.
Z nižšie uvedeného príkladu napr. crafter zdrojový systém bude mať xml element `companyBusinessAdressStreet`.


```java
public class BusinessPlace extends CodelistEntry {

    private LegalSubject company;
}
```

```java
@Embeddable
public class LegalSubject {

    private String name;
    private String ico;
    private String dic;
    private String icDph;
    private String centralRegister;
    private Boolean taxPayer;
    private Address businessAddress;
}

@Embeddable
public class Address {

    private String street;
    private String referenceNumber;
    private String zipCode;
    private String registerNumber;
    private String city;
    private String displayValue;
}
```

Systém podporuje viacero zdrojov dát. Ak daný čiselník používa iný zdrojový systém, ktorý má ine názvy polí,
je možné prepísať východzie mapovanie z triedy [CodelistEntry](src/main/java/camp/xit/kiwi/codelist/client/model/CodelistEntry.java)
pomocou anotácie [EntryMapping](src/main/java/camp/xit/kiwi/codelist/client/EntryMapping.java).

Príklad:

```java
@EntryMapping(provider = CrafterDataProvider.class, value = {
    @EntryFieldMapping(field = "code", mappedField = "ID"),
    @EntryFieldMapping(field = "name", mappedField = "DESCRIPTION"),
    @EntryFieldMapping(field = "days", mappedField = "DAYS")
})
public class PaymentDeferment extends CodelistEntry {

    private Integer days;
}
```

Anotáciu `@EntryMapping` je možné vložiť aj mimo samotnej tredy definujúcej číselník napr:

```java
@EntryMapping(provider = CrafterDataProvider.class, entryClass=PaymentDeferment.class, value = {
    @EntryFieldMapping(field = "code", mappedField = "ID"),
    @EntryFieldMapping(field = "name", mappedField = "DESCRIPTION"),
    @EntryFieldMapping(field = "days", mappedField = "DAYS")
})
class PaymentDefermentMapping {}
```

### Zdrojový systém

Každý zdrojový systém musí implementovať interface [DataProvider](src/main/java/camp/xit/kiwi/codelist/provider/DataProvider.java).
Momentálne je možné definovať maximálne jednu implementáciu zdrojového systému.

### Enumerácie

Je možné definovať enumeráciu pre daný číselník a to tak, že projekt, ktorý konzumuje toto API, si vytvorí enumeračnú triedu zodpovedajúcu požiadavkam. Najlepší zdroj príkladov sú [junit testy](src/test/java/camp/xit/kiwi/codelist/client/CodelistEnumTest.java). Pre každú enumeračnú triedu musí existovať odvodený číselník. Pre implicitne odvodené číselníky nie je potrebné definovať custom triedy.

#### Príklad použitia

```java
public class ContractState extends CodelistEntry {

    public enum States implements CodelistEnum<ContractState> {
        ACTIVE, INACTIV, INPROGRESS, XNA
    }
}
```

```java
public enum InsuranceProducts implements CodelistEnum<InsuranceProduct> {
    XSELL_A, XSELL_B, XNA, NONE
}
```

#### Použitie API

Použitie API je v oboch prípadoch rovnaké:

Potom môžeš použiť priamo [CodelistClient](src/main/java/camp/xit/kiwi/codelist/client/CodelistClient.java):

```java
CodelistClient cl = new CodelistClient.Builder().getClient();
ContactState activeState = cl.getEntry(ContactState.States.ACTIVE);
```

alebo metôdu triedy [Codelist](src/main/java/camp/xit/kiwi/codelist/client/model/Codelist.java)

```java
CodelistClient cl = new CodelistClient.Builder().getClient();
Codelist<ContractState> csc = cl.getCodelist(ContractState.class);
scs.getEntry(ContractState.States.INACTIV);
```

resp.

Potom môžeš použiť priamo [CodelistClient](src/main/java/camp/xit/kiwi/codelist/client/CodelistClient.java):

```java
CodelistClient cl = new CodelistClient.Builder().getClient();
InsuranceProduct xsellA = cl.getEntry(InsuranceProducts.XSELL_A);
```

alebo metôdu triedy [Codelist](src/main/java/camp/xit/kiwi/codelist/client/model/Codelist.java)

```java
CodelistClient cl = new CodelistClient.Builder().getClient();
Codelist<InsuranceProduct> ipc = cl.getCodelist(InsuranceProduct.class);
InsuranceProduct xsellA = ipc.getEntry(InsuranceProducts.XSELL_A);
```

## Create Release

mvn clean release:prepare release:perform -DpushChanges=false -DlocalCheckout=true -Darguments='-Dmaven.javadoc.failOnError=false -Dmaven.deploy.skip=true -Dmaven.site.skip=true'