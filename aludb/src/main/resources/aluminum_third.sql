use aluminum;
create table third  select * from second limit 0;

insert into third (jialiaoliang,jialiaocishu,gongzuoshidianya,zaosheng,yangjixingchengri,yangjixingchengdunlv,
dianjiewendu,fenzibi,alfjialiaoshiji,lvshuiping,dianjiezhishuiping,fehanliang,sihanliang,shijichulvliang,dianliuxiaolv,
class) select jialiaoliang,jialiaocishu,gongzuoshidianya,zaosheng,yangjixingchengri,yangjixingchengdunlv,
dianjiewendu,fenzibi,alfjialiaoshiji,lvshuiping,dianjiezhishuiping,fehanliang,sihanliang,shijichulvliang,dianliuxiaolv,
class from first where class is not null;

update third set class =null;

update third set class ='medium' where dianliuxiaolv>=85 and dianliuxiaolv<=87;
update third set class ='good' where dianliuxiaolv>=88 and dianliuxiaolv<=90;
update third set class ='excellent' where dianliuxiaolv>=91 and dianliuxiaolv<=93;