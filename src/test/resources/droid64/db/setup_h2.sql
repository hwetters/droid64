
CREATE TABLE IF NOT EXISTS disk (
  diskid INTEGER(11) NOT NULL AUTO_INCREMENT,
  filepath VARCHAR(500) NOT NULL,
  filename VARCHAR(100) NOT NULL,
  label VARCHAR(32) DEFAULT NULL,
  updated TIMESTAMP NOT NULL,
  imagetype INTEGER(4) NOT NULL,
  errors INTEGER(4) DEFAULT NULL,
  warnings INTEGER(4) DEFAULT NULL,
  hostname VARCHAR(256) DEFAULT NULL,
  PRIMARY KEY (diskid)
);

CREATE TABLE IF NOT EXISTS diskfile (
  fileid INTEGER(11) NOT NULL AUTO_INCREMENT,
  diskid INTEGER(11) NOT NULL,
  name VARCHAR(32) NOT NULL,
  filetype INTEGER(1) NOT NULL DEFAULT 1,
  size INTEGER(4) NOT NULL DEFAULT 0,
  filenum INTEGER(4) NOT NULL DEFAULT 0,
  flags INTEGER(4) NOT NULL DEFAULT 0,
  namebytes BLOB,
  PRIMARY KEY (fileid),
  FOREIGN KEY (diskid) REFERENCES disk(diskid) ON DELETE CASCADE
);

