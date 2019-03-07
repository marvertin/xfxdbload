
CREATE TABLE ifxdbloader_objhash (
  objtype VARCHAR(32) not null,
  objname VARCHAR(128) not null,
  owner  VARCHAR(32) not null,
  srchash CHAR(40) not null,
  cathash CHAR(40) not null,

  poruser VARCHAR(32) default USER not null,
  portime DATETIME year to second default current year to second not null,
  refuser VARCHAR(32) default USER not null,
  reftime DATETIME year to second default current year to second not null,


  PRIMARY KEY (objtype, objname, owner)
)

LOCK MODE ROW
