# Changelog

## 1.0.2 (2020-11-05)
### core
### functions
* ```AddAlleleBalance``` : **fixed** GitHub.Issues#1 [`AB` field not added for missing genotypes] 
### filters
### other

## 1.0.1 (2020-09-30)
### core
### functions
* ```CoverageStats``` : **added**
* ```GzPaste``` : **changed** to use ```LineBuilder```
### filters
### other
* ```Gradle``` : **added**

## 1.0.0 (2020-09-24)
### core
* ```Fasta.getCharactersAt(String chromosome, long position, int length)``` : **fixed** to be thread safe
* ```VEPConsequence``` : **changed** value index starts at 0 (for UNKNOWN) instead of -1
### functions
* ```CheckReference``` : **added** check for unexpected alleles (```'\n'``` etc...)
* ```CheckReference``` : **added** testing script
* ```CommonGenotypes``` : **moved** to plugin Jar
* ```CountInGnomAD``` : **moved** to plugin Jar
* ```FrequencyCorrelation``` : **changed** ```VEPConsequence``` index starts at 0 instead of -1
* ```GenerateHomoRefForPosition``` : **added** testing script
* ```Kappa``` : **added** testing script
* ```NumberOfCsqPerGene``` : **changed** ```VEPConsequence``` index starts at 0 instead of -1
* ```PrivateVSPanel``` : **added** testing script
* ```VCFToReference``` : **added** testing script
### filters
### other
* ```CommonLib``` : **fixed** ```LineBuilder```
* ```Main.getVersion()``` : **changed** to read this CHANGELOG file
* ```Main.main(String[] args)``` : **moved** to Run to enforce ```assert```
* ```FileParameter``` : **fixed** ```getExtension()``` returns ```String[]{}``` instead of ```null``` by default
* ```Documentation``` : **added** versioning


