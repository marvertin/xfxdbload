
select data
from systrigbody b join systriggers t on b.trigid = t.trigid
where datakey in ('A', 'D') and trigname = ? and owner = ? 
order by b.trigid, datakey, seqno