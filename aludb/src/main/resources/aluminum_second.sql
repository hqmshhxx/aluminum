use aluminum;
DROP TABLE IF EXISTS `second`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `second` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `jialiaoliang` int(20) DEFAULT NULL COMMENT '加料量(kg)',
  `jialiaocishu` int(20) DEFAULT NULL COMMENT '加料次数',
  `gongzuoshidianya` float(4,3) DEFAULT NULL COMMENT '工作时电压',
  `zaosheng` int(11) DEFAULT NULL COMMENT '噪声(mv)',
  `yangjixingchengri` int(11) DEFAULT NULL COMMENT '阳极行程日(mm)',
  `yangjixingchengdunlv` int(11) DEFAULT NULL COMMENT '阳极行程吨铝(mm)',
  `dianjiewendu` int(11) DEFAULT NULL COMMENT '电解温度',
  `fenzibi` float(4,3) DEFAULT NULL COMMENT '分子比',
  `alfjialiaoshiji` int(11) DEFAULT NULL COMMENT 'ALF加料实际',
  `lvshuiping` int(11) DEFAULT NULL COMMENT '铝水平',
  `dianjiezhishuiping` int(11) DEFAULT NULL COMMENT '电解质水平',
  `fehanliang` float(4,3) DEFAULT NULL COMMENT 'Fe含量',
  `sihanliang` float(4,3) DEFAULT NULL COMMENT 'Si含量',
  `shijichulvliang` int(11) DEFAULT NULL COMMENT '实际出铝量',
  `dianliuxiaolv` float(7,3) DEFAULT NULL,
  `class` varchar(10) DEFAULT NULL COMMENT '类别',
  PRIMARY KEY (`id`),
  KEY `classIndex` (`class`),
  KEY `xiaolvIndex` (`dianliuxiaolv`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8;

insert into second (jialiaoliang,jialiaocishu,gongzuoshidianya,zaosheng,yangjixingchengri,yangjixingchengdunlv,
dianjiewendu,fenzibi,alfjialiaoshiji,lvshuiping,dianjiezhishuiping,fehanliang,sihanliang,shijichulvliang,dianliuxiaolv,
class) select jialiaoliang,jialiaocishu,gongzuoshidianya,zaosheng,yangjixingchengri,yangjixingchengdunlv,
dianjiewendu,fenzibi,alfjialiaoshiji,lvshuiping,dianjiezhishuiping,fehanliang,sihanliang,shijichulvliang,dianliuxiaolv,
class from first where caoxing = '正常' and class is not null;