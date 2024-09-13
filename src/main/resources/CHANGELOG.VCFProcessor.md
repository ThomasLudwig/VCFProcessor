# Changelog
## 1.2.1 (2024-09-13)
Minor revisions
### core
### functions
- Ignore "*" allele in most analyses
- `FilterF2` : Direct output instead of BufferedOutput (to avoid Out-of-Memory Exception)
### filters
### graphs
- `F2Graph` : Highest value bar at 100% height

## 1.2.0 (2024-07-15)
### core
- `BCF` : **added** possibility to process BCF file (through a call to VCF)
- Extensive Code rewrite to speed up BCF treatments
- `Canonical` : **fixed** Left alignment (the event is always the leftmost possible)
- Java 8 -> 11
### functions
- `ExtractCanonical` : **added** Function that convert a VCF to a list of canonical variant

## 1.1.0 (2024-05-16)
### core
- Extensive Code clean-up
- **removed** GUI
- `Canonical` : **added** overlap()
- `Info` : **added** getFrequency(String key, int allele) where key is given by the calling function instead of being preset
- `VCF` : **fixed** the method `applySampleFilters()` froze instead of dying if there was not enough columns in a line
### functions
- `FilterFrequencies` : **changed** removed limitation to pre-set populations
### filters
- `ABHetMismatch` : **added** Filters heterozygous genotypes that do not have 0.5-dev <= AB <= 0.5+dev

## 1.0.5 (2023-07-13)
### core
- `Genotype` : **added** getPl()
- `Function` : **added** Print memory Heap Size
- `VCFHandling` : **added** MULTIALLELIC_DROP
- `Cannonical` : **added** deserialization from String
- `Cannonical` : **added** isSNP()
- `Genotpye` : **added** setTo(Genotype replacement)
- `Genotpye` : **added** setToMissing()
- `Region` : **fixed** Region had a compareTo, but did not explicitly implement the Comparable interface
- `Region` : **added** Annotation for the region (Extra column of a bed file)
- `Region` : **added** setters for start, end and annotation
- `Region` : **added** boolean includes(Region r) to check if a region is fully included in another one
- `Bed` : **changed** Regions in the Bed file are sorted through SortedList and not by hand anymore
### functions
- `GetQCMetrics` : **added** Function that gets all the Metrics used by the QC function
- `SimulateVCFFromExisting` :**added** Function that Simulates a VCF File from an existing VCF File be mixing genotypes of samples from different ancestries
### filters

## 1.0.4 (2022-08-04)
### core
- `Main`: **optimized** Plug-in management
- `Ped` : **added** SEX_MALE, SEX_FEMALE, PHENO_UNAFFECTED, PHENO_AFFECTED values
- `VCF` : **fixed** count of read/filtered variants in AtomicInteger (multi-threading and i++ don't mix)
- `VEPAnnotation` : **fixed** when a key is missing from the VEP annotation, the warning is only displayed once. If debug mode is activated, print stacktrace
### functions
- `MaleFemale` : **added** Functions that shows (among other information) male/female AF
- `MergeVQSR` : **added** Functions that Merges SNP and INDEL results files from VQSR
- `QC1078` : **renamed** to `QC`
- `FilterGnomADNFEFrequency` : **added** Function that filters on GnomAD NFE frequencies
### filters
- `SampleFilter` : **added** Authorize VCF without samples
- `ISKSVAF` : **added** Filters Heterozygous Genotypes with V_AD/DP above of below a given threshold (should be in Plugin Project, but filters from Plugins are not yet supported)

## 1.0.3 (2020-12-09)
### core
### functions
- `QC1078` : **fixed** GitHub.Issues#2 non HQ variants were set to missing, so HQrate was equal to callrate. HQ parameter is removed (and now equal to callrate)
- `QC1078` : **added** AB check for genotypes. Failed ab check will set genotype to missing.
### filters
### other
- `getCustomRequirement` : **fixed** typo in method name

## 1.0.2 (2020-11-05)
### core
### functions
- `AddAlleleBalance` : **fixed** GitHub.Issues#1 [`AB` field not added for missing genotypes] 
### filters
### other

## 1.0.1 (2020-09-30)
### core
### functions
- `CoverageStats` : **added**
- `GzPaste` : **changed** to use `LineBuilder`
### filters
### other
- `Gradle` : **added**

## 1.0.0 (2020-09-24)
### core
- `Fasta.getCharactersAt(String chromosome, long position, int length)` : **fixed** to be thread safe
- `VEPConsequence` : **changed** value index starts at 0 (for UNKNOWN) instead of -1
### functions
- `CheckReference` : **added** check for unexpected alleles (`'\n'` etc...)
- `CheckReference` : **added** testing script
- `CommonGenotypes` : **moved** to plugin Jar
- `CountInGnomAD` : **moved** to plugin Jar
- `FrequencyCorrelation` : **changed** `VEPConsequence` index starts at 0 instead of -1
- `GenerateHomoRefForPosition` : **added** testing script
- `Kappa` : **added** testing script
- `NumberOfCsqPerGene` : **changed** `VEPConsequence` index starts at 0 instead of -1
- `PrivateVSPanel` : **added** testing script
- `VCFToReference` : **added** testing script
### filters
### other
- `CommonLib` : **fixed** `LineBuilder`
- `Main.getVersion()` : **changed** to read this CHANGELOG file
- `Main.main(String[] args)` : **moved** to Run to enforce `assert`
- `FileParameter` : **fixed** `getExtension()` returns `String[]{}` instead of `null` by default
- `Documentation` : **added** versioning


