
CREATE TABLE ifxdbloader_objhash (
  objtype VARCHAR(32) not null,
  objname VARCHAR(128) not null,
  owner  VARCHAR(32) not null,
  srchash CHAR(40) not null,
  cathash CHAR(40) not null,
  PRIMARY KEY (objtype, objname, owner)
)