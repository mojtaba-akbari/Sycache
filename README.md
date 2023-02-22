# Sycache

{"headCommand":"help","poolName":"","tailCommand":[]}

{"headCommand":"add-pool","poolName":"default","tailCommand":["1000","HashMapPool","ByteNode"]}

{"headCommand":"set","poolName":"default","tailCommand":["test","test","","0","0","1000","ENTITYID=id,DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test","23450","","2","0","1000","DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","test2","","0","0","1000","DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","2","","0","0","1000","ENTITYID=id,DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","{\"id\":16}","","0","0","1000","ENTITYID=id,DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","{\"id\":20}","","0","0","1000","ENTITYID=id,DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","{\"id\":20}","","0","0","1000","FILEEXT=txt,BUCKETNAME=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","{\"id\":20}","","0","0","1000","TOPICNAME=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","20","","0","0","1000","DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","test2","user-125","0","0","20","DBTABLE=test"]}

{"headCommand":"set","poolName":"default","tailCommand":["test2","test2","","0","0","0","DBTABLE=test"]}

{"headCommand":"get-tag","poolName":"default","tailCommand":["user-125"]}

{"headCommand":"invalid-tag","poolName":"default","tailCommand":["user-125"]}

{"headCommand":"get","poolName":"default","tailCommand":["test2"]}

{"headCommand":"get","poolName":"default","tailCommand":["test"]}

{"headCommand":"flush","poolName":"default","tailCommand":[]}

{"headCommand":"delpool","poolName":"default","tailCommand":[]}

{"headCommand":"quit","poolName":"","tailCommand":[]}

{"headCommand":"stat-pool","poolName":"default","tailCommand":[]}

{"headCommand":"list-pipeline","poolName":"default","tailCommand":[]}

{"headCommand":"stop-pipeline","poolName":"default","tailCommand":["0"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["MOV,tmp:testtmp,default:test2","MOV,default:test,tmp:testtmp"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["MOV,tmp:testtmp,dir:abc","MOV,default:test,tmp:testtmp"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["MOV,tmp:test2tmp,default:test2","LEN,default:test,tmp:test2tmp","LEN,tmp:testlen,default:test","INC,tmp:testlen","LEN,tmp:testreallen,default:test"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["LEN,tmp:testlen,default:test","INC,tmp:testlen","INC,tmp:testlen","MOV,default:test2,tmp:testlen"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["APP,default:test,append"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["MOV,default:test2,dir:2"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["MOV,tmp:tmpNumber,dir:10","MOV,default:test,tmp:tmpNumber","INC,default:test,13"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["MOV,default:test2,dir:4"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["MOV,tmp:tmpNumber,dir:32754","INC,tmp:tmpNumber,13", "MOV,default:test,tmp:tmpNumber"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["WHS,default:test2"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["WHS,default:test2"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["WHS,default:test2","MOV,default:test,dir:true"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["WHV,default:test2,10"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["INV,default:user-125"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:x=>parseInt(x)+1,valueIncreaseByOne,false,true,MYSQl"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:,valueIncreaseByOne,false,true,STDOUT"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:,,false,false,MYSQL"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:,,false,false,S3"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:,,false,false,KAFKA"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:,,false,false,REDIS"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:,,false,false,STDOUT"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["STM,default:(x)=>{y=JSON.parse(x);y.id+=1;return JSON.stringify(y);},,false,true,MYSQL"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["ITV,default:,valueIncreaseByOne,false,true"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["IFV,default:test2,eq,1","INC,default:test2,1","INC,default:test2,2","IFVE","INC,default:test2,10"]}

{"headCommand":"pipeline-sync","poolName":"default","tailCommand":["IFV,default:test2,eq,12","INC,default:test2,1","INC,default:test2,2","IFVE","DEC,default:test2,2"]}

{"headCommand":"pipeline-async","poolName":"default","tailCommand":["MOV,tmp:tmpValue,default:test2","IFV,tmp:tmpValue,gt,2","MOV,tmp:counter,dir:3","LOP,tmp:counter","MUL,tmp:tmpValue,2","LOPE","IFVE","MOV,default:test2,tmp:tmpValue"]}
