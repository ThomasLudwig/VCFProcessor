# Overview

## Introduction

VCFProcessor is a tool to handle VCF File. In some points it is similar to vcftools and bcftools.

Follow [On GitHub](https://github.com/ThomasLudwig/VCFProcessor)

## Principle

VCFProcessor articulates itself around two main concepts :

* [functions](functions)
* [filters](filters)

### Functions

VCFProcessor can apply one function at a time. A function is a treatment to execute on the input VCF file, it can be an analysis, an annotation, a transformation, a formatting operation, etc. The command line to do this is

```bash
java -jar VCFProcessor.jar FunctionName Mandatory_Function_Arguments [Optional_Function_Arguments] [Filters]
```
For the full list of Functions, see [functions](functions)

### Filters

Filters are rules indicating which variants/genotypes/samples to filter or keep before executing a function.

For the full list of filters, see [filters](filters)