CREATE TABLE IF NOT EXISTS `settlement_mission` (

`id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
`external_party` varchar(30) NOT NULL,
`instrument` varchar(30) NOT NULL,
`depot` varchar(10) NOT NULL,
`direction` varchar(10) NOT NULL,
`qty` double NOT NULL,
`version` bigint NOT NULL

)ENGINE=InnoDB DEFAULT CHARSET=UTF8;