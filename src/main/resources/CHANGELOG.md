# Changelog
## 1.0.4 (2021-09-07)
### core
* `Main`: **optimized** Plug-in management
* `Ped` : **added** SEX_MALE, SEX_FEMALE, PHENO_UNAFFECTED, PHENO_AFFECTED values
* `VCF` : **fixed** count of read/filtered variants in AtomicInteger (multi-threading and i++ don't mix)
* `VEPAnnotation``` : **fixed** when a key is missing from the VEP annotation, the warning is only displayed once. If debug mode is activated, print stacktrace
### functions
* `MaleFemale` : **added** Functions that shows (among other information) male/female AF
* `MergeVQSR` : **added** Functions that Merges SNP and INDEL results files from VQSR
* `QC1078` : **renamed** to ```QC```
### filters
* `SampleFilter` : Authorize VCF without samples
### other

## 1.0.3 (2020-12-09)
### core
### functions
* ```QC1078``` : **fixed** GitHub.Issues#2 non HQ variants were set to missing, so HQrate was equal to callrate. HQ parameter is removed (and now equal to callrate)
* ```QC1078``` : **added** AB check for genotypes. Failed ab check will set genotype to missing.
### filters
### other
* ```getCustomRequirement``` : **fixed** typo in method name

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


