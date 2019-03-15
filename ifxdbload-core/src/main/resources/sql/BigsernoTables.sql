SELECT t.tabname 
FROM systables t JOIN syscolumns c ON C.tabid = t.tabid 
WHERE t.tabtype='T' AND t.tabid > 99 AND C.colname='serno' AND coltype in (52,308)
order by t.tabid