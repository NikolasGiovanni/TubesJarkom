DROP TABLE IF EXISTS UserChat
--cek ada berapa user
CREATE TABLE UserChat(
	IdUser int NOT NULL IDENTITY PRIMARY KEY,
	Username varchar(255) NOT NULL,
	Password varchar(255) NOT NULL,
	NamaTampilan varchar(255) NOT NULL
)

SELECT * FROM UserChat