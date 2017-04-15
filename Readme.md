Facebook Export EXIF Enricher

This application solves the common hurdle of missing EXIF photo information when photos are exported from facebook.
This can be really irritating when importing photos into a service like google photos, where the date used is incorrect.

My program will:
- trawl through the generated htm files from the export archive
- retrieve EXIF information
- apply this EXIF data to the exported photos

This application requires a facebook export, which is detailed here:
https://www.facebook.com/help/131112897028467

Current features:

- Retrieve date for image and set the EXIF property "Date Taken"
- Set the GPS properties longitude and latitude

Usage:

run on the command line (sbt):
env JAVA_OPTS="-Dfile.dir.input=facebookexportdirectory -Dfile.dir.output=outputdirectory" sbt run

Contact: johnreganprojects@gmail.com