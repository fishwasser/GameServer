rd /s /q ..\gamemanager-server\src\main\java\com\fish\yz\protobuf
rd /s /q ..\netty-client-demo\src\main\java\com\fish\yz\protobuf
rd /s /q ..\game-server\src\main\java\com\fish\yz\protobuf
rd /s /q ..\netty-demo\src\main\java\com\fish\yz\protobuf
rd /s /q ..\db-server\src\main\java\com\fish\yz\protobuf

protoc.exe --java_out=..\gamemanager-server\src\main\java\ common.proto
protoc.exe --java_out=..\netty-client-demo\src\main\java\ common.proto
protoc.exe --java_out=..\game-server\src\main\java\ common.proto
protoc.exe --java_out=..\netty-demo\src\main\java\ common.proto
protoc.exe --java_out=..\db-server\src\main\java\ common.proto

pause

