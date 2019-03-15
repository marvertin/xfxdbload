select t.tabname,c.colname 
from systables t join syscolumns c on c.tabid = t.tabid  
where t.tabid > 99 and t.tabtype='T'
order by t.tabid,c.colno