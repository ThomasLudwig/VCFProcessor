* when filtering samples : `SampleIDFilter{Keep samples : 370,370,370,370,370,...and 365 more}` instead of the IDs
* do not need ALLELE_NUM to gnomad_AF as they are included in each annotation
* ability to work with VCF file that contain no samples (example for `ShowFields`)
* `ClassNotFoundException` when older plugin are in the plugin dir. Error message needs to be more explicit.