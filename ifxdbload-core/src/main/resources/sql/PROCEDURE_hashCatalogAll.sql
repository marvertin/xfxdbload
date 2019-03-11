select p.procname as nazev,b.data,p.procid 
from sysprocedures p join sysprocbody b on p.procid=b.procid
where p.mode!='o' and p.mode!='r' AND p.mode!='d' and b.datakey='T' and owner = ?
order by p.procid,b.seqno