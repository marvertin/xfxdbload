select viewtext as data
from sysviews  v join systables t on v.tabid = t.tabid
where tabname = ?
order by v.tabid, seqno