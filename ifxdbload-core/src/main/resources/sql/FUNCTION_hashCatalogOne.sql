select b.data 
from sysprocedures p join sysprocbody b on p.procid=b.procid
where b.datakey='T' and p.procname = ? and p.owner = ?
order by p.procid,b.seqno