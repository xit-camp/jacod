# XLXS Codelist Data Provider
![coverage](https://code.xit.camp/kiwi/cfg-mng/codelist-xlsx-provider/badges/master/build.svg)

## Create Release

```bash
mvn clean release:prepare release:perform -DpushChanges=false -DlocalCheckout=true -Darguments='-Dmaven.javadoc.failOnError=false -Dmaven.deploy.skip=true -Dmaven.site.skip=true'
```

## Synchronizácia z GDrive

```bash
curl -v -X PUT http://contract.dev.pbpartner.lan/spin/xlsx/update
```
