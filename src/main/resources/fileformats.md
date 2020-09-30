# File Formats

Here are the File Formats Handled by VCFProcessor

## VCF v4.2

VCF is a text file format (most likely stored in a compressed manner). It contains meta-information lines, a header line, and then data lines each containing information about a position in the genome. The format also has the ability to contain genotype information on samples for each position.

In the VCF file's body there is one line per genomic position containing a variant, and the column for those lines are :

| CHROM | POS | ID | REF | ALT | QUAL | FILTER | INFO | FORMAT | Sample1 | Sample2 | ... | SampleN |
|-------|-----|----|-----|-----|------|--------|------|--------|---------|---------|-----|---------|

See the full specifications : https://samtools.github.io/hts-specs/VCFv4.2.pdf

## PED

The PED file formats used in VCFProcessor is an adaptation of the PED/FAM format used in PLINK.

It is used to specify information about the samples present in the VCF files, and to describe relation between them. This tabulated text file has 7 columns :

| FamID | ID | MID | FID | Sex | Pheno | Group |
|-------|----|-----|-----|-----|-------|-------|

1. FamID : the family ID (all samples in the same family share the same FamID)
2. ID : the sample's unique ID
3. MID : the ID of the sample's mother
4. FID : the ID of the sample's father
5. Sex : the sample's sex (0 male / 1 female)
6. Pheno : the sample's phenotype (0 unaffected / 1 affected)
7. Group : the group to which the sample belongs (batch id, population, symptoms, ...)

## BED

The BED format consists of one line per feature, each containing 3-12 columns of data, plus optional track definition lines.

The first three fields in each feature line are required:

1. chrom - name of the chromosome or scaffold. Any valid seq_region_name can be used, and chromosome names can be given with or without the 'chr' prefix.
2. chromStart - Start position of the feature in standard chromosomal coordinates (i.e. first base is 0).
3. chromEnd - End position of the feature in standard chromosomal coordinates

See the full specifications : https://m.ensembl.org/info/website/upload/bed.html