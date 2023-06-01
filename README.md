# R - Tree with REST API
### Overview
 - Persistent database of n dimensional objects which is cached and retrieved as needed.
 - Indexing is done using R-Tree.
 - Objects in database can be points or hyper cuboids given in a format of axis aligned bounding boxes.
 - Provides REST API for connection to new database or already established database, kNN query, and region query.
 - Inserts return IDs od the object inserted unique to the database.
