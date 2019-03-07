
UPDATE ifxdbloader_objhash 
SET (srchash, cathash, refuser, reftime) = (?, ?, USER, current year to second)
WHERE objtype = ? AND objname = ? AND owner = ? 