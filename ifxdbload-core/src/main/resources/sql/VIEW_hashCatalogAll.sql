

select tabname as nazev, viewtext as data
from sysviews  v join systables t on v.tabid = t.tabid
order by v.tabid, seqno