CREATE TABLE IF NOT EXISTS `orders`
(
    `id`         INT(11) NOT NULL,
    `auto_id`    INT(11) NOT NULL AUTO_INCREMENT,
    `storage_id` INT(11) NOT NULL,
    `product_id` VARCHAR(128),
    `count`      INT(11),
    `address`    VARCHAR(128) DEFAULT 'CHINA',
    PRIMARY KEY (id, auto_id)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE IF NOT EXISTS `storage`
(
    `id`         INT(11)     NOT NULL,
    `product_id` VARCHAR(32) NOT NULL,
    `used`       INT(11),
    PRIMARY KEY (id, product_id)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `storage`
VALUES (1, 'product-1', 0);