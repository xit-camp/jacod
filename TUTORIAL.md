
# JACOD - Java Codelist API Tutorial

## Installation

**Using maven**

```xml
<dependency>
    <groupId>camp.xit.jacod</groupId>
    <artifactId>jacod-bom</artifactId>
    <version>${jacod.version}</version>
</dependency>
```

## Usage

### Base Codelist (no entry class defined)

```java
CodelistClient cl = new CodelistClient.Builder()
        .withDataProvider(new CSVDataProvider())
        .build();

Codelist title = cl.getCodelist("Title");
title.getEntry("DrSC.");
title.stream()
    .filter(e -> e.getCode().contains("Dr"))
    .forEach(System.out::println);
```

### Extended codelist

```java
@Getter
@Setter
@ToString(callSuper = true)
public class Title extends CodelistEntry {

    public enum Position {
        BEFORE, AFTER
    }

    private Position position;


    public Title() {
    }


    public Title(String code) {
        super(code);
    }


    public Title(CodelistEnum<Title> codeEnum) {
        super(codeEnum.toString());
    }
}
```

```java
CodelistClient cl = new CodelistClient.Builder()
        .addScanPackages("com.example.model").
        .withDataProvider(new CSVDataProvider())
        .build();

Codelist<Title> aps = cl.getCodelist(Title.class);
```

### Codelist references

Codelist entry can reference other codelists e.g.:

```java
public class PresentedPaperSection extends CodelistEntry {

    private PaperType paperType;
}
```

If codelist reference is base codelist without entry class, you have to use @EntryRef annotation to define codelist name.

Example:

```java
public class InsuranceProduct extends CodelistEntry {


    @EntryRef("InsuranceCompany")
    private CodelistEntry company;
}
```

Same for collection references:

```java
public class InsuranceProduct extends CodelistEntry {


    @EntryRef("InsuranceCompany")
    private List<CodelistEntry> companies;
}
```
### Embedded types

Every extended codelist may define properties of simple types, but also more complex (embedded) types.
Class that define embedded type has contain `@Embeddable` annotation. Embedded type class does not need to
extend CodelistEntry class, but can contain reference to another codelist entry.

Embedded type is only wrapper for some subset of values.

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

## Data Mapping

The library supports multiple data sources. You can use custom field mapping if provided codelist contains different field names than your class representation.
In that case you can use [@EntryMapping](src/main/java/camp/xit/kiwi/codelist/client/EntryMapping.java) annotation.

Example:

```java
@EntryMapping(provider = CSVDataProvider.class, value = {
    @EntryFieldMapping(field = "code", mappedField = "ID"),
    @EntryFieldMapping(field = "name", mappedField = "DESCRIPTION"),
    @EntryFieldMapping(field = "days", mappedField = "DAYS")
})
public class PaymentDeferment extends CodelistEntry {

    private Integer days;
}
```
[@EntryMapping](src/main/java/camp/xit/kiwi/codelist/client/EntryMapping.java) annotation can be also used outside of class that defines codelist entry class:

```java
@EntryMapping(provider = CrafterDataProvider.class, entryClass=PaymentDeferment.class, value = {
    @EntryFieldMapping(field = "code", mappedField = "ID"),
    @EntryFieldMapping(field = "name", mappedField = "DESCRIPTION"),
    @EntryFieldMapping(field = "days", mappedField = "DAYS")
})
class PaymentDefermentMapping {}
```

To overwrite mapping of codelist without custom class definition use [@BaseEntryMapping](src/main/java/camp/xit/kiwi/codelist/client/BaseEntryMapping.java) annotation:

```java
import camp.xit.jacod.BaseEntryMapping;
import camp.xit.jacod.EntryFieldMapping;
import camp.xit.kiwi.jacod.provider.spin.SpinDataProvider;

@BaseEntryMapping(codelist = "LoanType", provider = CSVDataProvider.class, resourceName = "LOANTYPE", fields = {
    @EntryFieldMapping(field = "name", mappedField = "TITLE")
})
public interface LoanType {
}
```

### Data Provider

To write custom data provider you have to implement [DataProvider](src/main/java/camp/xit/kiwi/codelist/provider/DataProvider.java) interface. Example of simple data provider is [CSVDataProvider](-   /data/home/hlavki/develop/xit/projects/jacod/providers/csv/src/main/java/camp/xit/jacod/provider/csv/CSVDataProvider.java)

### Enumerations

You can use enumeration in JACOD in 2 ways:
1. Define fields with enumeration types
1. Define enumerated codelists

#### Enumerated Codelists

You must define custom entry class to use enumerated codelists. For more information look in [tests](src/test/java/camp/xit/kiwi/codelist/client/CodelistEnumTest.java).

Example:

```java
public class ContractState extends CodelistEntry {

    public enum States implements CodelistEnum<ContractState> {
        ACTIVE, INACTIV, IN_PROGRESS, XNA
    }
}
```
