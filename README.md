# JACOD - Java Codelist API

Single purpose java library to provide first class support for business domain of codelists. Almost every business oriented project needs codelists. Mostly read only data provided across every application component. JACOD provides these features:
* Easy map flat data to java objects
* Easy access to codelist (CodelistClient API)
* Caching for instant access to data
* Easy Spring Boot integration (example soon)

JACOD provides API to handle any datasource. It works primary with flat data, so you can map from e.g. CSV, Excel, Google Sheet, JDBC etc. Of course you can also write your own DataProvider or merge data from multiple providers.

## Tutorial

### Basic example

Try simple example:
Let's say we have `LoanType.csv` CSV file:
```
code, name, validFrom
MORTGAGE, Mortgage Loan, 2019-01-01
CREDIT, Credit Loan, 2019-01-01
STUDENT, Student Loan, 2019-01-01
```
Then we can write simple client:
```java
import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.xlsx.CSVDataProvider;

public class Main {

    public static void main(String[] args) throws Exception {
        CodelistClient client = new CodelistClient.Builder()
                .withExpiryTime(1, TimeUnit.HOURS)
                .withDataProvider(new CSVDataProvider("src/csv")).build();

        Codelist<? extends CodelistEntry> loanType = client.getCodelist("LoanType");
        loanType.forEach(System.out::println);
    }
}
```
Perfect, now we have cached codelist data, that will be refreshed every 1 hour and output will be:
```
CodelistEntry(code=CREDIT, name=Credit Loan, order=null, validFrom=2019-01-01, validTo=9999-12-31, selected=null)
CodelistEntry(code=STUDENT, name=Student Loan, order=null, validFrom=2019-01-01, validTo=9999-12-31, selected=null)
CodelistEntry(code=MORTGAGE, name=Mortgage Loan, order=null, validFrom=2019-01-01, validTo=9999-12-31, selected=null)
```

But what if I need more complex structure. Let's say we need to add minimal loan rate to LoanType:

```
code, name, validFrom, rate
MORTGAGE, Mortgage Loan, 2019-01-01, 1.2
CREDIT, Credit Loan, 2019-01-01, 8
STUDENT, Student Loan, 2019-01-01, 4.7
```

We need to add class `LoanType` that extends `CodelistEntry`:
```java
package camp.xit.jacod.example;

import camp.xit.jacod.model.CodelistEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanType extends CodelistEntry {

    private Double rate;
}
```
now Main class looks like:
```java
package camp.xit.jacod.example;

import camp.xit.jacod.model.CodelistEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanType extends CodelistEntry {

    private Double rate;
}
````
```java
package camp.xit.jacod.example;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.provider.xlsx.CSVDataProvider;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        CodelistClient client = new CodelistClient.Builder()
                .addScanPackages("camp.xit.jacod.example")
                .withExpiryTime(1, TimeUnit.HOURS)
                .withDataProvider(new CSVDataProvider("src/csv")).build();

        LoanType credit = client.getEntry(LoanType.class, "CREDIT");
        assert(credit.getRate() == 8d);
    }
}
```
### Referencing between codelists

Let's say we have 2 codelists: Bank and LoanType. Every bank provide different set of loans.

So we have `LoanType.csv`:
```
code, name, validFrom
MORTGAGE, Mortgage Loan, 2019-01-01
CREDIT, Credit Loan, 2019-01-01
STUDENT, Student Loan, 2019-01-01
```
and `Bank.csv`:
```
code, name, validFrom, loanTypes
BANK_OF_AMERICA, Bank of America, 2019-01-01, STUDENT
JP_MORGAN, JP Morgan, 2019-01-01, "MORTGAGE, CREDIT"
CHASE_BANK, Chase Bank, 2019-01-01, "STUDENT, CREDIT"
```
`Bank.java`
```java
@Getter
@Setter
public class Bank extends CodelistEntry {

    @EntryRef("LoanType")
    private List<CodelistEntry> loanTypes;
}
```
Then we can access Banks:
```java
package camp.xit.jacod.example;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.provider.xlsx.CSVDataProvider;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        CodelistClient client = new CodelistClient.Builder()
                .addScanPackages("camp.xit.jacod.example")
                .withExpiryTime(1, TimeUnit.HOURS)
                .withDataProvider(new CSVDataProvider("src/csv")).build();

        Codelist<Bank> banks = client.getCodelist(Bank.class);
    }
}
```
