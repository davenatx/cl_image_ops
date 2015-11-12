# cl_image_ops
The purpose of this project is to identify cropped images (images where the image length is less than the image width) and overlay the image onto a standard size page.  This is accomplished by reading the "CL" image repository and inserting a database record representing each TIFF image into the an H2 database.  This step is necessary because we need to use SQL to investigate this data to determine how to complete this project.  For example, we need to not only determine which images are "cropped", but we also need to identify the different resolutions in order to create a process to "fix" them.

All of the images presently in the "CL" image repository are read and stored in the H2 database.

##Database 

This database has one table, IMAGE_RECORDS.  This table has the following fields:

  * FNAME - File Name
  * FPATH - File Path
  * FILEDATE - File Date created by the image repository path. e.g. .../YYYY/MM/DD
  * COMPRESSION - TIFF compression
  * IMG_WIDTH - Pixel Width of Image
  * IMG_LENGTH - Pixel Length of Image
  * X_RESOLUTION - X Resolution in DPI
  * Y_RESOLUTION - Y Resolution in DPI
  * IS_OVERLAYED - Indicates if the image has been overlayed

##SQL

Below is some helpful SQL for investigating this database:

This SQL creates an index on the IMG_LENGTH field and orders it in descending order.  Presently, there does not appear to be a way for Slick 2 to do this:  https://github.com/slick/slick/issues/1035.  This is necessary because it helps H2 perform order by operations:

````
CREATE INDEX IDX_IMG_LENGTH_DESC ON IMAGE_RECORDS(IMG_LENGTH DESC);  
````

Find the number of records in the database:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS;

COUNT(*)  
1218388
````

Find the number of unique file names in the database:

````
SELECT DISTINCT COUNT(FNAME)
FROM IMAGE_RECORDS;

COUNT(FNAME)  
1218388
````

Find the number of records where the X_RESOLUTION does not equal the Y_RESOLUTION:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION != Y_RESOLUTION;

COUNT(*)  
2
````

Find the records where the X_RESOLUTION does not equal the Y_RESOLUTION:

````
SELECT FNAME, FPATH
FROM IMAGE_RECORDS
WHERE X_RESOLUTION != Y_RESOLUTION;

FNAME  			FPATH  
198202265.002	C:\ADINAS01\CL\R\1982\05\18
198202265.003	C:\ADINAS01\CL\R\1982\05\18
````

Find the distinct X_RESOLUTIONs:

````
SELECT DISTINCT X_RESOLUTION
FROM IMAGE_RECORDS
ORDER BY X_RESOLUTION;

X_RESOLUTION  
96
100
150
200
240
292
300
400
600
1200
````

Find the number of images with an X_RESOLTUION of 96:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 96;

COUNT(*)  
634999
````

Find the number of images with an X_RESOLTUION of 100:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 100;

COUNT(*)  
35
````

Find the number of images with an X_RESOLTUION of 150:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 150;

COUNT(*)  
38
````

Find the number of images with an X_RESOLTUION of 200:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 200;

COUNT(*)  
232067
````

Find the number of images with an X_RESOLTUION of 240:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 240;

COUNT(*)  
1608
````

Find the number of images with an X_RESOLTUION of 292:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 292;

COUNT(*)  
2
````

Find the number of images with an X_RESOLTUION of 300:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 300;

COUNT(*)  
173712
````

Find the number of images with an X_RESOLTUION of 400:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 400;

COUNT(*)  
175907
````

Find the number of images with an X_RESOLTUION of 600:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 600;

COUNT(*)  
19
````

Find the number of images with an X_RESOLTUION of 1200:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE X_RESOLUTION = 1200;

COUNT(*)  
1
````

# Stopped Here!

Find the number of images where the image length is less than the image width:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE IMG_LENGTH < IMG_WIDTH;

COUNT(*)  
19257
````

Find the X_RESOLUTIONs that exist in the set of images where the image length is less than the image width:

````
SELECT DISTINCT(X_RESOLUTION)
FROM IMAGE_RECORDS
WHERE IMG_LENGTH < IMG_WIDTH;

X_RESOLUTION  
72
200
300
400
````

Find the number of images where the X_RESOLUTION is 72 and the image length is less than the image width:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE IMG_LENGTH < IMG_WIDTH
AND X_RESOLUTION = 72;

COUNT(*)  
184
````

Find the number of images where the X_RESOLUTION is 200 and the image length is less than the image width:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE IMG_LENGTH < IMG_WIDTH
AND X_RESOLUTION = 200;

COUNT(*)  
2
````

Find the number of images where the X_RESOLUTION is 300 and the image length is less than the image width:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE IMG_LENGTH < IMG_WIDTH
AND X_RESOLUTION = 300;

COUNT(*)  
19068
````

Find the number of images where the X_RESOLUTION is 400 and the image length is less than the image width:

````
SELECT COUNT(*)
FROM IMAGE_RECORDS
WHERE IMG_LENGTH < IMG_WIDTH
AND X_RESOLUTION = 400;

COUNT(*)  
3
````

Find **bad** images where the IMG_LENGTH is 1:

````
SELECT *
FROM IMAGE_RECORDS
WHERE IMG_LENGTH = 1;

FNAME       FPATH              FILEDATE   COMPRESSION   IMG_WIDTH   IMG_LENGTH    X_RESOLUTION    Y_RESOLUTION  IS_OVERLAYED  ID  
127284.007  \HC\R\1979\03\27   1979-03-27 4             1696      1             200             200           FALSE     221882
171445.001  \HC\R\1983\06\28   1983-06-28 4             1696      1             200             200             FALSE     287357
````

##Identified Cropped Image Segments

* For the 184 images in this segment with 72 DPI, determine the proper image length for the image to overlay on and process this segment.
  
  It appears these images are really 300 DPI images that were cropped and had the resolution incorrectly set:
  
  ````
  FNAME       FPATH              FILEDATE   COMPRESSION   IMG_WIDTH   IMG_LENGTH    X_RESOLUTION    Y_RESOLUTION  IS_OVERLAYED  ID  
  53255.001     \HC\R\1965\09\23   1965-09-23 4             3568      5536          300             300             FALSE     119609
  53255.002     \HC\R\1965\09\23   1965-09-23 4             3568      5536          300             300             FALSE     119610
  53255.003     \HC\R\1965\09\23   1965-09-23 4             3568      1859          72              72              FALSE     119611
  ````

* For the 2 images in this segment with 200 DPI, handle these **manually**:
  
  \HC\R\1979\03\27\127284.007 **(Bad Image)**
  \HC\R\1983\06\28\171445.001 **(Bad Image)**

* For the 19,076 images in this segment with 300 DPI, use an image length of 5527 (calculated above) as the image length for the image to overlay on.

* For the 3 images in this segment with 400 DPI, handle these **manually**:
  
  \HC\R\1943\10\04\1943128046.001 **(Plat)**
  \HC\R\1964\03\23\49080.001 **(Plat)**
  \HC\R\1973\11\06\85188.016 **(CCR Map)**

##Solution Overview

This process will query cropped image records.  Then it will overlay the images in order to "fix" them.  Finally, these images are "exported" to another directory so they can be quality controlled and indexed.  Because our indexer cannot index single pages of a document, I need to ensure I design this process to "export" all of the pages of a document regardless if all the pages were overlayed.  Also, I would like to organize the export directory by year.  Therefore, I should point to a parent "export" directory.  Then, each record that is exported should reside in a "yearly" folder under this "export" directory.

Outline of high-level approach:

1. Determine if each FNAME (filename) in the database is unique. **Yes**

2. Determine an average image length to use as a default image length for the image to overlay on.  Because the 72 DPI images are really 300 DPI images, use one factor.  The average image length calculated above is 5527.  However, it appears 5536 is the most common image length in this segment.  **I am using 5536**

3. Query the cropped image records that are 72 DPI or 300 DPI together because they are **really** the same resolution:

  ````
  SELECT COUNT(*)
  FROM IMAGE_RECORDS
  WHERE IMG_LENGTH < IMG_WIDTH
  AND (X_RESOLUTION = 300 OR X_RESOLUTION=72);
  
  COUNT(*)  
  19252
  ````

4. Process the results, and overwrite the existing image with the OverlayImage.overlay().

5. For each image that is overlayed, update the IS_OVERLAYED field in the database to true.

6. Query records where IS_OVERALYED = true

7. Map over this list.  Split the fileName on the "." to obtain the document number this image or page represents.

8. Query the records where the FNAME is like the document number extracted in step 7.  This process serves to retrieve all of the pages that belong to a document. 

9. For each record in the list from step 8, copy each file to a "yearly" folder based on the year associated with the record.

10. In order to successfully do this, ensure the yearly folder exists in the copy operation.  If it does not, create it.

11. The copy process will overwrite some existing images in the yearly folder.  However, this should not be a problem because 1) all the file names in the database are unique and 2) this is an expected case because if the first and last page of a document were both overlayed, both pages will be represented in the records returned in step 6.  Therefore, step 8 will process the same document twice.  While this is not necessarily the most efficient way to handle this process, it is probably the simplest.

12. To determine the export routine exports the expected number of images, the export process should implement a counter.  It should output the number or records returned from step 6.  This number represents each page of an image, or record, that was overlayed.  **We already know it should equal 19,252.**  For the copy process in step 8, a counter should track the running total of files copies.  It should also handle the case where the files already exist, like is mentioned in step 11, and omit these from the total.  Ultimately, this total will represent the number of unique files copied.  This total can be confirmed on the file system once the "export" process is complete.

##Solution Code

* **ImageReader.scala** drops and creates the database.  It also scans the souceDirectory specified in the settings.properties file and populates the database.

* **ImageOverlay.scala** queries the database for cropped images and overlays them.

* **ImageExport.scala** queries the database for overlayed images and exports all the files representing a document where at least one page was overlayed.  These files are exported to the exportDirectory specified in the settings.properties file.