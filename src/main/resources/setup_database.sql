/** 
  * Run this SQL script to setup a database which can be used by DroiD64.     *
  * Currently, MySQL, PostgreSQL, H2 and FirebirdSQL has been verified to     *
  * work with DroiD64. You might need to modify it to match your environment. *
  * The SQL script here has proved to work for me. Your mileage may vary.     *
  *
  * Note: this SQL script contains several different scripts to fit different
  *       databases. Comment out and uncomment the section for your database.
  *
  * Script updated for DroiD64 version 0.13b.
  * See http://droid64.sourceforge.net/ for latest updates.
  */

/**
  * Setup MySQL and H2 databases for DroiD64.
  */

CREATE DATABASE IF NOT EXISTS droid64;

GRANT ALL PRIVILEGES  ON droid64.* TO 'droid64'@'localhost' IDENTIFIED BY 'uridium' WITH GRANT OPTION;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS diskfile (
  fileid INTEGER(11) NOT NULL AUTO_INCREMENT,
  diskid INTEGER(11) NOT NULL,
  name VARCHAR(32) NOT NULL,
  filetype INTEGER(1) NOT NULL DEFAULT 1,
  size INTEGER(4) NOT NULL DEFAULT 0,
  filenum INTEGER(4) NOT NULL DEFAULT 0,
  flags INTEGER(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (fileid),
  FOREIGN KEY (diskid) REFERENCES disk(diskid) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

/** Apply changes for DroiD64 version 0.1 */
ALTER TABLE disk ADD COLUMN IF NOT EXISTS imagetype int NOT NULL;
ALTER TABLE disk ADD COLUMN IF NOT EXISTS errors int DEFAULT NULL;
ALTER TABLE disk ADD COLUMN IF NOT EXISTS warnings int DEFAULT NULL;
/** Apply changes for DroiD64 version 0.13 */
ALTER TABLE disk ADD COLUMN IF NOT EXISTS hostname VARCHAR(255) DEFAULT NULL;

/** End of setup for MySQL and H2 database **/

/**
  * Setup PostgreSQL database for DroiD64.
  *
  * Example setup is using these settings:
  * JDBC Driver : org.postgresql.Driver
  * URL         : jdbc:postgresql://localhost:5432/droid6
  * User        : droid64
  * Password    : uridium
  */
/* Uncomment if you need PostgreSQL **
CREATE USER droid64 WITH PASSWORD 'uridium';
CREATE DATABASE droid64 WITH ENCODING 'UTF8' LC_CTYPE='en_US.UTF-8' OWNER droid64;

\connect droid64
CREATE TABLE IF NOT EXISTS disk (
  diskid SERIAL PRIMARY KEY,
  filepath VARCHAR(500) NOT NULL,
  filename VARCHAR(100) NOT NULL,
  label VARCHAR(32) DEFAULT NULL,
  updated TIMESTAMP NOT NULL,
  imagetype INTEGER NOT NULL,
  errors INTEGER DEFAULT NULL,
  warnings INTEGER DEFAULT NULL,
  hostname VARCHAR(256) DEFAULT NULL );

CREATE TABLE IF NOT EXISTS diskfile (
  fileid SERIAL PRIMARY KEY,
  diskid INTEGER NOT NULL REFERENCES disk(diskid) ON DELETE CASCADE,
  name VARCHAR(32) NOT NULL,
  filetype INTEGER NOT NULL DEFAULT 1,
  size INTEGER NOT NULL DEFAULT 0,
  filenum INTEGER NOT NULL DEFAULT 0,
  flags INTEGER NOT NULL DEFAULT 0 );
  
GRANT ALL PRIVILEGES ON disk TO droid64;
GRANT ALL PRIVILEGES ON diskfile TO droid64;
GRANT USAGE, SELECT ON SEQUENCE disk_diskid_seq TO droid64;
GRANT USAGE, SELECT ON SEQUENCE diskfile_fileid_seq TO droid64;

*/

/**
  * Setup Firebird SQL database for DroiD64.
  *
  * Example setup is using these settings:
  * JDBC Driver : org.firebirdsql.jdbc.FBDriver
  * URL         : jdbc:firebirdsql://localhost:3050//var/lib/firebird/data/droid64?encoding=ISO8859_1
  * User        : droid64
  * Password    : uridium
  */
/* Uncomment if you need FirebirdSQL **

* First run these command line commands:
* gsec -user sysdba -pass masterkey -add droid64 -pw uridium
* isql -u droid64 -p uridium

CREATE DATABASE '/var/lib/firebird/data/droid64';
CONNECT  '/var/lib/firebird/data/droid64';

CREATE TABLE disk (
  diskid INTEGER NOT NULL,
  filepath VARCHAR(500) NOT NULL,
  filename VARCHAR(100) NOT NULL,
  label VARCHAR(32) DEFAULT NULL,
  updated TIMESTAMP NOT NULL,
  imagetype INTEGER NOT NULL,
  errors INTEGER DEFAULT NULL,
  warnings INTEGER DEFAULT NULL,
  hostname VARCHAR(256) DEFAULT NULL,
  PRIMARY KEY (diskid)
);

CREATE TABLE diskfile (
  fileid INTEGER NOT NULL,
  diskid INTEGER NOT NULL,
  name VARCHAR(32) NOT NULL,
  filetype INTEGER default 1 NOT NULL,
  size INTEGER default 0 NOT NULL,
  filenum INTEGER default 0 NOT NULL,
  flags INTEGER default 0 NOT NULL,
  PRIMARY KEY (fileid),
  FOREIGN KEY (diskid) REFERENCES disk(diskid) ON DELETE CASCADE
);

CREATE SEQUENCE disk_id_sequence;
CREATE SEQUENCE diskfile_id_sequence;

set term !! ;
CREATE TRIGGER disk_trigger FOR disk
ACTIVE BEFORE INSERT POSITION 0
AS
BEGIN
if (NEW.diskID is NULL) then NEW.diskID = GEN_ID(disk_id_sequence, 1);
END!!
set term ; !!

set term !! ;
CREATE TRIGGER diskfile_trigger FOR diskfile
ACTIVE BEFORE INSERT POSITION 0
AS
BEGIN
if (new.fileid is NULL) then new.fileid = GEN_ID(diskfile_id_sequence, 1);
END!!
set term ; !!
 
 */



/** End of file */
