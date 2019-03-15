UPDATE xonce_scripts 
SET (status, checksum, description, comments, 
    refuser, reftime) =
    (?, ?, ?, ?,
    USER, current year to second)
WHERE scriptid = ? 
