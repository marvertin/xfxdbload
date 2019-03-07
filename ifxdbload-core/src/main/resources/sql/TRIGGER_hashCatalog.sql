
select trigname as nazev, data, b.trigid
from systrigbody b join systriggers t on b.trigid = t.trigid
where datakey in ('A', 'D') and owner = ?
order by b.trigid, datakey, seqno