/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50722
Source Host           : localhost:3306
Source Database       : coin_dark

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2018-08-07 09:52:23
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for block
-- ----------------------------
DROP TABLE IF EXISTS `block`;
CREATE TABLE `block` (
  `block_hash` char(64) NOT NULL,
  `previous_block_hash` char(64) DEFAULT NULL,
  `next_block_hash` char(64) DEFAULT NULL,
  `merkleroot` char(64) DEFAULT NULL,
  `chainwork` char(64) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `height` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  `median_time` bigint(20) DEFAULT NULL,
  `bits` varchar(255) DEFAULT NULL,
  `nonce` bigint(20) DEFAULT NULL,
  `difficulty` double DEFAULT NULL,
  `status` tinyint(3) NOT NULL DEFAULT '1' COMMENT '0:无效，1:有效',
  `create_ts` datetime DEFAULT NULL,
  `update_ts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`block_hash`),
  KEY `idx_height` (`height`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for input
-- ----------------------------
DROP TABLE IF EXISTS `input`;
CREATE TABLE `input` (
  `block_hash` char(64) DEFAULT NULL,
  `transaction_txid` char(64) NOT NULL COMMENT '当前vin所属transaction',
  `txid` char(64) DEFAULT NULL,
  `coinbase` varchar(512) DEFAULT NULL,
  `vout` int(11) DEFAULT NULL,
  `sequence` bigint(20) DEFAULT NULL,
  `script_sig_hex` mediumtext,
  `script_sig_asm` text,
  `status` tinyint(3) NOT NULL DEFAULT '1' COMMENT '0:无效，1:有效',
  `create_ts` datetime DEFAULT NULL,
  `update_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for output
-- ----------------------------
DROP TABLE IF EXISTS `output`;
CREATE TABLE `output` (
  `wallet_account_id` bigint(20) DEFAULT NULL,
  `block_hash` char(64) DEFAULT NULL,
  `transaction_txid` char(64) NOT NULL COMMENT '当前vout所属transaction',
  `value` double DEFAULT NULL,
  `value_sat` bigint(20) DEFAULT NULL,
  `n` int(11) DEFAULT NULL,
  `script_pub_key_asm` varchar(512) DEFAULT NULL,
  `script_pub_key_hex` mediumtext,
  `script_pub_key_req_sigs` int(11) DEFAULT NULL,
  `script_pub_key_type` char(64) DEFAULT NULL,
  `script_pub_key_addresses` varchar(512) DEFAULT NULL,
  `status` tinyint(3) NOT NULL DEFAULT '1' COMMENT '0:无效，1:有效',
  `create_ts` datetime DEFAULT NULL,
  `update_ts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_txid_n` (`transaction_txid`,`n`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for transaction
-- ----------------------------
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `txid` char(64) NOT NULL,
  `hex` mediumtext,
  `size` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `locktime` bigint(20) DEFAULT NULL,
  `height` bigint(20) DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  `block_hash` char(64) NOT NULL,
  `block_time` bigint(20) DEFAULT NULL,
  `status` tinyint(3) NOT NULL DEFAULT '1' COMMENT '0:无效,1:有效',
  `create_ts` datetime DEFAULT NULL,
  `update_ts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`txid`),
  KEY `idx_block_hash` (`block_hash`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
